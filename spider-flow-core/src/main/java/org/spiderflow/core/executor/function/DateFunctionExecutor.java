package org.spiderflow.core.executor.function;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.spiderflow.annotation.Comment;
import org.spiderflow.annotation.Example;
import org.spiderflow.executor.FunctionExecutor;
import org.springframework.stereotype.Component;

/**
 * Get the time/Format Class Instance PreventNPE Default Format(yyyy-MM-dd HH:mm:ss)
 * @author Administrator
 *
 */
@Component
@Comment("Date and Time")
public class DateFunctionExecutor implements FunctionExecutor{
	
	@Override
	public String getFunctionPrefix() {
		return "date";
	}
	
	private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

	@Comment("Format Date")
	@Example("${date.format(date.now())}")
	public static String format(Date date) {
		return format(date, DEFAULT_PATTERN);
	}

	@Comment("Format Date")
	@Example("${date.format(1569059534000l)}")
	public static String format(Long millis) {
		return format(millis, DEFAULT_PATTERN);
	}

	@Comment("Format Date")
	@Example("${date.format(date.now(),'yyyy-MM-dd')}")
	public static String format(Date date, String pattern) {
		return date != null ? DateFormatUtils.format(date, pattern) : null;
	}

	@Comment("Format Date")
	@Example("${date.format(1569059534000l,'yyyy-MM-dd')}")
	public static String format(Long millis, String pattern) {
		return millis != null ? DateFormatUtils.format(millis, pattern) : null;
	}
	
	@Comment("Date Created")
	@Example("${date.parse('2019-01-01 00:00:00')}")
	public static Date parse(String date) throws ParseException{
		return date != null ? DateUtils.parseDate(date, DEFAULT_PATTERN) : null;
	}
	
	@Comment("Date Created")
	@Example("${date.parse('2019-01-01','yyyy-MM-dd')}")
	public static Date parse(String date,String pattern) throws ParseException{
		return date != null ? DateUtils.parseDate(date, pattern) : null;
	}

	@Comment("The numeric value for the date type")
	@Example("${date.parse(1569059534000l)}")
	public static Date parse(Long millis){
		return new Date(millis);
	}
	
	@Comment("Get the current time")
	@Example("${date.now()}")
	public static Date now(){
		return new Date();
	}
	
	@Comment("Get specified datenNext Anniversary")
	@Example("${date.addYears(date.now(),2)}")
	public static Date addYears(Date date,int amount){
		return DateUtils.addYears(date, amount);
	}
	
	@Comment("Get specified datenNext Date")
	@Example("${date.addMonths(date.now(),2)}")
	public static Date addMonths(Date date,int amount){
		return DateUtils.addMonths(date, amount);
	}
	
	@Comment("Get specified datenNext Date")
	@Example("${date.addDays(date.now(),2)}")
	public static Date addDays(Date date,int amount){
		return DateUtils.addDays(date, amount);
	}
	
	@Comment("Get specified datenIn how many hours")
	@Example("${date.addHours(date.now(),2)}")
	public static Date addHours(Date date,int amount){
		return DateUtils.addHours(date, amount);
	}
	
	@Comment("Get specified daten20 minutes before appointment")
	@Example("${date.addMinutes(date.now(),2)}")
	public static Date addMinutes(Date date,int amount){
		return DateUtils.addMinutes(date, amount);
	}
	
	@Comment("Get specified datenSeconds")
	@Example("${date.addSeconds(date.now(),2)}")
	public static Date addSeconds(Date date,int amount){
		return DateUtils.addSeconds(date, amount);
	}
}
