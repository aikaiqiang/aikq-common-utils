package org.lsmy.cloud.common.util;

import java.util.UUID;

import org.lsmy.cloud.common.bean.IdempotentBean;

public class IdempotentUtil {

	public static void init(IdempotentBean idempotentBean){
		if(idempotentBean == null) return;
		idempotentBean.setIdempotent_uuid(get());
	}
	
	public static String get(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	public static String get(Class<?> clazz,String method){
		return clazz.getName()+"."+method;
	}
}
