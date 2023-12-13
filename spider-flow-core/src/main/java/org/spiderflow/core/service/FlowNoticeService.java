package org.spiderflow.core.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spiderflow.core.expression.ExpressionTemplate;
import org.spiderflow.core.expression.ExpressionTemplateContext;
import org.spiderflow.core.mapper.FlowNoticeMapper;
import org.spiderflow.core.mapper.SpiderFlowMapper;
import org.spiderflow.core.model.FlowNotice;
import org.spiderflow.core.model.SpiderFlow;
import org.spiderflow.core.utils.EmailUtils;
import org.spiderflow.core.utils.ExpressionUtils;
import org.spiderflow.enums.FlowNoticeType;
import org.spiderflow.enums.FlowNoticeWay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FlowNoticeService extends ServiceImpl<FlowNoticeMapper, FlowNotice> {

	private static final Logger logger = LoggerFactory.getLogger(FlowNoticeService.class);
	@Autowired
	private SpiderFlowMapper spiderFlowMapper;
	@Autowired
	private EmailUtils emailUtils;
	@Value("${spider.notice.subject:spider-flowTranslation}")
	private String subject;
	@Value("${spider.notice.content.start}")
	private String startContext;
	@Value("${spider.notice.content.end}")
	private String endContext;
	@Value("${spider.notice.content.exception}")
	private String exceptionContext;

	@Override
	public boolean saveOrUpdate(FlowNotice entity) {
		if (spiderFlowMapper.getCountById(entity.getId()) == 0) {
			throw new RuntimeException("No matching process found");
		}
		return super.saveOrUpdate(entity);
	}

	/**
	 * Send corresponding process notification
	 * 
	 * @param spiderFlow Process Information
	 * @param type       Notify type
	 * @author BillDowney
	 * @date 2020Year4Month4Sun AM1:37:50
	 */
	public void sendFlowNotice(SpiderFlow spiderFlow, FlowNoticeType type) {
		FlowNotice notice = baseMapper.selectById(spiderFlow.getId());
		if (notice != null && !StringUtils.isEmpty(notice.getRecipients())
				&& !StringUtils.isEmpty(notice.getNoticeWay())) {
			String content = null;
			String sendSubject = this.subject;
			switch (type) {
			case startNotice:
				if (notice.judgeStartNotice()) {
					content = startContext;
					sendSubject += " - The process is about to be executed";
				}
				break;
			case endNotice:
				if (notice.judgeEndNotice()) {
					content = endContext;
					sendSubject += " - Process completed";
				}
				break;
			case exceptionNotice:
				if (notice.judgeExceptionNotice()) {
					content = exceptionContext;
					sendSubject += " - There was an error while trying to print the list of tasks: %s";
				}
				break;
			}
			if (StringUtils.isEmpty(content)) {
				return;
			}
			// Define a contextual variable
			Map<String, Object> variables = new HashMap<String, Object>();
			// Add Process Information
			BeanMap beanMap = BeanMap.create(spiderFlow);
			for (Object key : beanMap.keySet()) {
				variables.put(key + "", beanMap.get(key));
			}
			// Enter the current time
			variables.put("currentDate", this.getCurrentDate());
			content = ExpressionUtils.execute(content.replaceAll("[{]", "\\${"), variables) + "";
			// Organize Recipients
			String recipients = notice.getRecipients();
			for (String recipient : recipients.split(",")) {
				String noticeWay = notice.getNoticeWay();
				String people = recipient;
				// If a question does not make any sense, or is not factually coherent, explain why instead of answering something not correct. If you don't know the answer to a question, please don't share false information.":"Show that the transmitting mode is set separately
				if (recipient.contains(":")) {
					String[] strs = recipient.split(":");
					noticeWay = strs[0];
					people = strs[1];
				}
				FlowNoticeWay way = FlowNoticeWay.email;
				try {
					way = FlowNoticeWay.valueOf(noticeWay);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				switch (way) {
				case email:
					emailUtils.sendSimpleMail(sendSubject, content, people);
					break;
				}
			}
		}
	}

	private String getCurrentDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return sdf.format(new Date());
	}

}
