package org.spiderflow.core.executor.function;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.spiderflow.annotation.Comment;
import org.spiderflow.annotation.Example;
import org.spiderflow.executor.FunctionExecutor;
import org.springframework.stereotype.Component;

/**
 * url Character set to use in the encoding/Decrypt Default charset(UTF-8) Class Instance PreventNPE 
 */
@Component
public class UrlFunctionExecutor implements FunctionExecutor{
	
	@Override
	public String getFunctionPrefix() {
		return "url";
	}
	
	@Comment("GeturlParameters")
	@Example("${url.parameter('http://www.baidu.com/s?wd=spider-flow','wd')}")
	public static String parameter(String url,String key){
		return parameterMap(url).get(key);
	}
	
	@Comment("GeturlAll parameters")
	@Example("${url.parameterMap('http://www.baidu.com/s?wd=spider-flow&abbr=sf')}")
	public static Map<String,String> parameterMap(String url){
		Map<String,String> map = new HashMap<String,String>();
		int index = url.indexOf("?");
		if(index != -1) {
	        String param = url.substring(index+1);
	        if(StringUtils.isNotBlank(param)) {
		        String[] params = param.split("&");
		        for (String item : params) {
		            String[] kv = item.split("=");
		            if(kv.length > 0) {
		            	if(StringUtils.isNotBlank(kv[0])) {
		            		String value = "";
		            		if(StringUtils.isNotBlank(kv[1])) {
		            			int kv1Index = kv[1].indexOf("#");
		            			if(kv1Index != -1) {
		            				value = kv[1].substring(0,kv1Index);
		            			}else {
		            				value = kv[1];
		            			}
		            		}
		            		map.put(kv[0],value);
		            	}
		            }
		        }
	        }
		}
		return map;
	}
	
	@Comment("urlCode")
	@Example("${url.encode('http://www.baidu.com/s?wd=spider-flow')}")
	public static String encode(String url){
		return encode(url,Charset.defaultCharset().name());
	}
	
	@Comment("urlCode")
	@Example("${url.encode('http://www.baidu.com/s?wd=spider-flow','UTF-8')}")
	public static String encode(String url,String charset){
		try {
			return url != null ? URLEncoder.encode(url,charset) : null;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	@Comment("urlDecrypt")
	@Example("${url.decode(strVar)}")
	public static String decode(String url){
		return decode(url,Charset.defaultCharset().name());
	}
	
	@Comment("urlDecrypt")
	@Example("${url.decode(strVar,'UTF-8')}")
	public static String decode(String url,String charset){
		try {
			return url != null ? URLDecoder.decode(url, charset) : null;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
}
