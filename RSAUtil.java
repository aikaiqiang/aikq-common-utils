import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

public class RSAUtil {
	// 指定加密算法为RSA
	private static String ALGORITHM = "RSA";
	// 指定key的大小
	private static int KEYSIZE = 1024;

	public static final String KEY_ALGORITHM = "RSA";
	
	public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

	/**
	 * @Description 生成密钥对
	 * @author lsf
	 * @date 2017年4月26日 下午7:45:17
	 * @throws Exception
	 * @lastModifier
	 */
	public static void generateKeyPair() throws Exception {
		/** RSA算法要求有一个可信任的随机数源 */
		SecureRandom sr = new SecureRandom();
		/** 为RSA算法创建一个KeyPairGenerator对象 */
		KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM);
		/** 利用上面的随机数据源初始化这个KeyPairGenerator对象 */
		kpg.initialize(KEYSIZE, sr);
		/** 生成密匙对 */
		KeyPair kp = kpg.generateKeyPair();
		/** 得到公钥 */
		PublicKey publicKey = kp.getPublic();
		/** 得到私钥 */
		PrivateKey privateKey = kp.getPrivate();

		System.out.println("私钥：" + Base64.encodeBase64String(privateKey.getEncoded()));
		System.out.println("公钥：" + Base64.encodeBase64String(publicKey.getEncoded()));
	}

	/**
	 * @Description 产生签名
	 * @author lsf
	 * @date 2017年4月26日 下午7:44:35
	 * @param data
	 * @param privateKey
	 * @return
	 * @throws Exception
	 * @lastModifier
	 */
	public static String sign(byte[] data, String privateKey) throws Exception {
		// 解密由base64编码的私钥
		byte[] keyBytes = decryptBASE64(privateKey);

		// 构造PKCS8EncodedKeySpec对象
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);

		// KEY_ALGORITHM 指定的加密算法
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

		// 取私钥对象
		PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);

		// 用私钥对信息生成数字签名
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(priKey);
		signature.update(data);

		return encryptBASE64(signature.sign());
	}

	/**
	 * @Description 验证签名
	 * @author lsf
	 * @date 2017年4月26日 下午7:44:27
	 * @param data
	 * @param publicKey
	 * @param sign
	 * @return
	 * @throws Exception
	 * @lastModifier
	 */
	public static boolean verify(byte[] data, String publicKey, String sign) throws Exception {

		// 解密由base64编码的公钥
		byte[] keyBytes = decryptBASE64(publicKey);

		// 构造X509EncodedKeySpec对象
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);

		// KEY_ALGORITHM 指定的加密算法
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

		// 取公钥匙对象
		PublicKey pubKey = keyFactory.generatePublic(keySpec);

		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(pubKey);
		signature.update(data);

		// 验证签名是否正常
		return signature.verify(decryptBASE64(sign));
	}

	/**
	 * BASE64解密
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptBASE64(String key) throws Exception {
		return Base64.decodeBase64(key);
	}

	/**
	 * BASE64加密
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String encryptBASE64(byte[] key) throws Exception {
		return Base64.encodeBase64String(key);
	}

	/**
	 * @Description 加密
	 * @author lsf
	 * @date 2017年4月26日 下午7:43:54
	 * @param privateKeyStr
	 * @param source
	 * @return
	 * @throws Exception
	 * @lastModifier
	 */
	public static String encrypt(String privateKeyStr, String source) throws Exception {
		// 转换私钥
		PrivateKey privateKey = convertPrivateKey(privateKeyStr);
		// 得到Cipher对象来实现对源数据的RSA加密
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		int MaxBlockSize = KEYSIZE / 8;
		String[] datas = splitString(source, MaxBlockSize - 11);
		String encryptText = "";
		for (String s : datas) {
			encryptText += bcd2Str(cipher.doFinal(s.getBytes()));
		}
		return encryptText;

	}

	public static String[] splitString(String string, int len) {
		int x = string.length() / len;
		int y = string.length() % len;
		int z = 0;
		if (y != 0) {
			z = 1;
		}
		String[] strings = new String[x + z];
		String str = "";
		for (int i = 0; i < x + z; i++) {
			if (i == x + z - 1 && y != 0) {
				str = string.substring(i * len, i * len + y);
			} else {
				str = string.substring(i * len, i * len + len);
			}
			strings[i] = str;
		}
		return strings;
	}

	public static String bcd2Str(byte[] bytes) {
		char temp[] = new char[bytes.length * 2], val;

		for (int i = 0; i < bytes.length; i++) {
			val = (char) (((bytes[i] & 0xf0) >> 4) & 0x0f);
			temp[i * 2] = (char) (val > 9 ? val + 'A' - 10 : val + '0');

			val = (char) (bytes[i] & 0x0f);
			temp[i * 2 + 1] = (char) (val > 9 ? val + 'A' - 10 : val + '0');
		}
		return new String(temp);
	}

	/**
	 * @Description 解密
	 * @author lsf
	 * @date 2017年4月26日 下午7:41:23
	 * @param publicKeyStr
	 * @param encryptText
	 * @return
	 * @throws Exception
	 * @lastModifier
	 */
	public static String decrypt(String publicKeyStr, String encryptText) throws Exception {
		// 转换公钥
		PublicKey publicKey = convertPublicKey(publicKeyStr);
		// 得到Cipher对象对已用公钥加密的数据进行RSA解密
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
		int key_len = KEYSIZE / 8;
		byte[] bytes = encryptText.getBytes();
		byte[] bcd = ASCII_To_BCD(bytes, bytes.length);
		System.err.println(bcd.length);
		String ming = "";
		byte[][] arrays = splitArray(bcd, key_len);
		for (byte[] arr : arrays) {
			ming += new String(cipher.doFinal(arr));
		}
		return ming;
	}

	public static byte[] ASCII_To_BCD(byte[] ascii, int asc_len) {
		byte[] bcd = new byte[asc_len / 2];
		int j = 0;
		for (int i = 0; i < (asc_len + 1) / 2; i++) {
			bcd[i] = ASC_To_BCD(ascii[j++]);
			bcd[i] = (byte) (((j >= asc_len) ? 0x00 : ASC_To_BCD(ascii[j++])) + (bcd[i] << 4));
		}
		return bcd;
	}

	public static byte ASC_To_BCD(byte asc) {
		byte bcd;

		if ((asc >= '0') && (asc <= '9'))
			bcd = (byte) (asc - '0');
		else if ((asc >= 'A') && (asc <= 'F'))
			bcd = (byte) (asc - 'A' + 10);
		else if ((asc >= 'a') && (asc <= 'f'))
			bcd = (byte) (asc - 'a' + 10);
		else
			bcd = (byte) (asc - 48);
		return bcd;
	}

	public static byte[][] splitArray(byte[] data, int len) {
		int x = data.length / len;
		int y = data.length % len;
		int z = 0;
		if (y != 0) {
			z = 1;
		}
		byte[][] arrays = new byte[x + z][];
		byte[] arr;
		for (int i = 0; i < x + z; i++) {
			arr = new byte[len];
			if (i == x + z - 1 && y != 0) {
				System.arraycopy(data, i * len, arr, 0, y);
			} else {
				System.arraycopy(data, i * len, arr, 0, len);
			}
			arrays[i] = arr;
		}
		return arrays;
	}

	/**
	 * 转换为公钥对象，X509EncodedKeySpec 用于构建公钥的规范
	 * 
	 * @param keyBytes
	 * @return
	 */
	public static PublicKey convertPublicKey(byte[] keyBytes) {
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
		try {
			KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
			PublicKey publicKey = factory.generatePublic(x509EncodedKeySpec);
			return publicKey;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 转换为公钥对象，X509EncodedKeySpec 用于构建公钥的规范
	 * 
	 * @param keyString
	 * @return
	 */
	public static PublicKey convertPublicKey(String keyString) {
		byte[] keyBytes = Base64.decodeBase64(keyString.getBytes());
		return convertPublicKey(keyBytes);
	}

	/**
	 * 转换为私钥对象，PKCS8EncodedKeySpec 用于构建私钥的规范
	 * 
	 * @param keyBytes
	 * @return
	 */
	public static PrivateKey convertPrivateKey(byte[] keyBytes) {
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
		try {
			KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
			PrivateKey privateKey = factory.generatePrivate(pkcs8EncodedKeySpec);
			return privateKey;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 转换为私钥对象，PKCS8EncodedKeySpec 用于构建私钥的规范
	 * 
	 * @param keyString
	 * @return
	 */
	public static PrivateKey convertPrivateKey(String keyString) {
		byte[] keyBytes = Base64.decodeBase64(keyString.getBytes());
		return convertPrivateKey(keyBytes);
	}

	public static void main(String[] args) throws Exception {
		// 生成公钥私钥
		// generateKeyPair();

		String pri_key = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJhOg4uRDDi72g6Ygs9/WWSmXNZElskjiB9TRlo+QE/P4tAhGG6+cG6Xcqn/Hjun1yzlJt4pqv34oMNouWADhIhYrYa5a4wLCC+BgFXbRJkbl5LqVcq7QuPrc+58SstSSLmpRyrQztYMWE3dLCzlpzw5nkODY85MRpHy7kyvKIodAgMBAAECgYBxMuGYKK0BubS+5mFK4SbKpM8ZPY8oXW09gwIl7mCUMTktYXusS4R63bsOWBvbUMqfho0Vz7hT3Kd3tO0aTGMCXzoXff5xvTIZSMwLTBprr+3wawy0rHKfy67bnQDKpeuhQ8sxBNPrJ9Pv7bKCTpH+urKSpU0NVU/kcs3BMvxOIQJBAOK6tepEib8dZBr9BhCap2ZY05wcTYbQ5kMn9BLojXYLQHGlTptGKh0woqhwGc++I8R1hDP9s+ZB0OoxOrD2ZwkCQQCr+Cw17Y9vYNRvf5sdmFVasPpn70/CDFAQsU/Sozg/rTLAe6xZwFGvQsobLzS4vxHCHJASk4D43fEiy5YmIZt1AkBXao2BCXPij75s+WlBNZ+dQlo0MmVhuWFOOzVLpQYFoUjziDzKeT77iijssGwDQNghgv253fNir7WQ1fI/EIGhAkBUA0UN/4XQYtzFr/CGz9H7IXTj69zqLvu6e/VEMWscaK/fq0uy+Squ1ZFJIJHAI9A4JZ4ZBSi/7CWO2yj6bfa1AkBPnKsB293OqnqnFo3VvhxOMJ1rhEW9T8Y25GHoj/wq/crPROr1CvoSn/x9TktICC4uLcjXqzgtTxHi5r0l41nJ";
		String pub_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCYToOLkQw4u9oOmILPf1lkplzWRJbJI4gfU0ZaPkBPz+LQIRhuvnBul3Kp/x47p9cs5SbeKar9+KDDaLlgA4SIWK2GuWuMCwgvgYBV20SZG5eS6lXKu0Lj63PufErLUki5qUcq0M7WDFhN3Sws5ac8OZ5Dg2POTEaR8u5MryiKHQIDAQAB";

		String source = "asdfasdfasdfasdfasdfasdfasfdasdfqwerqwerqwerqwrqwerqwerqwerqwerqwerqwerzxcvzxcvzxcvzxcvzxcvzxcvzcxvxzyuyuioyuioyuioyuioyuioyuioyuionhjklhkhjkljklhjklhjklhjklhjklnm,nm.n.nm,.nm,.,mn.nm.,nm";// 要加密的字符串

		String encryptText = RSAUtil.encrypt(pri_key, source);// 生成的密文
		System.out.println("加密后的密文是：" + encryptText);
		String target = RSAUtil.decrypt(pub_key, encryptText);// 解密密文
		System.out.println("解密之后的明文是：" + target);
		byte[] bytes = source.getBytes();
		// 产生签名
		String sign = RSAUtil.sign(bytes, pri_key);
		System.err.println("签名:" + sign);

		// 验证签名
		boolean status = RSAUtil.verify(bytes, pub_key, sign);
		System.err.println("状态:" + status);

	}
}
