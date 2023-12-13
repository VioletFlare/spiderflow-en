package org.spiderflow.concurrent;

import org.spiderflow.model.SpiderNode;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SpiderFlowThreadPoolExecutor {

	/**
	 * Maximum number of threads
	 */
	private int maxThreads;

	/**
	 * Threads
	 */
	private ThreadPoolExecutor executor;

	/**
	 * ThreadnumberCounter
	 */
	private final AtomicInteger poolNumber = new AtomicInteger(1);

	/**
	 * ThreadGroup
	 */
	private static final ThreadGroup SPIDER_FLOW_THREAD_GROUP = new ThreadGroup("spider-flow-group");

	/**
	 * Thread name prefix
	 */
	private static final String THREAD_POOL_NAME_PREFIX = "spider-flow-";

	public SpiderFlowThreadPoolExecutor(int maxThreads) {
		super();
		this.maxThreads = maxThreads;
		//Thread pool instance
		this.executor = new ThreadPoolExecutor(maxThreads, maxThreads, 10, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), runnable -> {
			//Rewrite the thread name
			return new Thread(SPIDER_FLOW_THREAD_GROUP, runnable, THREAD_POOL_NAME_PREFIX + poolNumber.getAndIncrement());
		});
	}

	public Future<?> submit(Runnable runnable){
		return this.executor.submit(runnable);
	}


	/**
	 * Create child thread pool
	 * @param threads	Thread pool size
	 * @return
	 */
	public SubThreadPoolExecutor createSubThreadPoolExecutor(int threads,ThreadSubmitStrategy submitStrategy){
		return new SubThreadPoolExecutor(Math.min(maxThreads, threads),submitStrategy);
	}

	/**
	 * Threads
	 */
	public class SubThreadPoolExecutor{

		/**
		 * Thread pool size
		 */
		private int threads;

		/**
		 * In progress
		 */
		private Future<?>[] futures;

		/**
		 * Quantity in execution
		 */
		private AtomicInteger executing = new AtomicInteger(0);

		/**
		 * Are you running
		 */
		private volatile boolean running = true;

		/**
		 * Are you submitting a task
		 */
		private volatile boolean submitting = false;

		private ThreadSubmitStrategy submitStrategy;

		public SubThreadPoolExecutor(int threads,ThreadSubmitStrategy submitStrategy) {
			super();
			this.threads = threads;
			this.futures = new Future[threads];
			this.submitStrategy = submitStrategy;
		}
		
		/**
		 * Wait for all threads to finish
		 */
		public void awaitTermination(){
			while(executing.get() > 0){
				removeDoneFuture();
			}
			running = false;
			//When finished,Wake up the submitter to make it quit
			synchronized (submitStrategy){
				submitStrategy.notifyAll();
			}
		}
		
		private int index(){
			for (int i = 0; i < threads; i++) {
				if(futures[i] == null || futures[i].isDone()){
					return i;
				}
			}
			return -1;
		}

		/**
		 * Delete completed tasks
		 */
		private void removeDoneFuture(){
			for (int i = 0; i < threads; i++) {
				try {
					if(futures[i] != null && futures[i].get(10,TimeUnit.MILLISECONDS) == null){
						futures[i] = null;
					}
				} catch (Throwable t) {
					//Ignore exception
				} 
			}
		}

		/**
		 * Wait for a free thread
		 */
		private void await(){
			while(index() == -1){
				removeDoneFuture();
			}
		}

		/**
		 * Special thanks to:
		 */
		public <T> Future<T> submitAsync(Runnable runnable, T value, SpiderNode node){
			SpiderFutureTask<T> future = new SpiderFutureTask<>(()-> {
				try {
					//执行任务
					runnable.run();
				} finally {
					//Threads currently executing-1
					executing.decrementAndGet();
				}
			}, value,node,this);

			submitStrategy.add(future);
			//If it is the first time you callsubmitSyncMethod，Submit a bug report using Bug Buddy
			if(!submitting){
				submitting = true;
				CompletableFuture.runAsync(this::submit);
			}
			synchronized (submitStrategy){
				//Notify that the task has been taken from the pool and submitted to the collector
				submitStrategy.notifyAll();

			}
			return future;
		}

		private void submit(){
			while(running){
				try {
					synchronized (submitStrategy){
						//If the collection is empty，Please wait while submitting
						if(submitStrategy.isEmpty()){
							submitStrategy.wait();	//Please wait
						}
					}
					//When this thread is woken，Submit all tasks in the queue to the thread pool
					while(!submitStrategy.isEmpty()){
						//Submit to thread pool from submit strategy
						SpiderFutureTask<?> futureTask = submitStrategy.get();
						//If there are no free threads and the queue is submitted，Then run directly
						if(index() == -1 && Thread.currentThread().getThreadGroup() == SPIDER_FLOW_THREAD_GROUP){
							futureTask.run();
						}else{
							//Submit when a free thread is available
							await();
							//Submit Task to Queue
							futures[index()] = executor.submit(futureTask);
						}
					}
				} catch (InterruptedException ignored) {
				}
			}
		}
	}
}
