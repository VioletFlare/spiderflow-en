package org.spiderflow.enums;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Process Notification Mode
 * 
 * @author BillDowney
 * @date 2020Year4Month3Sun Afternoon3:26:18
 */
public enum FlowNoticeWay {

	email("Email Notifications");

	private FlowNoticeWay(String title) {
		this.title = title;
	}

	private String title;

	@Override
	public String toString() {
		return this.name() + ":" + this.title;
	}

	public static Map<String, String> getMap() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (FlowNoticeWay type : FlowNoticeWay.values()) {
			map.put(type.name(), type.toString());
		}
		return map;
	}

}
