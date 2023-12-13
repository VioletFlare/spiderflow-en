package org.spiderflow.core.executor.function;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.spiderflow.annotation.Comment;
import org.spiderflow.annotation.Example;
import org.spiderflow.executor.FunctionExecutor;
import org.springframework.stereotype.Component;

/**
 * List Class Instance PreventNPE Added similarpython的split()Method 
 * @author Administrator
 *
 */
@Component
@Comment("listCommon Methods")
public class ListFunctionExecutor implements FunctionExecutor{
	
	@Override
	public String getFunctionPrefix() {
		return "list";
	}

	@Comment("GetlistThe length of the text")
	@Example("${list.length(listVar)}")
	public static int length(List<?> list){
		return list != null ? list.size() : 0;
	}
	
	/**
	 * 
	 * @param list 原List
	 * @param len How many pieces go into the split
	 * @return List<List<?>> Shared Music
	 */
	@Comment("分割List")
	@Example("${list.split(listVar,10)}")
	public static List<List<?>> split(List<?> list,int len){
		List<List<?>> result = new ArrayList<>();
		if (list == null || list.size() == 0 || len < 1) {
			return result;
		}
		int size = list.size();
		int count = (size + len - 1) / len;
		for (int i = 0; i < count; i++) {
			List<?> subList = list.subList(i * len, ((i + 1) * len > size ? size : len * (i + 1)));
			result.add(subList);
		}
		return result;
	}
	
	@Comment("InterceptList")
	@Example("${list.sublist(listVar,fromIndex,toIndex)}")
	public static List<?> sublist(List<?> list,int fromIndex,int toIndex){
		return list!= null ? list.subList(fromIndex, toIndex) : new ArrayList<>();
	}

	@Comment("Filter Stringslist元素")
	@Example("${listVar.filterStr(pattern)}")
	public static List<String> filterStr(List<String> list, String pattern) {
		if (list == null || list.isEmpty()) {
			return null;
		}
		List<String> result = new ArrayList<>(list.size());
		for (String item : list) {
			if (Pattern.matches(pattern, item)) {
				result.add(item);
			}
		}
		return result;
	}
		
}
