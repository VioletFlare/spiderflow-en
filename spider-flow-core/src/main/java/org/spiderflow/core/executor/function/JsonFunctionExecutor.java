package org.spiderflow.core.executor.function;

import org.spiderflow.annotation.Comment;
import org.spiderflow.annotation.Example;
import org.spiderflow.executor.FunctionExecutor;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

/**
 * Json和String Configure KSpread... Class Instance PreventNPE 
 * @author Administrator
 *
 */
@Component
@Comment("jsonCommon Methods")
public class JsonFunctionExecutor implements FunctionExecutor{
	
	@Override
	public String getFunctionPrefix() {
		return "json";
	}

	@Comment("Turn string intojson对象")
	@Example("${json.parse('{code : 1}')}")
	public static Object parse(String jsonString){
		return jsonString != null ? JSON.parse(jsonString) : null;
	}
	
	@Comment("Object TojsonString")
	@Example("${json.stringify(objVar)}")
	public static String stringify(Object object){
		return object != null ? JSON.toJSONString(object) : null;
	}
}
