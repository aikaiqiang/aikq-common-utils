import org.apache.commons.codec.digest.DigestUtils;
/**
 * MD5工具类
 * 
 * @author lsf
 *
 */
public class MD5Util {

	/**
	 * MD5加密
	 * 
	 * @param plainText
	 * @return
	 */
	public static String md5Encode(String plainText) {
		return DigestUtils.md5Hex(plainText).toUpperCase();
	}

	public static void main(String[] args) {
		// 测试
		System.out.println(MD5Util.md5Encode("123"));
	}

}
