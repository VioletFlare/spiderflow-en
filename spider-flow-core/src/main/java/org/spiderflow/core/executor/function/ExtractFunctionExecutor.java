package org.spiderflow.core.executor.function;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.spiderflow.annotation.Comment;
import org.spiderflow.annotation.Example;
import org.spiderflow.core.utils.ExtractUtils;
import org.spiderflow.executor.FunctionExecutor;
import org.springframework.stereotype.Component;

@Component
@Comment("Data extraction - Frequently asked questions")
public class ExtractFunctionExecutor implements FunctionExecutor{

	@Override
	public String getFunctionPrefix() {
		return "extract";
	}
	
	@Comment("Based onjsonpathExtract Contents")
	@Example("${extract.jsonpath(resp.json,'$.code')}")
	public static Object jsonpath(Object root,String jsonpath){
		return ExtractUtils.getValueByJsonPath(root, jsonpath);
	}
	
	@Comment("Extract based on regular expression")
	@Example("${extract.regx(resp.html,'<title>(.*?)</title>')}")
	public static String regx(String content,String pattern){
		return ExtractUtils.getFirstMatcher(content, pattern, true);
	}
	
	@Comment("Extract based on regular expression")
	@Example("${extract.regx(resp.html,'<title>(.*?)</title>',1)}")
	public static String regx(String content,String pattern,int groupIndex){
		return ExtractUtils.getFirstMatcher(content, pattern, groupIndex);
	}
	
	@Comment("Extract based on regular expression")
	@Example("${extract.regx(resp.html,'<a href=\"(.*?)\">(.*?)</a>',[1,2])}")
	public static List<String> regx(String content,String pattern,List<Integer> groups){
		return ExtractUtils.getFirstMatcher(content, pattern, groups);
	}
	
	@Comment("Extract based on regular expression")
	@Example("${extract.regxs(resp.html,'<h2>(.*?)</h2>')}")
	public static List<String> regxs(String content,String pattern){
		return ExtractUtils.getMatchers(content, pattern, true);
	}
	
	@Comment("Extract based on regular expression")
	@Example("${extract.regxs(resp.html,'<h2>(.*?)</h2>',1)}")
	public static List<String> regxs(String content,String pattern,int groupIndex){
		return ExtractUtils.getMatchers(content, pattern, groupIndex);
	}
	
	@Comment("Extract based on regular expression")
	@Example("${extract.regxs(resp.html,'<a href=\"(.*?)\">(.*?)</a>',[1,2])}")
	public static List<List<String>> regxs(String content,String pattern,List<Integer> groups){
		return ExtractUtils.getMatchers(content, pattern, groups);
	}
	
	@Comment("Based onxpathExtract Contents")
	@Example("${extract.xpath(resp.element(),'//title/text()')}")
	public static String xpath(Element element,String xpath){
		return ExtractUtils.getValueByXPath(element, xpath);
	}
	
	@Comment("Based onxpathExtract Contents")
	@Example("${extract.xpath(resp.html,'//title/text()')}")
	public static String xpath(String content,String xpath){
		return xpath(Jsoup.parse(content),xpath);
	}
	
	@Comment("Based onxpathsExtract Contents")
	@Example("${extract.xpaths(resp.element(),'//h2/text()')}")
	public static List<String> xpaths(Element element,String xpath){
		return ExtractUtils.getValuesByXPath(element, xpath);
	}
	
	@Comment("Based onxpathsExtract Contents")
	@Example("${extract.xpaths(resp.html,'//h2/text()')}")
	public static List<String> xpaths(String content,String xpath){
		return xpaths(Jsoup.parse(content),xpath);
	}
	
	@Comment("Based oncssSelector Extraction")
	@Example("${extract.selectors(resp.html,'div > a')}")
	public static List<String> selectors(Object object,String selector){
		return ExtractUtils.getHTMLBySelector(getElement(object), selector);
	}
	
	@Comment("Based oncssSelector Extraction")
	@Example("${extract.selector(resp.html,'div > a','text')}")
	public static Object selector(Object object,String selector,String type){
		if("element".equals(type)){
			return ExtractUtils.getFirstElement(getElement(object), selector);
		}else if("text".equals(type)){
			return ExtractUtils.getFirstTextBySelector(getElement(object), selector);
		}else if("outerhtml".equals(type)){
			return ExtractUtils.getFirstOuterHTMLBySelector(getElement(object), selector);
		}
		return null;
	}
	
	@Comment("Based oncssSelector Extraction")
	@Example("${extract.selector(resp.html,'div > a','attr','href')}")
	public static String selector(Object object,String selector,String type,String attrValue){
		if("attr".equals(type)){
			return ExtractUtils.getFirstAttrBySelector(getElement(object), selector,attrValue);
		}
		return null;
	}
	
	@Comment("Based oncssSelector Extraction")
	@Example("${extract.selector(resp.html,'div > a')}")
	public static String selector(Object object,String selector){
		return ExtractUtils.getFirstHTMLBySelector(getElement(object), selector);
	}
	
	@Comment("Based oncssSelector Extraction")
	@Example("${extract.selectors(resp.html,'div > a','element')}")
	public static Object selectors(Object object,String selector,String type){
		if("element".equals(type)){
			return ExtractUtils.getElements(getElement(object), selector);
		}else if("text".equals(type)){
			return ExtractUtils.getTextBySelector(getElement(object), selector);
		}else if("outerhtml".equals(type)){
			return ExtractUtils.getOuterHTMLBySelector(getElement(object), selector);
		}
		return null;
	}
	
	@Comment("Based oncssSelector Extraction")
	@Example("${extract.selectors(resp.html,'div > a','attr','href')}")
	public static Object selectors(Object object,String selector,String type,String attrValue){
		if("attr".equals(type)){
			return ExtractUtils.getAttrBySelector(getElement(object), selector,attrValue);
		}
		return null;
	}
	
	private static Element getElement(Object object){
		if(object != null){
			return object instanceof Element ? (Element)object:Jsoup.parse((String) object);
		}
		return null;
	}
}
