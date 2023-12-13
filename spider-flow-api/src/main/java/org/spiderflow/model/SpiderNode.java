package org.spiderflow.model;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import com.alibaba.fastjson.JSONArray;


/**
 * Crawling interval
 * @author jmxd
 *
 */
public class SpiderNode {
	/**
	 * 15th LastJsonProperty
	 */
	private Map<String,Object> jsonProperty = new HashMap<>();
	/**
	 * Next node in the list
	 */
	private List<SpiderNode> nextNodes = new ArrayList<>();

	/**
	 * The previous node in the list
	 */
	private List<SpiderNode> prevNodes = new ArrayList<>();

	/**
	 * Parent nodeID
	 */
	private Set<String> parentNodes;

	/**
	 * Condition to switch nodes
	 */
	private Map<String,String> condition = new HashMap<>();

	/**
	 * Abnormal Rotation
	 */
	private Map<String,String> exception = new HashMap<>();

	/**
	 * Pass Variable
	 */
	private Map<String,String> transmitVariable = new HashMap<>();
	/**
	 * Question Name
	 */
	private String nodeName;
	/**
	 * 15th LastID
	 */
	private String nodeId;

	/**
	 * Counter,The current node has finished execution
	 */
	private AtomicInteger counter = new AtomicInteger();

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getStringJsonValue(String key){
		String value = (String) this.jsonProperty.get(key);
		if(value != null){
			value = StringEscapeUtils.unescapeHtml4(value);
		}
		return value;
	}

	public String getStringJsonValue(String key,String defaultValue){
		String value = getStringJsonValue(key);
		return StringUtils.isNotBlank(value) ? value : defaultValue;
	}
	
	public List<Map<String,String>> getListJsonValue(String ... keys){
		List<JSONArray> arrays = new ArrayList<>();
		int size = -1;
		List<Map<String,String>> result = new ArrayList<>();
		for (int i = 0; i < keys.length; i++) {
			JSONArray jsonArray = (JSONArray) this.jsonProperty.get(keys[i]);
			if(jsonArray != null){
				if(size == -1){
					size = jsonArray.size();
				}else if(size != jsonArray.size()){
					throw new ArrayIndexOutOfBoundsException();
				}
				arrays.add(jsonArray);
			}
		}
		for (int i = 0;i < size;i++) {
			Map<String,String> item = new HashMap<>();
			for (int j = 0; j < keys.length; j++) {
				String val = arrays.get(j).getString(i);
				if(val != null){
					val = StringEscapeUtils.unescapeHtml4(val);
				}
				item.put(keys[j],val);
			}
			result.add(item);
		}
		return result;
	}
	public void setJsonProperty(Map<String, Object> jsonProperty) {
		this.jsonProperty = jsonProperty;
	}

	public void addNextNode(SpiderNode nextNode){
		nextNode.prevNodes.add(this);
		this.nextNodes.add(nextNode);
	}

	public String getExceptionFlow(String fromNodeId) {
		return exception.get(fromNodeId);
	}

	public boolean isTransmitVariable(String fromNodeId) {
		String value = transmitVariable.get(fromNodeId);
		return value == null || "1".equalsIgnoreCase(value);
	}

	public void setTransmitVariable(String fromNodeId,String value){
		this.transmitVariable.put(fromNodeId,value);
	}

	public void setExceptionFlow(String fromNodeId,String value){
		this.exception.put(fromNodeId,value);
	}

	public List<SpiderNode> getNextNodes() {
		return nextNodes;
	}

	public String getCondition(String fromNodeId) {
		return condition.get(fromNodeId);
	}

	public void setCondition(String fromNodeId,String condition) {
		this.condition.put(fromNodeId, condition);
	}

	public void increment(){
		counter.incrementAndGet();
	}

	public void decrement(){
		counter.decrementAndGet();
	}

	public boolean hasLeftNode(String nodeId){
		if(parentNodes == null){
			Set<String> parents = new HashSet<>();
			generateParents(parents);
			this.parentNodes = parents;
		}
		return this.parentNodes.contains(nodeId);
	}

	private void generateParents(Set<String> parents){
		for (SpiderNode prevNode : prevNodes) {
			if(parents.add(prevNode.nodeId)){
				prevNode.generateParents(parents);
			}
		}
	}

	public boolean isDone(){
		return isDone(new HashSet<>());
	}
	public boolean isDone(Set<String> visited){
		if(this.counter.get() == 0){
			for (SpiderNode prevNode : prevNodes) {
				if(visited.add(nodeId)&&!prevNode.isDone(visited)){
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "SpiderNode [jsonProperty=" + jsonProperty + ", nextNodes=" + nextNodes + ", condition=" + condition
				+ ", nodeName=" + nodeName + ", nodeId=" + nodeId + "]";
	}
}
