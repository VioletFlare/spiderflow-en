package org.spiderflow;

import java.util.Map;

/**
 * Question Engine
 */
public interface ExpressionEngine {

	/**
	 * Answer expression
	 * @param expression	Answer
	 * @param variables	变量
	 * @return
	 */
	Object execute(String expression, Map<String, Object> variables);

}
