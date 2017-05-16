import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * fastjson工具类
 * 
 * @author lsf
 * 
 */
public class FastJsonUtils {

	private static Logger logger = LoggerFactory.getLogger(FastJsonUtils.class);

	/**
	 * 功能描述：把JSON数据转换成普通字符串列表
	 * 
	 * @param jsonData
	 *            JSON数据
	 * @return
	 * @throws Exception
	 * @author lsf
	 */
	public static List<String> getStringList(String jsonData) throws Exception {
		return JSON.parseArray(jsonData, String.class);
	}

	/**
	 * 功能描述：把JSON数据转换成指定的java对象
	 * 
	 * @param jsonData
	 *            JSON数据
	 * @param clazz
	 *            指定的java对象
	 * @return
	 * @throws Exception
	 * @author lsf
	 */
	public static <T> T getSingleBean(String jsonData, Class<T> clazz) throws Exception {
		return JSON.parseObject(jsonData, clazz);
	}

	/**
	 * 功能描述：把JSON数据转换成指定的java对象
	 * 
	 * @param jsonData
	 *            JSON数据
	 * @param clazz
	 *            指定的java对象
	 * @return
	 * @author zhangxh
	 */
	public static <T> T getBean(String jsonData, Class<T> clazz) {
		try {
			return JSON.parseObject(jsonData, clazz);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 功能描述：把JSON数据转换成指定的java对象列表
	 * 
	 * @param jsonData
	 *            JSON数据
	 * @param clazz
	 *            指定的java对象
	 * @return
	 * @throws Exception
	 * @author lsf
	 */
	public static <T> List<T> getBeanList(String jsonData, Class<T> clazz) throws Exception {
		return JSON.parseArray(jsonData, clazz);
	}

	/**
	 * 功能描述：把JSON数据转换成较为复杂的java对象列表
	 * 
	 * @param jsonData
	 *            JSON数据
	 * @return
	 * @throws Exception
	 * @author lsf
	 */
	public static List<Map<String, Object>> getBeanMapList(String jsonData) throws Exception {
		return JSON.parseObject(jsonData, new TypeReference<List<Map<String, Object>>>() {
		});
	}

	/**
	 * 功能描述：把javaBean 转换成字符串
	 * 
	 * @param Object
	 *            javabean
	 * @return
	 * @author zhangxh
	 */
	public static String toJsonString(Object obj) {
		if (obj == null)
			return "";
		return JSON.toJSONString(obj);
	}

	/**
	 * 将网络请求下来的数据用fastjson处理空的情况，并将时间戳转化为标准时间格式
	 * 
	 * @param result
	 * @return
	 */
	public static String dealResponseResult(String result) {
		result = JSONObject.toJSONString(result, SerializerFeature.WriteClassName, SerializerFeature.WriteMapNullValue,
				SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullListAsEmpty,
				SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullStringAsEmpty,
				SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteEnumUsingToString,
				SerializerFeature.WriteSlashAsSpecial, SerializerFeature.WriteTabAsSpecial);
		return result;
	}

	/**
	 * 将java 字符串转化为指定类型的javabean对象
	 * 
	 * @param str
	 * @param clazz
	 * @return
	 */
	public static <T> T JsonStringToBean(String str, Class<T> clazz) {
		try {
			Object obj = JSON.parse(str);
			return JSON.parseObject(obj.toString(), clazz);
		} catch (Exception ex) {
			logger.error("转换异常！{}", ex);
			return JSON.parseObject(str, clazz);
		}
	}
	
	/**
	 * 将java 字符串转化为指定类型的java List对象
	 * 
	 * @param str
	 * @param clazz
	 * @return
	 */
	public static <T> List<T> JsonStringToBeanList(String str, Class<T> clazz) {
		try {
			Object obj = JSON.parse(str);
			return JSON.parseArray(obj.toString(), clazz);
		} catch (Exception ex) {
			logger.error("转换异常！{}", ex);
			return JSON.parseArray(str, clazz);
		}
	}
	
}
