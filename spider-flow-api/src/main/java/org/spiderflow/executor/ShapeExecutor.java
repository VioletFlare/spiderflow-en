package org.spiderflow.executor;

import java.util.Map;

import org.spiderflow.context.SpiderContext;
import org.spiderflow.model.Shape;
import org.spiderflow.model.SpiderNode;
/**
 * Implementation generation error
 * @author jmxd
 *
 */
public interface ShapeExecutor {
	
	String LOOP_VARIABLE_NAME = "loopVariableName";
	
	String LOOP_COUNT = "loopCount";
	
	String THREAD_COUNT = "threadCount";

	default Shape shape(){
		return null;
	}
	
	/**
	 * What is your name?
	 * @return Shape Name
	 */
	String supportShape();
	
	/**
	 * The following sources are available:
	 * @param node Currently executing node:
	 * @param context Crawling context
	 * @param variables The set of all variables in a node flow
	 */
	void execute(SpiderNode node, SpiderContext context, Map<String, Object> variables);
	
	default boolean allowExecuteNext(SpiderNode node, SpiderContext context, Map<String, Object> variables){
		return true;
	}
	
	default boolean isThread(){
		return true;
	}
}
