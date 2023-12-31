package org.spiderflow.core.executor.shape;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spiderflow.Grammerable;
import org.spiderflow.context.CookieContext;
import org.spiderflow.context.SpiderContext;
import org.spiderflow.core.executor.function.MD5FunctionExecutor;
import org.spiderflow.core.io.HttpRequest;
import org.spiderflow.core.io.HttpResponse;
import org.spiderflow.core.utils.ExpressionUtils;
import org.spiderflow.executor.ShapeExecutor;
import org.spiderflow.io.SpiderResponse;
import org.spiderflow.listener.SpiderListener;
import org.spiderflow.model.Grammer;
import org.spiderflow.model.SpiderNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Request Executor
 * @author Administrator
 *
 */
@Component
public class RequestExecutor implements ShapeExecutor,Grammerable, SpiderListener {
	
	public static final String SLEEP = "sleep";
	
	public static final String URL = "url";
	
	public static final String PROXY = "proxy";
	
	public static final String REQUEST_METHOD = "method";
	
	public static final String PARAMETER_NAME = "parameter-name";
	
	public static final String PARAMETER_VALUE = "parameter-value";

	public static final String COOKIE_NAME = "cookie-name";

	public static final String COOKIE_VALUE = "cookie-value";

	public static final String PARAMETER_FORM_NAME = "parameter-form-name";
	
	public static final String PARAMETER_FORM_VALUE = "parameter-form-value";
	
	public static final String PARAMETER_FORM_FILENAME = "parameter-form-filename";
	
	public static final String PARAMETER_FORM_TYPE = "parameter-form-type";
	
	public static final String BODY_TYPE = "body-type";
	
	public static final String BODY_CONTENT_TYPE = "body-content-type";
	
	public static final String REQUEST_BODY = "request-body";
	
	public static final String HEADER_NAME = "header-name";
	
	public static final String HEADER_VALUE = "header-value";
	
	public static final String TIMEOUT = "timeout";

	public static final String RETRY_COUNT = "retryCount";

	public static final String RETRY_INTERVAL = "retryInterval";

	public static final String RESPONSE_CHARSET = "response-charset";
	
	public static final String FOLLOW_REDIRECT = "follow-redirect";
	
	public static final String TLS_VALIDATE = "tls-validate";

	public static final String LAST_EXECUTE_TIME = "__last_execute_time_";

	public static final String COOKIE_AUTO_SET = "cookie-auto-set";

	public static final String REPEAT_ENABLE = "repeat-enable";

	public static final String BLOOM_FILTER_KEY = "_bloomfilter";

	@Value("${spider.workspace}")
	private String workspcace;

	@Value("${spider.bloomfilter.capacity:5000000}")
	private Integer capacity;

	@Value("${spider.bloomfilter.error-rate:0.00001}")
	private Double errorRate;

	private static final Logger logger = LoggerFactory.getLogger(RequestExecutor.class);
	
	@Override
	public String supportShape() {
		return "request";
	}

	@PostConstruct
	void init(){
		//Allow setting of restricted request headers
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
	}

	@Override
	public void execute(SpiderNode node, SpiderContext context, Map<String,Object> variables) {
		CookieContext cookieContext = context.getCookieContext();
		String sleepCondition = node.getStringJsonValue(SLEEP);
		if(StringUtils.isNotBlank(sleepCondition)){
			try {
				Object value = ExpressionUtils.execute(sleepCondition, variables);
				if(value != null){
					long sleepTime = NumberUtils.toLong(value.toString(), 0L);
					synchronized (node.getNodeId().intern()) {
						//Actual wait time = Next time the reminder is triggered + Sleep Time - Current Time
						Long lastExecuteTime = context.get(LAST_EXECUTE_TIME + node.getNodeId(), 0L);
						if (lastExecuteTime != 0) {
							sleepTime = lastExecuteTime + sleepTime - System.currentTimeMillis();
						}
						if (sleepTime > 0) {
							context.pause(node.getNodeId(),"common",SLEEP,sleepTime);
							logger.debug("Please set a delay:{}ms", sleepTime);
							Thread.sleep(sleepTime);
						}
						context.put(LAST_EXECUTE_TIME + node.getNodeId(), System.currentTimeMillis());
					}
				}
			} catch (Throwable t) {
				logger.error("Failed to set delay.", t);
			}
		}
		BloomFilter<String> bloomFilter = null;
		//Retry Count
		int retryCount = NumberUtils.toInt(node.getStringJsonValue(RETRY_COUNT), 0) + 1;
		//Retry delay，Seconds
		int retryInterval = NumberUtils.toInt(node.getStringJsonValue(RETRY_INTERVAL), 0);
        boolean successed = false;
		for (int i = 0; i < retryCount && !successed; i++) {
			HttpRequest request = HttpRequest.create();
			//Setup Requesturl
			String url = null;
			try {
				url = ExpressionUtils.execute(node.getStringJsonValue(URL), variables).toString();
			} catch (Exception e) {
				logger.error("Setup RequesturlError，Anomalous messages", e);
				ExceptionUtils.wrapAndThrow(e);
			}
			if("1".equalsIgnoreCase(node.getStringJsonValue(REPEAT_ENABLE,"0"))){
				bloomFilter = createBloomFilter(context);
				synchronized (bloomFilter){
					if(bloomFilter.mightContain(MD5FunctionExecutor.string(url))){
						logger.info("The following text is not translated:URL:{}",url);
						return;
					}
				}
			}
			context.pause(node.getNodeId(),"common",URL,url);
			logger.info("Setup Requesturl:{}", url);
			request.url(url);
			//Please set the request timeout
			int timeout = NumberUtils.toInt(node.getStringJsonValue(TIMEOUT), 60000);
			logger.debug("Please set the request timeout:{}", timeout);
			request.timeout(timeout);

			String method = Objects.toString(node.getStringJsonValue(REQUEST_METHOD), "GET");
			//Set Request Method
			request.method(method);
			logger.debug("Set Request Method:{}", method);

			//Whether to follow redirects
			boolean followRedirects = !"0".equals(node.getStringJsonValue(FOLLOW_REDIRECT));
			request.followRedirect(followRedirects);
			logger.debug("Set Up Follow Up Redirect：{}", followRedirects);

			SpiderNode root = context.getRootNode();
			//Setup Requestheader
			setRequestHeader(root, request, root.getListJsonValue(HEADER_NAME,HEADER_VALUE), context, variables);
			setRequestHeader(node, request, node.getListJsonValue(HEADER_NAME,HEADER_VALUE), context, variables);

			//Set GlobalCookie
			Map<String, String> cookies = getRequestCookie(root, root.getListJsonValue(COOKIE_NAME, COOKIE_VALUE), context, variables);
			if(!cookies.isEmpty()){
				logger.info("Set GlobalCookie：{}", cookies);
				request.cookies(cookies);
			}
			//Set Up Automatic ManagementCookie
			boolean cookieAutoSet = !"0".equals(node.getStringJsonValue(COOKIE_AUTO_SET));
			if(cookieAutoSet && !cookieContext.isEmpty()){
				context.pause(node.getNodeId(),COOKIE_AUTO_SET,COOKIE_AUTO_SET,cookieContext);
				request.cookies(cookieContext);
				logger.info("Auto SetupCookie：{}", cookieContext);
			}
			//Set Up This NodeCookie
			cookies = getRequestCookie(node, node.getListJsonValue(COOKIE_NAME, COOKIE_VALUE), context, variables);
			if(!cookies.isEmpty()){
				request.cookies(cookies);
				logger.debug("1 hour before appointmentCookie：{}", cookies);
			}
			if(cookieAutoSet){
				cookieContext.putAll(cookies);
			}

			String bodyType = node.getStringJsonValue(BODY_TYPE);
			List<InputStream> streams = null;
			if("raw".equals(bodyType)){
				String contentType = node.getStringJsonValue(BODY_CONTENT_TYPE);
				request.contentType(contentType);
				try {
					Object requestBody = ExpressionUtils.execute(node.getStringJsonValue(REQUEST_BODY), variables);
					context.pause(node.getNodeId(),"request-body",REQUEST_BODY,requestBody);
					request.data(requestBody);
					logger.info("Setup RequestBody:{}", requestBody);
				} catch (Exception e) {
					logger.debug("Setup RequestBodyError", e);
				}
			}else if("form-data".equals(bodyType)){
				List<Map<String, String>> formParameters = node.getListJsonValue(PARAMETER_FORM_NAME,PARAMETER_FORM_VALUE,PARAMETER_FORM_TYPE,PARAMETER_FORM_FILENAME);
				streams = setRequestFormParameter(node,request,formParameters,context,variables);
			}else{
				//Set Request Parameters
				setRequestParameter(root, request, root.getListJsonValue(PARAMETER_NAME,PARAMETER_VALUE), context, variables);
				setRequestParameter(node, request, node.getListJsonValue(PARAMETER_NAME,PARAMETER_VALUE), context, variables);
			}
			//Set Up Proxy
			String proxy = node.getStringJsonValue(PROXY);
			if(StringUtils.isNotBlank(proxy)){
				try {
					Object value = ExpressionUtils.execute(proxy, variables);
					context.pause(node.getNodeId(),"common",PROXY,value);
					if(value != null){
						String[] proxyArr = value.toString().split(":");
						if(proxyArr.length == 2){
							request.proxy(proxyArr[0], Integer.parseInt(proxyArr[1]));
							logger.info("Set Up Proxy：{}",proxy);
						}
					}
				} catch (Exception e) {
					logger.error("Set Proxy Error，Anomalous messages:{}",e);
				}
			}
			Throwable exception = null;
			try {
				HttpResponse response = request.execute();
                successed = response.getStatusCode() == 200;
                if(successed){
                	if(bloomFilter != null){
                		synchronized (bloomFilter){
							bloomFilter.put(MD5FunctionExecutor.string(url));
						}
					}
                    String charset = node.getStringJsonValue(RESPONSE_CHARSET);
                    if(StringUtils.isNotBlank(charset)){
                        response.setCharset(charset);
                        logger.debug("1 hour before appointmentresponse charset:{}",charset);
                    }
                    //cookieSavecookieContext
                    cookieContext.putAll(response.getCookies());
                    //Save result to variable
                    variables.put("resp", response);
                }
			} catch (IOException e) {
				successed = false;
                exception = e;
			} finally{
                if(streams != null){
                    for (InputStream is : streams) {
                        try {
                            is.close();
                        } catch (Exception e) {
                        }
                    }
                }
                if(!successed){
                    if(i + 1 < retryCount){
                        if(retryInterval > 0){
                            try {
                                Thread.sleep(retryInterval);
                            } catch (InterruptedException ignored) {
                            }
                        }
                        logger.info("1st{}Next Retry:{}",i + 1,url);
                    }else{
                        //Log access denied log
						if(context.getFlowId() != null){ //Test Environment
							//TODO Add Record Request Parameters
							File file = new File(workspcace, context.getFlowId() + File.separator + "logs" + File.separator + "access_error.log");
							try {
								File directory = file.getParentFile();
								if(!directory.exists()){
									directory.mkdirs();
								}
								FileUtils.write(file,url + "\r\n","UTF-8",true);
							} catch (IOException ignored) {
							}
						}
                        logger.error("Request{}Error,Anomalous messages:{}",url,exception);
                    }
                }
			}
		}
	}
	
	private List<InputStream> setRequestFormParameter(SpiderNode node, HttpRequest request,List<Map<String, String>> parameters,SpiderContext context,Map<String,Object> variables){
		List<InputStream> streams = new ArrayList<>();
		if(parameters != null){
			for (Map<String,String> nameValue : parameters) {
				Object value;
				String parameterName = nameValue.get(PARAMETER_FORM_NAME);
				if(StringUtils.isNotBlank(parameterName)){
					String parameterValue = nameValue.get(PARAMETER_FORM_VALUE);
					String parameterType = nameValue.get(PARAMETER_FORM_TYPE);
					String parameterFilename = nameValue.get(PARAMETER_FORM_FILENAME);
					boolean hasFile = "file".equals(parameterType);
					try {
						value = ExpressionUtils.execute(parameterValue, variables);
						if(hasFile){
							InputStream stream = null;
							if(value instanceof byte[]){
								stream = new ByteArrayInputStream((byte[]) value);
							}else if(value instanceof String){
								stream = new ByteArrayInputStream(((String)value).getBytes());
							}else if(value instanceof InputStream){
								stream = (InputStream) value;
							}
							if(stream != null){
								streams.add(stream);
								request.data(parameterName, parameterFilename, stream);
								context.pause(node.getNodeId(),"request-body",parameterName,parameterFilename);
								logger.info("Set Request Parameters：{}={}",parameterName,parameterFilename);
							}else{
								logger.warn("Set Request Parameters：{}Failure，No binary content",parameterName);
							}
						}else{
							request.data(parameterName, value);
							context.pause(node.getNodeId(),"request-body",parameterName,value);
							logger.info("Set Request Parameters：{}={}",parameterName,value);
						}
						
					} catch (Exception e) {
						logger.error("Set Request Parameters：{}Error,Anomalous messages:{}",parameterName,e);
					}
				}
			}
		}
		return streams;
	}

	private Map<String,String> getRequestCookie(SpiderNode node, List<Map<String, String>> cookies, SpiderContext context, Map<String, Object> variables) {
		Map<String,String> cookieMap = new HashMap<>();
		if (cookies != null) {
			for (Map<String, String> nameValue : cookies) {
				Object value;
				String cookieName = nameValue.get(COOKIE_NAME);
				if (StringUtils.isNotBlank(cookieName)) {
					String cookieValue = nameValue.get(COOKIE_VALUE);
					try {
						value = ExpressionUtils.execute(cookieValue, variables);
						if (value != null) {
							cookieMap.put(cookieName, value.toString());
							context.pause(node.getNodeId(),"request-cookie",cookieName,value.toString());
							logger.info("Setup RequestCookie：{}={}", cookieName, value);
						}
					} catch (Exception e) {
						logger.error("Setup RequestCookie：{}Error,Anomalous messages：{}", cookieName, e);
					}
				}
			}
		}
		return cookieMap;
	}

	private void setRequestParameter(SpiderNode node, HttpRequest request, List<Map<String, String>> parameters, SpiderContext context, Map<String, Object> variables) {
		if (parameters != null) {
			for (Map<String, String> nameValue : parameters) {
				Object value = null;
				String parameterName = nameValue.get(PARAMETER_NAME);
				if (StringUtils.isNotBlank(parameterName)) {
					String parameterValue = nameValue.get(PARAMETER_VALUE);
					try {
						value = ExpressionUtils.execute(parameterValue, variables);
						context.pause(node.getNodeId(),"request-parameter",parameterName,value);
						logger.info("Set Request Parameters：{}={}", parameterName, value);
					} catch (Exception e) {
						logger.error("Set Request Parameters：{}Error,Anomalous messages：{}", parameterName, e);
					}
					request.data(parameterName, value);
				}
			}
		}
	}

	private void setRequestHeader(SpiderNode node,HttpRequest request, List<Map<String, String>> headers, SpiderContext context, Map<String, Object> variables) {
		if (headers != null) {
			for (Map<String, String> nameValue : headers) {
				Object value = null;
				String headerName = nameValue.get(HEADER_NAME);
				if (StringUtils.isNotBlank(headerName)) {
					String headerValue = nameValue.get(HEADER_VALUE);
					try {
						value = ExpressionUtils.execute(headerValue, variables);
						context.pause(node.getNodeId(),"request-header",headerName,value);
						logger.info("Setup RequestHeader：{}={}", headerName, value);
					} catch (Exception e) {
						logger.error("Setup RequestHeader：{}Error,Anomalous messages：{}", headerName, e);
					}
					request.header(headerName, value);
				}
			}
		}
	}

	@Override
	public List<Grammer> grammers() {
		List<Grammer> grammers = Grammer.findGrammers(SpiderResponse.class,"resp" , "SpiderResponse", false);
		Grammer grammer = new Grammer();
		grammer.setFunction("resp");
		grammer.setComment("Fetch Results");
		grammer.setOwner("SpiderResponse");
		grammers.add(grammer);
		return grammers;
	}

	@Override
	public void beforeStart(SpiderContext context) {

	}

	private BloomFilter<String> createBloomFilter(SpiderContext context){
		BloomFilter<String> filter = context.get(BLOOM_FILTER_KEY);
		if(filter == null){
			Funnel<CharSequence> funnel = Funnels.stringFunnel(Charset.forName("UTF-8"));
			String fileName = context.getFlowId() + File.separator + "url.bf";
			File file = new File(workspcace,fileName);
			if(file.exists()){
				try(FileInputStream fis = new FileInputStream(file)){
					filter = BloomFilter.readFrom(fis,funnel);
				} catch (IOException e) {
					logger.error("Failed to read from brl filter",e);
				}

			}else{
				filter = BloomFilter.create(funnel,capacity,errorRate);
			}
			context.put(BLOOM_FILTER_KEY,filter);
		}
		return filter;
	}

	@Override
	public void afterEnd(SpiderContext context) {
		BloomFilter<String> filter = context.get(BLOOM_FILTER_KEY);
		if(filter != null){
			File file = new File(workspcace,context.getFlowId() + File.separator + "url.bf");
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			try(FileOutputStream fos = new FileOutputStream(file)){
				filter.writeTo(fos);
				fos.flush();
			}catch(IOException e){
				logger.error("Save Error",e);
			}
		}
	}
}
