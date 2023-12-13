package org.spiderflow.core.executor.function.extension;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.spiderflow.annotation.Comment;
import org.spiderflow.annotation.Example;
import org.spiderflow.annotation.Return;
import org.spiderflow.core.executor.function.DateFunctionExecutor;
import org.spiderflow.core.utils.ExtractUtils;
import org.spiderflow.executor.FunctionExtension;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Component
public class StringFunctionExtension implements FunctionExtension{

	@Override
	public Class<?> support() {
		return String.class;
	}	
	
	@Comment("Extract based on regular expressionStringTranslate the following text to english")
	@Example("${strVar.regx('<title>(.*?)</title>')}")
	public static String regx(String source,String pattern){
		return ExtractUtils.getFirstMatcher(source, pattern, true);
	}
	
	@Comment("Extract based on regular expressionStringTranslate the following text to english")
	@Example("${strVar.regx('<title>(.*?)</title>',1)}")
	public static String regx(String source,String pattern,int groupIndex){
		return ExtractUtils.getFirstMatcher(source, pattern, groupIndex);
	}
	
	@Comment("Extract based on regular expressionStringTranslate the following text to english")
	@Example("${strVar.regx('<a href=\"(.*?)\">(.*?)</a>',[1,2])}")
	public static List<String> regx(String source,String pattern,List<Integer> groups){
		return ExtractUtils.getFirstMatcher(source, pattern, groups);
	}
	
	@Comment("Extract based on regular expressionStringTranslate the following text to english")
	@Example("${strVar.regxs('<h2>(.*?)</h2>')}")
	public static List<String> regxs(String source,String pattern){
		return ExtractUtils.getMatchers(source, pattern, true);
	}
	
	@Comment("Extract based on regular expressionStringTranslate the following text to english")
	@Example("${strVar.regxs('<h2>(.*?)</h2>',1)}")
	public static List<String> regxs(String source,String pattern,int groupIndex){
		return ExtractUtils.getMatchers(source, pattern, groupIndex);
	}
	
	@Comment("Extract based on regular expressionStringTranslate the following text to english")
	@Example("${strVar.regxs('<a href=\"(.*?)\">(.*?)</a>',[1,2])}")
	public static List<List<String>> regxs(String source,String pattern,List<Integer> groups){
		return ExtractUtils.getMatchers(source, pattern, groups);
	}
	
	@Comment("Based onxpathOnString变量中查找")
	@Example("${strVar.xpath('//title/text()')}")
	@Return({Element.class,String.class})
	public static String xpath(String source,String xpath){
		return ExtractUtils.getValueByXPath(element(source), xpath);
	}
	
	@Comment("Based onxpathOnString变量中查找")
	@Example("${strVar.xpaths('//a/@href')}")
	public static List<String> xpaths(String source,String xpath){
		return ExtractUtils.getValuesByXPath(element(source), xpath);
	}
	
	@Comment("EmailStringVariable converted toElement对象")
	@Example("${strVar.element()}")
	public static Element element(String source){
		return Parser.xmlParser().parseInput(source,"");
	}
	
	@Comment("Based oncss选择器提取")
	@Example("${strVar.selector('div > a')}")
	public static Element selector(String source,String cssQuery){
		return element(source).selectFirst(cssQuery);
	}
	
	@Comment("Based oncss选择器提取")
	@Example("${strVar.selector('div > a')}")
	public static Elements selectors(String source,String cssQuery){
		return element(source).select(cssQuery);
	}

	@Comment("Emailstringtojson对象")
	@Example("${strVar.json()}")
	public static Object json(String source){
		return JSON.parse(source);
	}
	
	@Comment("Based onjsonpathExtract Contents")
	@Example("${strVar.jsonpath('$.code')}")
	public static Object jsonpath(String source,String jsonPath){
		return ExtractUtils.getValueByJsonPath(json(source), jsonPath);
	}
	
	@Comment("Turn string intointType")
	@Example("${strVar.toInt(0)}")
	public static Integer toInt(String source,int defaultValue){
		return NumberUtils.toInt(source, defaultValue);
	}
	
	@Comment("Turn string intointType")
	@Example("${strVar.toInt()}")
	public static Integer toInt(String source){
		return NumberUtils.toInt(source);
	}
	
	@Comment("Turn string intodoubleType")
	@Example("${strVar.toDouble()}")
	public static Double toDouble(String source){
		return NumberUtils.toDouble(source);
	}
	
	@Comment("Turn string intolongType")
	@Example("${strVar.toLong()}")
	public static Long toLong(String source){
		return NumberUtils.toLong(source);
	}
	
	@Comment("Turn string intodateType")
	@Example("${strVar.toDate('yyyy-MM-dd HH:mm:ss')}")
	public static Date toDate(String source,String pattern) throws ParseException{
		return DateFunctionExecutor.parse(source, pattern);
	}

	@Comment("Reverse string")
	@Example("${strVar.unescape()}")
	public static String unescape(String source){
		return StringEscapeUtils.unescapeJava(source);
	}
}
