package org.spiderflow.core.executor.function;

import org.apache.commons.codec.binary.Base64;
import org.spiderflow.annotation.Comment;
import org.spiderflow.annotation.Example;
import org.spiderflow.executor.FunctionExecutor;
import org.springframework.stereotype.Component;

/**
 * String content andBase64 Configure KSpread... Class Instance PreventNPE
 * @author Administrator
 *
 */
@Component
@Comment("base64Common Methods")
public class Base64FunctionExecutor implements FunctionExecutor{
	
	@Override
	public String getFunctionPrefix() {
		return "base64";
	}
	
	@Comment("Based onbyte[]Conductbase64Encryption")
	@Example("${base64.encode(resp.bytes)}")
	public static String encode(byte[] bytes){
		return bytes != null ? Base64.encodeBase64String(bytes) : null;
	}
	
	@Comment("Based onStringConductbase64Encryption")
	@Example("${base64.encode(resp.bytes,'UTF-8')}")
	public static String encode(String content,String charset){
		return encode(StringFunctionExecutor.bytes(content,charset));
	}
	
	@Comment("Based onStringConductbase64Encryption")
	@Example("${base64.encode(resp.html)}")
	public static String encode(String content){
		return encode(StringFunctionExecutor.bytes(content));
	}
	
	@Comment("Based onbyte[]Conductbase64Encryption")
	@Example("${base64.encodeBytes(resp.bytes)}")
	public static byte[] encodeBytes(byte[] bytes){
		return bytes != null ? Base64.encodeBase64(bytes) : null;
	}
	
	@Comment("Based onStringConductbase64Encryption")
	@Example("${base64.encodeBytes(resp.html,'UTF-8')}")
	public static byte[] encodeBytes(String content,String charset){
		return encodeBytes(StringFunctionExecutor.bytes(content,charset));
	}
	
	@Comment("Based onStringConductbase64Encryption")
	@Example("${base64.encodeBytes(resp.html)}")
	public static byte[] encodeBytes(String content){
		return encodeBytes(StringFunctionExecutor.bytes(content));
	}
	
	@Comment("Based onStringConductbase64Decrypt")
	@Example("${base64.decode(resp.html)}")
	public static byte[] decode(String base64){
		return base64  != null ? Base64.decodeBase64(base64) :null;
	}
	
	@Comment("Based onbyte[]Conductbase64Decrypt")
	@Example("${base64.decode(resp.bytes)}")
	public static byte[] decode(byte[] base64){
		return base64  != null ? Base64.decodeBase64(base64) :null;
	}
	
	@Comment("Based onStringConductbase64Decrypt")
	@Example("${base64.decodeString(resp.html)}")
	public static String decodeString(String base64){
		return base64  != null ? new String(Base64.decodeBase64(base64)) :null;
	}
	
	@Comment("Based onbyte[]Conductbase64Decrypt")
	@Example("${base64.decodeString(resp.bytes)}")
	public static String decodeString(byte[] base64){
		return base64  != null ? new String(Base64.decodeBase64(base64)) :null;
	}
	
	@Comment("Based onbyte[]Conductbase64Decrypt")
	@Example("${base64.decodeString(resp.bytes,'UTF-8')}")
	public static String decodeString(byte[] base64,String charset){
		return base64  != null ? StringFunctionExecutor.newString(Base64.decodeBase64(base64),charset) :null;
	}
}
