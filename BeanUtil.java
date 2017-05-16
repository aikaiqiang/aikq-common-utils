package org.lsmy.cloud.common.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.lsmy.cloud.common.mybatis.Page;



/**
 * @Description 复制属性专用
 * @author xhz
 * @date 2017年4月24日 下午2:24:22
 */
public class BeanUtil {

	/**
	 * @Description 单个复制
	 * @author xhz
	 * @date 2017年4月24日 下午2:24:38
	 * @param source
	 * @param clazz
	 * @return
	 * @lastModifier
	 */
	public static <T> T copy(Object source,Class<T> clazz)
	{
		if(source == null){
			return null;
		}
		try {
			T target = clazz.newInstance();
			copy(source,target);
			return target;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
		
	}
	
	/**
	 * @Description 获取所有的成员变量，包括父类
	 * @author xhz
	 * @date 2017年4月24日 下午2:24:49
	 * @param clazz
	 * @return
	 * @lastModifier
	 */
	public static List<Field> getAllField(Class<?> clazz){
		
		List<Field> liField = new ArrayList<Field>();
		while(true){
			Field[] fields = clazz.getDeclaredFields();
			for(Field field :fields){
				liField.add(field);
			}
			clazz = clazz.getSuperclass();
			if(clazz == null){
				break;
			}
		}
		return liField;
	}
	
	/**
	 * @Description 获取属性，包括父类
	 * @author xhz
	 * @date 2017年4月24日 下午2:25:36
	 * @param clazz
	 * @param name
	 * @return
	 * @lastModifier
	 */
	public static Field getField(Class<?> clazz,String name){
		Field field = null;
		while(true){
			try {
				field = clazz.getDeclaredField(name);
				break;
			} catch (Exception e) {
			}
			clazz = clazz.getSuperclass();
			if(clazz == null){
				break;
			}
		}
		
		return field;
	}
	
	/**
	 * @Description 复制所有属性
	 * @author xhz
	 * @date 2017年4月24日 下午2:24:06
	 * @param source
	 * @param target
	 * @lastModifier
	 */
	public static void copy(Object source,Object target){
		if(source == null || target == null) return; 
		
		Class<?> clazz = target.getClass();
		Class<?> sourceClazz = source.getClass();
		
		List<Field> liFiled = getAllField(clazz);
		for(Field field : liFiled){
			Field srouceField = getField(sourceClazz,field.getName());
			if(srouceField == null){
				continue;
			}
			field.setAccessible(true);
			srouceField.setAccessible(true);
			try {
				field.set(target, srouceField.get(source));
			} catch (Exception e) {
			} 
		}
	}
	
	/**
	 * @Description 批量复制属性
	 * @author xhz
	 * @date 2017年4月24日 下午2:26:06
	 * @param source
	 * @param clazz
	 * @return
	 * @lastModifier
	 */
	public static <T> List<T> copy(List source,Class<T> clazz){
		List<T> result = new ArrayList<T>();
		if(source == null || source.isEmpty()){
			return result;
		}
		for(Object o : source){
			T e = copy(o,clazz);
			result.add(e);
		}
		
		return result;
	}
	
	public static void main(String[] args){
		Page p = new Page();
		p.setBegin(2);
		System.out.println(copy(p,Page.class).getBegin());
		Field field = getField(Page.class,"length");
		System.out.println(field.isAccessible());
		field.setAccessible(true);
		System.out.println(field.isAccessible());
		field = getField(Page.class,"length");
		System.out.println(field.isAccessible());
	}
}
