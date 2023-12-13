package org.spiderflow.io;

import java.io.InputStream;
import java.util.Map;

import org.spiderflow.annotation.Comment;
import org.spiderflow.annotation.Example;

import com.alibaba.fastjson.JSON;

public interface SpiderResponse {

	@Comment("Get the return value")
	@Example("${resp.statusCode}")
	int getStatusCode();

	@Comment("Get web page title")
	@Example("${resp.title}")
	String getTitle();

	@Comment("Get Web Pagehtml")
	@Example("${resp.html}")
	String getHtml();

	@Comment("Getjson")
	@Example("${resp.json}")
	default Object getJson(){
		return JSON.parse(getHtml());
	}
	@Comment("Getcookies")
	@Example("${resp.cookies}")
	Map<String,String> getCookies();

	@Comment("Getheaders")
	@Example("${resp.headers}")
	Map<String,String> getHeaders();

	@Comment("Getbyte[]")
	@Example("${resp.bytes}")
	byte[] getBytes();

	@Comment("GetContentType")
	@Example("${resp.contentType}")
	String getContentType();

	@Comment("Get currenturl")
	@Example("${resp.url}")
	String getUrl();

	@Example("${resp.setCharset('UTF-8')}")
	default void setCharset(String charset){

	}
	@Example("${resp.stream}")
	default InputStream getStream(){
		return null;
	}
}
