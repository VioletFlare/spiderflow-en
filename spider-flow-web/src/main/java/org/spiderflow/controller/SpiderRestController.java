package org.spiderflow.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spiderflow.context.SpiderContext;
import org.spiderflow.core.Spider;
import org.spiderflow.core.job.SpiderJob;
import org.spiderflow.core.job.SpiderJobContext;
import org.spiderflow.core.model.SpiderFlow;
import org.spiderflow.core.model.Task;
import org.spiderflow.core.service.SpiderFlowService;
import org.spiderflow.core.service.TaskService;
import org.spiderflow.model.JsonBean;
import org.spiderflow.model.SpiderOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rest")
public class SpiderRestController {
	
	private static Logger logger = LoggerFactory.getLogger(SpiderRestController.class);
	
	@Autowired
	private SpiderFlowService spiderFlowService;
	
	@Autowired
	private Spider spider;
	
	@Value("${spider.workspace}")
	private String workspace;

	@Autowired
	private SpiderJob spiderJob;

	@Autowired
	private TaskService taskService;

	/**
	 * SyncInfo
	 * @param id
	 * @return
	 */
	@RequestMapping("/runAsync/{id}")
	public JsonBean<Integer> runAsync(@PathVariable("id")String id){
		SpiderFlow flow = spiderFlowService.getById(id);
		if(flow == null){
			return new JsonBean<>(0, "Cannot find this article");
		}
		Task task = new Task();
		task.setFlowId(flow.getId());
		task.setBeginTime(new Date());
		taskService.save(task);
		Spider.executorInstance.submit(()->{
			spiderJob.run(flow,task,null);
		});
		return new JsonBean<>(task.getId());
	}

	/**
	 * Stop Operation
	 * @param taskId
	 */
	@RequestMapping("/stop/{taskId}")
	public JsonBean<Void> stop(@PathVariable("taskId")Integer taskId){
		SpiderContext context = SpiderJob.getSpiderContext(taskId);
		if(context == null){
			return new JsonBean<>(0,"Tasks do not exist！");
		}
		context.setRunning(false);
		return new JsonBean<>(1,"Stop Successful！");

	}

	/**
	 * Query task status
	 * @param taskId
	 */
	@RequestMapping("/status/{taskId}")
	public JsonBean<Integer> status(@PathVariable("taskId")Integer taskId){
		SpiderContext context = SpiderJob.getSpiderContext(taskId);
		if(context == null){
			return new JsonBean<>(0);	//
		}
		return new JsonBean<>(1);	//In progress
	}

	/**
	 * Simulate the application's behavior by showing the assistant dialogs on a separate window.
	 * @param id
	 * @param params
	 * @return
	 */
	@RequestMapping("/run/{id}")
	public JsonBean<List<SpiderOutput>> run(@PathVariable("id")String id,@RequestBody(required = false)Map<String,Object> params){
		SpiderFlow flow = spiderFlowService.getById(id);
		if(flow == null){
			return new JsonBean<>(0, "Cannot find this article");
		}
		List<SpiderOutput> outputs;
		Integer maxId = spiderFlowService.getFlowMaxTaskId(id);
		SpiderJobContext context = SpiderJobContext.create(workspace, id,maxId,true);
		try{
			outputs = spider.run(flow,context, params);	
		}catch(Exception e){
			logger.error("Failed to execute child process",e);
			return new JsonBean<>(-1, "Failed to execute");
		} finally{
			context.close();
		}
		return new JsonBean<>(outputs);
	}
	
}
