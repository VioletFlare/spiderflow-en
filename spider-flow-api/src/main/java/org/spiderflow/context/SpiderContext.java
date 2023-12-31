package org.spiderflow.context;

import org.spiderflow.concurrent.SpiderFlowThreadPoolExecutor.SubThreadPoolExecutor;
import org.spiderflow.model.SpiderNode;
import org.spiderflow.model.SpiderOutput;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Crawling context
 * @author jmxd
 *
 */
public class SpiderContext extends HashMap<String, Object>{
	
	private String id = UUID.randomUUID().toString().replace("-", "");

	/**
	 * 流程ID
	 */
	private String flowId;
	
	private static final long serialVersionUID = 8379177178417619790L;

	/**
	 * Procedure execution thread
	 */
	private SubThreadPoolExecutor threadPool;

	/**
	 * 根节点
	 */
	private SpiderNode rootNode;

	/**
	 * Is the Crawler running
	 */
	private volatile boolean running = true;

	/**
	 * FutureQueue
	 */
	private LinkedBlockingQueue<Future<?>> futureQueue = new LinkedBlockingQueue<>();

	/**
	 * CookieContexte
	 */
	private CookieContext cookieContext = new CookieContext();

	public List<SpiderOutput> getOutputs() {
		return Collections.emptyList();
	}

	public <T> T get(String key){
		return (T) super.get(key);
	}

	public <T> T get(String key,T defaultValue){
		T value = this.get(key);
		return value == null ? defaultValue : value;
	}

	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	public LinkedBlockingQueue<Future<?>> getFutureQueue() {
		return futureQueue;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public void addOutput(SpiderOutput output){

	}

	public SubThreadPoolExecutor getThreadPool() {
		return threadPool;
	}

	public void setThreadPool(SubThreadPoolExecutor threadPool) {
		this.threadPool = threadPool;
	}

	public SpiderNode getRootNode() {
		return rootNode;
	}

	public void setRootNode(SpiderNode rootNode) {
		this.rootNode = rootNode;
	}
	
	public String getId() {
		return id;
	}
	
	public CookieContext getCookieContext() {
		return cookieContext;
	}

	public void pause(String nodeId,String event,String key,Object value){}

	public void resume(){}

	public void stop(){}

}
