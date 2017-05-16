import java.util.regex.Pattern;

public class IntUtil {
	
	public static boolean isZero(Integer value)
	{
		if(toInt(value) == 0){
			return true;
		}
		return false;
	}
	
	public static int toInt(Integer value)
	{
		if(value == null){
			return 0;
		}
		return value;
	}

	public static Integer toInteger(Integer value){
		if(value == null){
			return 0;
		}
		return value;
	}
	
	public static Integer toInteger(String value){
		if(value == null){
			return null;
		}
		return toInt(value);
	}
	
	public static int toInt(Object value)
	{
		if(value == null || "".equals(value)){
			return 0;
		}
		try{
			return Integer.valueOf(value.toString());
		}catch(Exception e){
			return 0;
		}
	}
	
	public static boolean isNotZero(Integer value)
	{
		return !isZero(value);
	}
	
	public static int returnInt(Double value){
		
		if(value == null || "".equals(value)){
			return 0;
		}
		
		return (int) Math.floor(value);
	}
	
	public static int returnInt(double value){
		
		return (int) Math.floor(value);
	}
	
	public static boolean isInteger(String str) {    
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");    
		return pattern.matcher(str).matches();    
	} 
	
	/**判断两个值是否相等*/
	public static boolean compareInteger(Integer value1,Integer value2){
		
		if(value1 == null || "".equals(value1)){
			value1 = 0;
		}
		if(value2 == null || "".equals(value2)){
			value2 = 0;
		}
		
		return value1.equals(value2);
	}
	
	public static void main(String[] args) {
		System.out.println(returnInt(5.5));
		
		System.out.println(isInteger(5.5 + ""));
	}
	
}
