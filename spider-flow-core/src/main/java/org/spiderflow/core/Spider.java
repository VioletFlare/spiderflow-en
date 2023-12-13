package org.spiderflow.core;

import com.alibaba.ttl.TtlRunnable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spiderflow.concurrent.*;
import org.spiderflow.concurrent.SpiderFlowThreadPoolExecutor.SubThreadPoolExecutor;
import org.spiderflow.context.SpiderContext;
import org.spiderflow.context.SpiderContextHolder;
import org.spiderflow.core.executor.shape.LoopExecutor;
import org.spiderflow.core.model.SpiderFlow;
import org.spiderflow.core.service.FlowNoticeService;
import org.spiderflow.core.utils.ExecutorsUtils;
import org.spiderflow.core.utils.ExpressionUtils;
import org.spiderflow.core.utils.SpiderFlowUtils;
import org.spiderflow.enums.FlowNoticeType;
import org.spiderflow.executor.ShapeExecutor;
import org.spiderflow.listener.SpiderListener;
import org.spiderflow.model.SpiderNode;
import org.spiderflow.model.SpiderOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bugzilla's core class
 *
 * @author jmxd
 */
@Component
public class Spider {

	@Autowired(required = false)
	private List<SpiderListener> listeners;

	@Value("${spider.thread.max:64}")
	private Integer totalThreads;

	@Value("${spider.thread.default:8}")
	private Integer defaultThreads;

	@Value("${spider.detect.dead-cycle:5000}")
	private Integer deadCycle;
	
	@Autowired
	private FlowNoticeService flowNoticeService;

	public static SpiderFlowThreadPoolExecutor executorInstance;

	private static final String ATOMIC_DEAD_CYCLE = "__atomic_dead_cycle";

	private static Logger logger = LoggerFactory.getLogger(Spider.class);

	@PostConstruct
	private void init() {
		executorInstance = new SpiderFlowThreadPoolExecutor(totalThreads);
	}

	public List<SpiderOutput> run(SpiderFlow spiderFlow, SpiderContext context, Map<String, Object> variables) {
		if (variables == null) {
			variables = new HashMap<>();
		}
		SpiderNode root = SpiderFlowUtils.loadXMLFromString(spiderFlow.getXml());
		// Start Event Notify
		flowNoticeService.sendFlowNotice(spiderFlow, FlowNoticeType.startNotice);
		executeRoot(root, context, variables);
		// End of process notification
		flowNoticeService.sendFlowNotice(spiderFlow, FlowNoticeType.endNotice);
		return context.getOutputs();
	}

	public List<SpiderOutput> run(SpiderFlow spiderFlow, SpiderContext context) {
		return run(spiderFlow, context, new HashMap<>());
	}

	public void runWithTest(SpiderNode root, SpiderContext context) {
		//Save Context toThreadLocal里，This information will be used to populate the 'Cc' field of the email.
		SpiderContextHolder.set(context);
		//Counter for Death Rattle checks（The death spiral check only works when you are in test mode.）
		AtomicInteger executeCount = new AtomicInteger(0);
		//Save to Context，For follow-up inspection
		context.put(ATOMIC_DEAD_CYCLE, executeCount);
		//Answer node
		executeRoot(root, context, new HashMap<>());
		//When the debugging task has finished,判断是否超过预期
		if (executeCount.get() > deadCycle) {
			logger.error("Detected possible death loop, aborting,Test canceled");
		} else {
			logger.info("Test Complete！");
		}
		//Will O The WispThreadLocalRemove，Prevent memory leaks
		SpiderContextHolder.remove();
	}

	/**
	 * Answer node
	 */
	private void executeRoot(SpiderNode root, SpiderContext context, Map<String, Object> variables) {
		//Get the number of currently running process threads
		int nThreads = NumberUtils.toInt(root.getStringJsonValue(ShapeExecutor.THREAD_COUNT), defaultThreads);
		String strategy = root.getStringJsonValue("submit-strategy");
		ThreadSubmitStrategy submitStrategy;
		//Choose Submission Strategy，Here you have to usenew,Cannot share with other instances
		if("linked".equalsIgnoreCase(strategy)){
			submitStrategy = new LinkedThreadSubmitStrategy();
		}else if("child".equalsIgnoreCase(strategy)){
			submitStrategy = new ChildPriorThreadSubmitStrategy();
		}else if("parent".equalsIgnoreCase(strategy)){
			submitStrategy = new ParentPriorThreadSubmitStrategy();
		}else{
			submitStrategy = new RandomThreadSubmitStrategy();
		}
		//Create child thread pool，Use one thread per child,Warning: Too many subthreads for a total of threads（Enter queue and wait）,+1It is because it takes one thread to schedule the next level
		SubThreadPoolExecutor pool = executorInstance.createSubThreadPoolExecutor(Math.max(nThreads,1) + 1,submitStrategy);
		context.setRootNode(root);
		context.setThreadPool(pool);
		//Trigger Listener
		if (listeners != null) {
			listeners.forEach(listener -> listener.beforeStart(context));
		}
		Comparator<SpiderNode> comparator = submitStrategy.comparator();
		//Start a thread to perform a task,and monitor its completion and carry out the next level
		Future<?> f = pool.submitAsync(TtlRunnable.get(() -> {
			try {
				//Answer
				Spider.this.executeNode(null, root, context, variables);
				Queue<Future<?>> queue = context.getFutureQueue();
				//Get help from a team memberFuture,Until the queue is empty,When a task is completed，Then execute the next level
				while (!queue.isEmpty()) {
					try {
						//TODO Here you can see the list of all your tasks.
						Optional<Future<?>> max = queue.stream().filter(Future::isDone).max((o1, o2) -> {
							try {
								return comparator.compare(((SpiderTask) o1.get()).node, ((SpiderTask) o2.get()).node);
							} catch (InterruptedException | ExecutionException e) {
							}
							return 0;

						});
						if (max.isPresent()) {	//Are we done?
							queue.remove(max.get());
							if (context.isRunning()) {	//Check if running(When you click on the page"Stop"时,This value isfalse,Other =true)
								SpiderTask task = (SpiderTask) max.get().get();
								task.node.decrement();	//任务执行完毕,Counter decreased by one(This counter is forJoinUse a countdown)
								if (task.executor.allowExecuteNext(task.node, context, task.variables)) {	//Next level
									logger.debug("执行节点[{}:{}]完毕", task.node.getNodeName(), task.node.getNodeId());
									//Execute Next Level
									Spider.this.executeNextNodes(task.node, context, task.variables);
								} else {
									logger.debug("执行节点[{}:{}]完毕，Ignore next node", task.node.getNodeName(), task.node.getNodeId());
								}
							}
						}
						//Sleep1ms,Exitcpu
						Thread.sleep(1);
					} catch (InterruptedException ignored) {
					} catch (Throwable t){
						logger.error("Application crashed",t);
					}
				}
				//Waiting for thread pool to complete
				pool.awaitTermination();
			} finally {
				//Trigger Listener
				if (listeners != null) {
					listeners.forEach(listener -> listener.afterEnd(context));
				}
			}
		}), null, root);
		try {
			f.get();	//Block until all tasks have completed
		} catch (InterruptedException | ExecutionException ignored) {}
	}

	/**
	 * Execute Next Level
	 */
	private void executeNextNodes(SpiderNode node, SpiderContext context, Map<String, Object> variables) {
		List<SpiderNode> nextNodes = node.getNextNodes();
		if (nextNodes != null) {
			for (SpiderNode nextNode : nextNodes) {
				executeNode(node, nextNode, context, variables);
			}
		}
	}

	/**
	 * 执行节点
	 */
	public void executeNode(SpiderNode fromNode, SpiderNode node, SpiderContext context, Map<String, Object> variables) {
		String shape = node.getStringJsonValue("shape");
		if (StringUtils.isBlank(shape)) {
			executeNextNodes(node, context, variables);
			return;
		}
		//Determine conditions for the arrow shot，If true, show the banner message
		if (!executeCondition(fromNode, node, variables, context)) {
			return;
		}
		logger.debug("执行节点[{}:{}]", node.getNodeName(), node.getNodeId());
		//Find matching device
		ShapeExecutor executor = ExecutorsUtils.get(shape);
		if (executor == null) {
			logger.error("Failed to execute,Find an iPod failed:{}", shape);
			context.setRunning(false);
		}
		int loopCount = 1;	//Default Number of Cycles1,If the node has a cycle property and a cycle count/Conversation,Remove the selected card from the deck
		int loopStart = 0;	//Start counter at:
		int loopEnd = 1;	//End of cycle position
		String loopCountStr = node.getStringJsonValue(ShapeExecutor.LOOP_COUNT);
		Object loopArray = null;
		boolean isLoop = false;
		if (isLoop = StringUtils.isNotBlank(loopCountStr)) {
			try {
				loopArray = ExpressionUtils.execute(loopCountStr, variables);
				if(loopArray == null){
					loopCount = 0;
				}else if(loopArray instanceof Collection){
					loopCount = ((Collection)loopArray).size();
					loopArray = ((Collection)loopArray).toArray();
				}else if(loopArray.getClass().isArray()){
					loopCount = Array.getLength(loopArray);
				}else{
					loopCount = NumberUtils.toInt(loopArray.toString(),0);
					loopArray = null;
				}
				loopEnd = loopCount;
				if(loopCount > 0){
					loopStart = Math.max(NumberUtils.toInt(node.getStringJsonValue(LoopExecutor.LOOP_START), 0),0);
					int end = NumberUtils.toInt(node.getStringJsonValue(LoopExecutor.LOOP_END), -1);
					if(end >=0){
						loopEnd = Math.min(end,loopEnd);
					}else{
						loopEnd = Math.max(loopEnd + end + 1,0);
					}
				}
				logger.info("Get the number of cycles{}={}", loopCountStr, loopCount);
			} catch (Throwable t) {
				loopCount = 0;
				logger.error("Failed to get loop count,Anomalous messages：{}", t);
			}
		}
		if (loopCount > 0) {
			//Get the value of a cycle counter
			String loopVariableName = node.getStringJsonValue(ShapeExecutor.LOOP_VARIABLE_NAME);
			String loopItem = node.getStringJsonValue(LoopExecutor.LOOP_ITEM,"item");
			List<SpiderTask> tasks = new ArrayList<>();
			for (int i = loopStart; i < loopEnd; i++) {
				node.increment();	//Number of times to execute the node+1(Follow UpJoinUse a countdown)
				if (context.isRunning()) {
					Map<String, Object> nVariables = new HashMap<>();
					// Determine if a variable should be transmitted
					if(fromNode == null || node.isTransmitVariable(fromNode.getNodeId())){
						nVariables.putAll(variables);
					}
					if(isLoop){
						// Save subvariable
						if (!StringUtils.isBlank(loopVariableName)) {
							nVariables.put(loopVariableName, i);
						}
						// Saveitem
						nVariables.put(loopItem,loopArray == null ? i : Array.get(loopArray, i));
					}
					tasks.add(new SpiderTask(TtlRunnable.get(() -> {
						if (context.isRunning()) {
							try {
								//Text to translate: D-Bus Call，When to execute the child instead of the parent，Thank you for taking this survey.
								AtomicInteger executeCount = context.get(ATOMIC_DEAD_CYCLE);
								if (executeCount != null && executeCount.incrementAndGet() > deadCycle) {
									context.setRunning(false);
									return;
								}
								//Execute specific logic
								executor.execute(node, context, nVariables);
								//When no exception occurs，Removeex变量
								nVariables.remove("ex");
							} catch (Throwable t) {
								nVariables.put("ex", t);
								logger.error("执行节点[{}:{}]Error,Anomalous messages：{}", node.getNodeName(), node.getNodeId(), t);
							}
						}
					}), node, nVariables, executor));
				}
			}
			LinkedBlockingQueue<Future<?>> futureQueue = context.getFutureQueue();
			for (SpiderTask task : tasks) {
				if(executor.isThread()){	//Is the node running in async mode?
					//Submit Task to Queue,andFutureAdd to the end of the queue
					futureQueue.add(context.getThreadPool().submitAsync(task.runnable, task, node));
				}else{
					FutureTask<SpiderTask> futureTask = new FutureTask<>(task.runnable, task);
					futureTask.run();
					futureQueue.add(futureTask);
				}
			}
		}
	}

	/**
	 *	If you don't know the answer to a question, please don't share false information.
	 */
	private boolean executeCondition(SpiderNode fromNode, SpiderNode node, Map<String, Object> variables, SpiderContext context) {
		if (fromNode != null) {
			boolean hasException = variables.get("ex") != null;
			String exceptionFlow = node.getExceptionFlow(fromNode.getNodeId());
			//When an abnormal flow occurs : 1
			//未出现异常流转 : 2
			if(("1".equalsIgnoreCase(exceptionFlow) && !hasException) || ("2".equalsIgnoreCase(exceptionFlow) && hasException)){
				return false;
			}
			String condition = node.getCondition(fromNode.getNodeId());
			if (StringUtils.isNotBlank(condition)) { // If a question does not make any sense, or is not factually coherent, explain why instead of answering something not correct. If you don't know the answer to a question, please don't share false information.
				Object result = null;
				try {
					result = ExpressionUtils.execute(condition, variables);
				} catch (Exception e) {
					logger.error("判断{}Error,Anomalous messages：{}", condition, e);
				}
				if (result != null) {
					boolean isContinue = "true".equals(result) || Objects.equals(result, true);
					logger.debug("判断{}={}", condition, isContinue);
					return isContinue;
				}
				return false;
			}
		}
		return true;
	}

	class SpiderTask{

		Runnable runnable;

		SpiderNode node;

		Map<String,Object> variables;

		ShapeExecutor executor;

		public SpiderTask(Runnable runnable, SpiderNode node, Map<String, Object> variables,ShapeExecutor executor) {
			this.runnable = runnable;
			this.node = node;
			this.variables = variables;
			this.executor = executor;
		}
	}
}
