package org.spiderflow.listener;

import org.spiderflow.context.SpiderContext;

public interface SpiderListener {

	/**
	 * Before we get started,
	 */
	void beforeStart(SpiderContext context);
	
	/**
	 * After you have finished
	 */
	void afterEnd(SpiderContext context);
	
}
