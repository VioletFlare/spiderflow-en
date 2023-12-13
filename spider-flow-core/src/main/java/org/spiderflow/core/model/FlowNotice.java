package org.spiderflow.core.model;

import org.spiderflow.enums.FlowNoticeWay;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 *  Climb Rate
 * 
 * @author BillDowney
 * @date 2020Year4Month3Sun Afternoon2:57:46
 */
@TableName("sp_flow_notice")
public class FlowNotice {

	@TableField(exist = false)
	private final String START_FLAG = "1";

	/**
	 * Keywords,Corresponds{@link SpiderFlow}中的流程id
	 */
	@TableId(type = IdType.UUID)
	private String id;
	/**
	 * Recipient,Multiple Recipient","15th，Add a custom notification tag,Use default configuration method unless otherwise noted
	 * Example：sms:13012345678,email:12345678@qq.com,13012345670
	 */
	private String recipients;
	/**
	 * Notify by{@link FlowNoticeWay}
	 */
	private String noticeWay;
	/**
	 * Start Event Notify:1:Enable notifications,0:Disable alarms
	 */
	private String startNotice;
	/**
	 * Processing exception notification:1:Enable notifications,0:Disable alarms
	 */
	private String exceptionNotice;
	/**
	 * End of process notification:1:Enable notifications,0:Disable alarms
	 */
	private String endNotice;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRecipients() {
		return recipients;
	}

	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}

	public String getNoticeWay() {
		return noticeWay;
	}

	public void setNoticeWay(String noticeWay) {
		this.noticeWay = noticeWay;
	}

	public String getStartNotice() {
		return startNotice;
	}

	public void setStartNotice(String startNotice) {
		this.startNotice = startNotice;
	}

	public String getExceptionNotice() {
		return exceptionNotice;
	}

	public void setExceptionNotice(String exceptionNotice) {
		this.exceptionNotice = exceptionNotice;
	}

	public String getEndNotice() {
		return endNotice;
	}

	public void setEndNotice(String endNotice) {
		this.endNotice = endNotice;
	}

	public boolean judgeStartNotice() {
		if (START_FLAG.equals(this.startNotice)) {
			return true;
		}
		return false;
	}

	public boolean judgeExceptionNotice() {
		if (START_FLAG.equals(this.exceptionNotice)) {
			return true;
		}
		return false;
	}

	public boolean judgeEndNotice() {
		if (START_FLAG.equals(this.endNotice)) {
			return true;
		}
		return false;
	}

}
