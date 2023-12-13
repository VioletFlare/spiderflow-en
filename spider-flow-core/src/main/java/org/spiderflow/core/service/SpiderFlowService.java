package org.spiderflow.core.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerUtils;
import org.quartz.spi.OperableTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spiderflow.core.job.SpiderJobManager;
import org.spiderflow.core.mapper.FlowNoticeMapper;
import org.spiderflow.core.mapper.SpiderFlowMapper;
import org.spiderflow.core.model.SpiderFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Crawling process is currently running
 * @author Administrator
 *
 */
@Service
public class SpiderFlowService extends ServiceImpl<SpiderFlowMapper, SpiderFlow> {

	@Autowired
	private SpiderFlowMapper sfMapper;

	@Autowired
	private SpiderJobManager spiderJobManager;

	@Autowired
	private FlowNoticeMapper flowNoticeMapper;

	private static Logger logger = LoggerFactory.getLogger(SpiderFlowService.class);

	@Value("${spider.workspace}")
	private String workspace;

	//Perform the tasks that gedit will automatically execute on file open
	@PostConstruct
	private void initJobs(){
		//Next scheduled task time
		sfMapper.resetNextExecuteTime();
		//Get enablecorn的Tasks
		List<SpiderFlow> spiderFlows = sfMapper.selectList(new QueryWrapper<SpiderFlow>().eq("enabled", "1"));
		if(spiderFlows != null && !spiderFlows.isEmpty()){
			for (SpiderFlow sf : spiderFlows) {
				if(StringUtils.isNotEmpty(sf.getCron())){
					Date nextExecuteTimt = spiderJobManager.addJob(sf);
					if (nextExecuteTimt != null) {
						sf.setNextExecuteTime(nextExecuteTimt);
						sfMapper.updateById(sf);
					}
				}
			}
		}
	}

	public IPage<SpiderFlow> selectSpiderPage(Page<SpiderFlow> page, String name){
		return sfMapper.selectSpiderPage(page,name);
	}

	public int executeCountIncrement(String id, Date lastExecuteTime, Date nextExecuteTime){
		if(nextExecuteTime == null){
			return sfMapper.executeCountIncrement(id, lastExecuteTime);
		}
		return sfMapper.executeCountIncrementAndExecuteTime(id, lastExecuteTime, nextExecuteTime);

	}

	/**
	 * Reset Scheduled Task
	 * @param id CrawlingID
	 * @param cron Timer
	 */
	public void resetCornExpression(String id, String cron){
		CronTrigger trigger = TriggerBuilder.newTrigger()
				.withIdentity("Caclulate Next Execute Date")
				.withSchedule(CronScheduleBuilder.cronSchedule(cron))
				.build();
		sfMapper.resetCornExpression(id, cron, trigger.getFireTimeAfter(null));
		spiderJobManager.remove(id);
		SpiderFlow spiderFlow = getById(id);
		if("1".equals(spiderFlow.getEnabled()) && StringUtils.isNotEmpty(spiderFlow.getCron())){
			spiderJobManager.addJob(spiderFlow);
		}
	}

	@Override
	public boolean save(SpiderFlow spiderFlow){
		//Analysecorn,Get and set the start time of a to-do
		if(StringUtils.isNotEmpty(spiderFlow.getCron())){
			CronTrigger trigger = TriggerBuilder.newTrigger()
							.withIdentity("Caclulate Next Execute Date")
							.withSchedule(CronScheduleBuilder.cronSchedule(spiderFlow.getCron()))
							.build();
			spiderFlow.setNextExecuteTime(trigger.getStartTime());
		}
		if(StringUtils.isNotEmpty(spiderFlow.getId())){	//update Tasks
			sfMapper.updateSpiderFlow(spiderFlow.getId(), spiderFlow.getName(), spiderFlow.getXml());
			spiderJobManager.remove(spiderFlow.getId());
			spiderFlow = getById(spiderFlow.getId());
			if("1".equals(spiderFlow.getEnabled()) && StringUtils.isNotEmpty(spiderFlow.getCron())){
				spiderJobManager.addJob(spiderFlow);
			}
		}else{//insert Tasks
			String id = UUID.randomUUID().toString().replace("-", "");
			sfMapper.insertSpiderFlow(id, spiderFlow.getName(), spiderFlow.getXml());
			spiderFlow.setId(id);
		}
		File file = new File(workspace,spiderFlow.getId() + File.separator + "xmls" + File.separator + System.currentTimeMillis() + ".xml");
		try {
			FileUtils.write(file,spiderFlow.getXml(),"UTF-8");
		} catch (IOException e) {
			logger.error("Save History Error",e);
		}
		return true;
	}

	public void stop(String id){
		sfMapper.resetSpiderStatus(id,"0");
		sfMapper.resetNextExecuteTime(id);
		spiderJobManager.remove(id);
	}

	public void copy(String id){
		// CopyID
		SpiderFlow spiderFlow = sfMapper.selectById(id);
		String new_id = UUID.randomUUID().toString().replace("-", "");
		sfMapper.insertSpiderFlow(new_id, spiderFlow.getName() + "-copy", spiderFlow.getXml());
	}

	public void start(String id){
		spiderJobManager.remove(id);
		SpiderFlow spiderFlow = getById(id);
		Date nextExecuteTime = spiderJobManager.addJob(spiderFlow);
		if (nextExecuteTime != null) {
			spiderFlow.setNextExecuteTime(nextExecuteTime);
			sfMapper.updateById(spiderFlow);
			sfMapper.resetSpiderStatus(id, "1");
		}
	}

	public void run(String id){
		spiderJobManager.run(id);
	}

	public void resetExecuteCount(String id){
		sfMapper.resetExecuteCount(id);
	}
	public void remove(String id){
		sfMapper.deleteById(id);
		spiderJobManager.remove(id);
		flowNoticeMapper.deleteById(id);
	}

	public List<SpiderFlow> selectOtherFlows(String id){
		return sfMapper.selectOtherFlows(id);
	}

	public List<SpiderFlow> selectFlows(){
		return sfMapper.selectFlows();
	}

    /**
     * Get the recent run times for an expression
	 * @param cron Answer
	 * @param numTimes Repeated
	 * @return
     */
	public List<String> getRecentTriggerTime(String cron,int numTimes) {
		List<String> list = new ArrayList<>();
		CronTrigger trigger;
		try {
			trigger = TriggerBuilder.newTrigger()
					.withSchedule(CronScheduleBuilder.cronSchedule(cron))
					.build();
		}catch (Exception e) {
			list.add("cronAnswer "+cron+" 有误：" + e.getCause());
			return list;
		}
		List<Date> dates = TriggerUtils.computeFireTimes((OperableTrigger) trigger, null, numTimes);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (Date date : dates) {
			list.add(dateFormat.format(date));
		}
		return list;
	}

	public List<Long> historyList(String id){
		File directory = new File(workspace, id + File.separator + "xmls");
		if(directory.exists() && directory.isDirectory()){
			File[] files = directory.listFiles((dir, name) -> name.endsWith(".xml"));
			if(files != null && files.length > 0){
				return Arrays.stream(files).map(f-> Long.parseLong(f.getName().replace(".xml",""))).sorted().collect(Collectors.toList());
			}
		}
		return Collections.emptyList();
	}

	public String readHistory(String id,String timestamp){
		File file = new File(workspace, id + File.separator + "xmls" + File.separator + timestamp + ".xml");
		if(file.exists()){
			try {
				return FileUtils.readFileToString(file,"UTF-8");
			} catch (IOException e) {
				logger.error("Read history version error",e);
			}
		}
		return null;
	}

	public Integer getFlowMaxTaskId(String flowId){
		return sfMapper.getFlowMaxTaskId(flowId);
	}
}
