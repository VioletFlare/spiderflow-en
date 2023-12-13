package org.spiderflow.core.executor.function;

import org.apache.commons.lang3.RandomUtils;
import org.spiderflow.annotation.Comment;
import org.spiderflow.annotation.Example;
import org.spiderflow.executor.FunctionExecutor;
import org.springframework.stereotype.Component;

/**
 * Random number/String Generating method 
 * @author Administrator
 *
 */
@Component
public class RandomFunctionExecutor implements FunctionExecutor{
	
	@Override
	public String getFunctionPrefix() {
		return "random";
	}
	
	@Comment("Get Randomint")
	@Example("${random.randomInt(1,10)}")
	public static int randomInt(int min,int max){
		return RandomUtils.nextInt(min, max);
	}
	
	@Comment("Get Randomdouble")
	@Example("${random.randomDouble(1,10)}")
	public static double randomDouble(double min,double max){
		return RandomUtils.nextDouble(min, max);
	}
	
	@Comment("Get Randomlong")
	@Example("${random.randomLong(1,10)}")
	public static long randomLong(long min,long max){
		return RandomUtils.nextLong(min, max);
	}
	
	/**
	 * 
	 * @param chars Number of characters
	 * @param length Character Ranges
	 * @return String 随机字符串
	 */
	@Comment("Get random string")
	@Example("${random.string('abcde',10)}")
	public static String string(String chars,int length){
		if (chars != null) {
			char[] newChars = new char[length];
			int len = chars.length();
			for (int i = 0; i < length; i++) {
				newChars[i] = chars.charAt(randomInt(0,len));
			}
			return new String(newChars);
		}
		return null;
	}
	
}
