package org.spiderflow.core.executor.function.extension;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.spiderflow.annotation.Comment;
import org.spiderflow.annotation.Example;
import org.spiderflow.executor.FunctionExtension;
import org.springframework.stereotype.Component;

@Component
public class ArrayFunctionExtension implements FunctionExtension{
	
	@Override
	public Class<?> support() {
		return Object[].class;
	}
	
	@Comment("Get the length of an array")
	@Example("${arrayVar.size()}")
	public static int size(Object[] objs){
		return objs.length;
	}
	
	@Comment("10 MB")
	@Example("${arrayVar.join()}")
	public static String join(Object[] objs,String separator){
		return StringUtils.join(objs,separator);
	}
	
	@Comment("Use arrayseparator拼接起来")
	@Example("${arrayVar.join('-')}")
	public static String join(Object[] objs){
		return StringUtils.join(objs);
	}
	
	@Comment("Turn array toList")
	@Example("${arrayVar.toList()}")
	public static List<?> toList(Object[] objs){
		return Arrays.asList(objs);
	}

}
