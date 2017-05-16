package org.lsmy.cloud.common.util;

public class LongUtil {
	
	public static boolean isZero(Long value)
	{
		if(toLong(value) == 0l){
			return true;
		}
		return false;
	}
	
	public static boolean isNotZero(Long value)
	{
		return !isZero(value);
	}
	
	public static long toLong(Long value)
	{
		if(value == null){
			return 0l;
		}
		return value;
	}
	
	public static long toLong(Object value)
	{
		if(value == null || "".equals(value)){
			return 0l;
		}
		try{
			return Long.valueOf(value.toString());
		}catch(Exception e){
			return 0l;
		}
	}

}
