import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.lsmy.cloud.common.vo.HttpResVO;


/**
 * @Description  HTTP请求处理帮助类
 * @author xhz
 * @date 2017年5月12日 上午10:02:57
 */
public class HttpUtils {

	private static final Log logger = LogFactory.getLog(HttpUtils.class);

	/**
	 * 默认超时时间的GET请求
	 * @param url
	 * @param map
	 * @return
	 */
	public static HttpResVO doHttpGet(String url){
		return doHttpGet(url,15000,15000);
	}
	/**
	 * 默认超时时间的POST请求
	 * @param url
	 * @param param
	 * @return
	 */
	public static HttpResVO doHttpPost(String url, String param ) {
		return doHttpPost( url, param, 15000, 15000);
	}

	/**
	 * 发起HTTP GET请求
	 * @param url
	 * @param connectTimeout
	 * @param readTimeout
	 * @return
	 */
	public static HttpResVO doHttpGet(String url, int connectTimeout, int readTimeout){
		if(url.endsWith("/")){
			url = url.substring(0, url.length() - 1);
		}
		StringBuffer requestHeader = new StringBuffer();
		StringBuffer responseHeader = new StringBuffer();
		String       response = null;
		long         period  = 0;
		boolean      isExcep = false;

		Header[] reqHd = null;
		Header[] respHd = null;

		HttpClientParams params = new HttpClientParams();
		HttpClient client = new HttpClient();
		logger.info("GET URL: "+url+" 连接超时时间："+connectTimeout+" 读取数据超时时间："+readTimeout);

		HttpMethod method = new GetMethod(url);

		client.setParams(params);

		HttpConnectionManagerParams managerParams = client.getHttpConnectionManager().getParams();

		// 设置连接超时时间(单位毫秒)
		managerParams.setConnectionTimeout(connectTimeout);
		// 设置读数据超时时间(单位毫秒)
		managerParams.setSoTimeout(readTimeout);
		long start = 0;
		int code = 200;
		try {
			 start = System.currentTimeMillis();

			client.executeMethod(method);

			reqHd = method.getRequestHeaders();

			respHd = method.getResponseHeaders();

			InputStream resStream = method.getResponseBodyAsStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(resStream));
			StringBuffer resBuffer = new StringBuffer();
			String resTemp = "";
			while((resTemp = br.readLine()) != null){
	           resBuffer.append(resTemp);
			}
			response = resBuffer.toString();

			period = System.currentTimeMillis()-start;
			code = method.getStatusCode();
		}catch(SocketTimeoutException e){
			response= "网络连接超时 "+e.getMessage()+" 设置的超时时间为："+connectTimeout+"毫秒";
			period  = System.currentTimeMillis()-start;
			isExcep = false;
			//经常出现这种异常，需要做分布式的检查，同时需要把报警逻辑拆分到另一个进程做处理,现在简单做一个sleep操作
			//注意：sleep容易引发任务执行不完的异常
			try{
				Thread.sleep(5000);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}catch(ConnectTimeoutException e){
			response= "连接超时 "+e.getMessage()+" 设置的超时时间为："+connectTimeout+"毫秒";
			period  = System.currentTimeMillis()-start;
			isExcep = false;
		}catch(UnknownHostException e){
			response= "不能解析的域名："+e.getMessage();
			period  = System.currentTimeMillis()-start;
			isExcep = true;
			logger.info("URI exception : "+e.getMessage(), e);
		}catch(ConnectException e){
			response= "拒绝连接的域名 "+e.getMessage();
			period  = System.currentTimeMillis()-start;
			isExcep = true;
			logger.info("URI exception : "+e.getMessage(), e);
		}catch(Exception e) {
			response= e.getClass().getSimpleName()+" "+e.getMessage()+" 设置的超时时间为："+connectTimeout+"毫秒";
			period  = System.currentTimeMillis()-start;
			isExcep = true;
			logger.info("URI exception : "+e.getMessage(), e);
		}finally {
			method.releaseConnection();
		}

		if(reqHd!=null){
			for(int i=0;i<reqHd.length;i++){
				requestHeader.append(reqHd[i].toString()+"\n");
			}
		}else{
			requestHeader.append("");
		}

		if(respHd!=null){
			for(int i=0;i<respHd.length;i++){
				responseHeader.append(respHd[i].toString()+"\n");
			}
		}else{
			responseHeader.append("");
		}

		HttpResVO res = new HttpResVO();
		res.setURL(url);
		res.setReq_time(start);
		res.setResp_period(period);
		res.setExcep(isExcep);
		res.setRequestHeader(requestHeader.toString());
		res.setResponse(response);
		res.setResponseHeader(requestHeader.toString());
		res.setResponseCode(code);
		return res;
	}

	/**
	 * 发起HTTP post请求
	 * @param url
	 * @param param
	 * @param connectTimeout
	 * @param readTimeout
	 * @return
	 */
	public static HttpResVO doHttpPost(String url, String param, int connectTimeout, int readTimeout) {
		if(url.endsWith("/")){
			url = url.substring(0, url.length() - 1);
		}

		StringBuilder  sb = new StringBuilder();
		BufferedReader in = null;
		long           period  = 0;
		boolean        isExcep = false;

		String     respHd  = null;
		long       start   = 0;
		int code = 200;

		try {
			logger.info("get data url :"+url+"?"+param.toString()+" 连接超时时间："+connectTimeout+" 读取数据超时时间："+readTimeout);

		    start = (new Date()).getTime();
			URL postURL = new URL(url);
			HttpURLConnection con = (HttpURLConnection) postURL.openConnection();

			con.setUseCaches(false); // do not use cache
			con.setDoOutput(true); // use for output
			con.setDoInput(true); // use for Input
			con.setConnectTimeout(connectTimeout);
			con.setReadTimeout(readTimeout);
			con.setRequestMethod("POST"); // use the POST method to submit the

			PrintWriter out = new PrintWriter(con.getOutputStream());

			out.print(param.toString()); // send to server

			out.close(); // close outputstream

			in = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));

			String aline;
			while (null != (aline = in.readLine())) {
				sb.append(aline).append('\n');
			}
			code = con.getResponseCode();
			respHd = con.getResponseMessage();

			long end = (new Date()).getTime();
			period   = end-start;
		}catch(SocketTimeoutException e){
			sb.append("网络连接超时 "+e.getMessage()+" 设置的超时时间为："+connectTimeout+"毫秒");
			period  = System.currentTimeMillis()-start;
			isExcep = false;
			//经常出现这种异常，需要做分布式的检查，同时需要把报警逻辑拆分到另一个进程做处理,现在简单做一个sleep操作
			//注意：sleep容易引发任务执行不完的异常
			try{
				Thread.sleep(5000);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}catch(ConnectTimeoutException e){
			sb.append("连接超时 "+e.getMessage()+" 设置的超时时间为："+connectTimeout+"毫秒");
			period  = System.currentTimeMillis()-start;
			isExcep = false;
		}catch(UnknownHostException e){
			sb.append("不能解析的域名 "+e.getMessage());
			period  = System.currentTimeMillis()-start;
			isExcep = true;
			logger.info("URI exception : "+e.getMessage(), e);
		}catch(ConnectException e){
			sb.append("拒绝连接的域名 "+e.getMessage());
			period  = System.currentTimeMillis()-start;
			isExcep = true;
			logger.info("URI exception : "+e.getMessage(), e);
		}catch(Exception e) {
			sb.append(e.getClass().getSimpleName()+" "+e.getMessage()+" 设置的超时时间为："+connectTimeout+"毫秒");
			period  = connectTimeout + readTimeout;
			isExcep = true;
			logger.error(url, e);
		}finally {
			if (in != null) {
				try {
					in.close();
				}catch (IOException e) {
					logger.error(e);
				}
				in = null;
			}
		}

		HttpResVO res = new HttpResVO();
		res.setURL(url+"?"+param);
		res.setReq_time(start);
		res.setResp_period(period);
		res.setExcep(isExcep);
		res.setRequestHeader("method:POST");
		res.setResponse(sb.toString());
		res.setResponseHeader(respHd);
		res.setResponseCode(code);
		return res;
	}

	public static BufferedReader doGetForReader(String strurl, int connectTimeout, int readTimeout) throws SocketTimeoutException {
		String host = null;
		return doGetForReader(strurl, host, connectTimeout, readTimeout);
	}

	public static BufferedReader doGetForReader(String strurl, String host, int connectTimeout, int readTimeout) throws SocketTimeoutException {
		try {
			URL url = new URL(strurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(readTimeout);
			if (host != null) {
				logger.info("Host:" + host);
				conn.setRequestProperty("Host", host);
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			return in;
		}
		catch (SocketTimeoutException e) {
			throw e;
		}
		catch (MalformedURLException e) {
			logger.info(host+e.getMessage(), e);
		}
		catch (IOException e) {
			logger.info(host+e.getMessage(), e);
		}
		finally {

		}
		return null;
	}
	public static String doGet(String url,Map<String,String> map, int connectTimeout, int readTimeout){
		StringBuilder param = new StringBuilder();
		if (map != null) {
			Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				if (param.length() > 0) {
					param.append('&');
				}

				Entry<String, String> entry = iterator.next();
				String key = entry.getKey();
				String value = entry.getValue();
				try {
					 if(value!=null){
						 param.append(key).append('=').append(URLEncoder.encode(value, "UTF-8"));
					 }
				}

				catch (UnsupportedEncodingException e) {
					logger.info(e.getMessage(), e);
				}
			}
		}
		HttpClientParams params = new HttpClientParams();
		String response = null;
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpClient client = new HttpClient(connectionManager);
		logger.info("GET URL: "+url+"?"+param.toString());

		HttpMethod method = new GetMethod(url+"?"+param.toString());
		method.setRequestHeader("Connection", "close");
		client.setParams(params);

		HttpConnectionManagerParams managerParams = client.getHttpConnectionManager().getParams();

		// 设置连接超时时间(单位毫秒)
		managerParams.setConnectionTimeout(connectTimeout);
		// 设置读数据超时时间(单位毫秒)
		managerParams.setSoTimeout(readTimeout);

		try {
			client.executeMethod(method);

			// if (method.getStatusCode() == HttpStatus.SC_OK) {
//			response = method.getResponseBodyAsString();
			// }
			InputStream resStream = method.getResponseBodyAsStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(resStream,"utf-8"));
			StringBuffer resBuffer = new StringBuffer();
			String resTemp = "";
			while((resTemp = br.readLine()) != null){
	           resBuffer.append(resTemp);
			}
			response = resBuffer.toString();
		}
		catch (URIException e) {
			logger.info(e.getMessage(), e);
			return null;
		}
		catch (IOException e) {
			logger.info(e.getMessage(), e);
		}
		finally {
			method.releaseConnection();
			client.getHttpConnectionManager().closeIdleConnections(0);
		}
		return response;
	}
	/**
	 * 默认超时时间的请求
	 * @param url
	 * @param map
	 * @return
	 */
	public static String doGet(String url,Map<String,String> map){
		return doGet(url,map,15000,15000);
	}
	/**
	 * 超时时间的请求
	 * @param url
	 * @param map
	 * @param timeout
	 * @return
	 */
	public static String doGet(String url,Map<String,String> map,int timeout){
		return doGet(url,map,timeout,timeout);
	}
	/**
	 * 获取页面代码
	 *
	 * @param url
	 * @return
	 */
	public static String doGet(String url) {

		HttpClientParams params = new HttpClientParams();
		params.setParameter("name", "name");
		String response = null;

		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(url);
		client.setParams(params);

		HttpConnectionManagerParams managerParams = client.getHttpConnectionManager().getParams();
		// 设置连接超时时间(单位毫秒)
		managerParams.setConnectionTimeout(15000);
		// 设置读数据超时时间(单位毫秒)
		managerParams.setSoTimeout(15000);

		try {
			client.executeMethod(method);
			// if (method.getStatusCode() == HttpStatus.SC_OK) {
//			response = method.getResponseBodyAsString();
			// }
			InputStream resStream = method.getResponseBodyAsStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(resStream));
			StringBuffer resBuffer = new StringBuffer();
			String resTemp = "";
			while((resTemp = br.readLine()) != null){
	           resBuffer.append(resTemp);
			}
			response = resBuffer.toString();
		}
		catch (URIException e) {
			logger.info(e.getMessage(), e);
			return null;
		}
		catch (IOException e) {
			logger.info(e.getMessage(), e);
		}
		finally {
			method.releaseConnection();
		}
		return response;
	}

	/**
	 * 获取网页代码.
	 * <p>
	 *
	 * @param strurl
	 * @return 网页代码.
	 */
	public static String doGet1(String strurl) {
		StringBuilder sb = new StringBuilder();
		BufferedReader in = null;
		try {
			URL url = new URL(strurl);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(15000);
			conn.setReadTimeout(15000);
//			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			in = new BufferedReader(new InputStreamReader(url.openStream(),"UTF-8"));
			String str;
			while ((str = in.readLine()) != null) {
				sb.append(str).append('\n');
			}
			in.close();
		}
		catch (MalformedURLException e) {
			logger.info(e.getMessage(), e);
		}
		catch (IOException e) {
			logger.info(e.getMessage(), e);
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
					logger.info(e.getMessage(), e);
				}
				in = null;
			}
		}
		return sb.toString();
	}

	public static String doGet2(String strurl, int connectTimeout, int readTimeout) {
		StringBuilder sb = new StringBuilder();
		BufferedReader in = null;
		try {
			URL url = new URL(strurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(readTimeout);
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String str;
			while ((str = in.readLine()) != null) {
				sb.append(str).append('\n');
			}
			in.close();
		}
		catch (MalformedURLException e) {
			logger.info(e.getMessage(), e);
		}
		catch (IOException e) {
			logger.info(e.getMessage(), e);
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
					logger.info(e.getMessage(), e);
				}
				in = null;
			}
		}
		return sb.toString();
	}
	public static String doHttpClientPost(String url, Map<String, String> map){
		logger.info("get data url :"+url+"?"+map);
		DefaultHttpClient httpclient = new DefaultHttpClient();


        HttpPost httpost = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();

        Set<String> keySet = map.keySet();
        for(String key : keySet) {
            nvps.add(new BasicNameValuePair(key, map.get(key)));
        }

        try {
        	logger.info("set utf-8 form entity to httppost");
            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            HttpResponse response = httpclient.execute(httpost);
            HttpEntity entity = response.getEntity();
            String charset = EntityUtils.getContentCharSet(entity);
            logger.info(charset);
            String body = EntityUtils.toString(entity);
            httpclient.getConnectionManager().shutdown();
            return body;
        } catch (Exception e) {
            logger.error("生成HTTP POST请求异常.",e);
        }
		return null;
	}
	public static String doPost(String url, Map<String, String> map) {
		StringBuilder param = new StringBuilder();
		if (map != null) {
			Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				if (param.length() > 0) {
					param.append('&');
				}

				Entry<String, String> entry = iterator.next();
				String key = entry.getKey();
				String value = entry.getValue();
				try {
					 if(value!=null){
						 param.append(key).append('=').append(URLEncoder.encode(value, "UTF-8"));
					 }
				}

				catch (UnsupportedEncodingException e) {
					logger.info(e.getMessage(), e);
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		BufferedReader in = null;
		try {
			logger.info("get data url :"+url+"?"+param.toString());
			URL postURL = new URL(url);
			HttpURLConnection con = (HttpURLConnection) postURL.openConnection();

			con.setUseCaches(false); // do not use cache
			con.setDoOutput(true); // use for output
			con.setDoInput(true); // use for Input
			con.setConnectTimeout(150000);
			con.setReadTimeout(150000);
			con.setRequestMethod("POST"); // use the POST method to submit the

			PrintWriter out = new PrintWriter(con.getOutputStream());

			out.print(param.toString()); // send to server

			out.close(); // close outputstream

			in = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));

			String aline;
			while (null != (aline = in.readLine())) {
				sb.append(aline).append('\n');
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			logger.error(url+"|"+e.getMessage(), e);
			return "";
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
					logger.info(e.getMessage(), e);
				}
				in = null;
			}
		}
		// logger.info(url+"?"+param.toString());
		return sb.toString();
	}
	/**
	 * 设置超时时间
	 * @param url
	 * @param map
	 * @param timeout
	 * @return
	 */
	public static String doPost(String url, Map<String, String> map,int timeout) {
		StringBuilder param = new StringBuilder();
		if (map != null) {
			Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				if (param.length() > 0) {
					param.append('&');
				}

				Entry<String, String> entry = iterator.next();
				String key = entry.getKey();
				String value = entry.getValue();
				try {
				   if(value!=null){
					   param.append(key).append('=').append(URLEncoder.encode(value, "UTF-8"));
				   }
				}

				catch (UnsupportedEncodingException e) {
					logger.info(e.getMessage(), e);
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		BufferedReader in = null;
		try {
			logger.info("get data url :"+url+"?"+param.toString());
			URL postURL = new URL(url);
			HttpURLConnection con = (HttpURLConnection) postURL.openConnection();

			con.setUseCaches(false); // do not use cache
			con.setDoOutput(true); // use for output
			con.setDoInput(true); // use for Input
			con.setConnectTimeout(timeout);
			con.setReadTimeout(timeout);
			con.setRequestMethod("POST"); // use the POST method to submit the

			PrintWriter out = new PrintWriter(con.getOutputStream());

			out.print(param.toString()); // send to server

			out.close(); // close outputstream

			in = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));

			String aline;
			while (null != (aline = in.readLine())) {
				sb.append(aline).append('\n');
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			logger.error(url+"|"+e.getMessage(), e);
			return "";
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
					logger.info(e.getMessage(), e);
				}
				in = null;
			}
		}
		// logger.info(url+"?"+param.toString());
		return sb.toString();
	}
	/**
	 * 后端取累加图表数据，超时时间设置为10分钟
	 * @param url
	 * @param map
	 * @return
	 */
	public static String doGraphPost(String url, Map<String, String> map) {
		StringBuilder param = new StringBuilder();
		if (map != null) {
			Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				if (param.length() > 0) {
					param.append('&');
				}

				Entry<String, String> entry = iterator.next();
				String key = entry.getKey();
				String value = entry.getValue();
				try {
					if(value!=null){
						param.append(key).append('=').append(URLEncoder.encode(value, "UTF-8"));
					}
				}

				catch (UnsupportedEncodingException e) {
					logger.info(e.getMessage(), e);
				}
			}
		}
		// logger.info(param.toString());
		StringBuilder sb = new StringBuilder();
		BufferedReader in = null;
		try {
			logger.info("get data url :"+url+"?"+param.toString());
			URL postURL = new URL(url);
			HttpURLConnection con = (HttpURLConnection) postURL.openConnection();

			con.setUseCaches(false); // do not use cache
			con.setDoOutput(true); // use for output
			con.setDoInput(true); // use for Input
			con.setConnectTimeout(15000);
			con.setReadTimeout(600000);
			con.setRequestMethod("POST"); // use the POST method to submit the

			PrintWriter out = new PrintWriter(con.getOutputStream());

			out.print(param.toString()); // send to server

			out.close(); // close outputstream

			in = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));

			String aline;
			while (null != (aline = in.readLine())) {
				sb.append(aline).append('\n');
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			logger.error(url+"|"+e.getMessage(), e);
			return "";
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
					logger.info(e.getMessage(), e);
				}
				in = null;
			}
		}
		// logger.info(url+"?"+param.toString());
		return sb.toString();
	}
	public static String doPost(String url, Map<String, String> map,int connectionTimeout,int readTimeOut) {
		StringBuilder param = new StringBuilder();
		if (map != null) {
			Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				if (param.length() > 0) {
					param.append('&');
				}

				Entry<String, String> entry = iterator.next();
				String key = entry.getKey();
				String value = entry.getValue();
				try {
					if(value!=null){
						param.append(key).append('=').append(URLEncoder.encode(value, "UTF-8"));
					}
				}

				catch (UnsupportedEncodingException e) {
					logger.info(e.getMessage(), e);
				}
			}
		}
		// logger.info(param.toString());
		StringBuilder sb = new StringBuilder();
		BufferedReader in = null;
		PrintWriter out   = null;
		try {
			logger.info("get data url :"+url+"?"+param.toString());
			URL postURL = new URL(url);
			HttpURLConnection con = (HttpURLConnection) postURL.openConnection();

			con.setUseCaches(false); // do not use cache
			con.setDoOutput(true); // use for output
			con.setDoInput(true); // use for Input
			con.setConnectTimeout(connectionTimeout);
			con.setReadTimeout(readTimeOut);
			con.setRequestMethod("POST"); // use the POST method to submit the

			out = new PrintWriter(con.getOutputStream());

			out.print(param.toString()); // send to server

			out.close(); // close outputstream

			in = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));

			String aline;
			while (null != (aline = in.readLine())) {
				sb.append(aline).append('\n');
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			logger.error(url+"|"+e.getMessage(), e);
			return "";
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
					logger.info(e.getMessage(), e);
				}
				in = null;
			}
			if(out!=null){
				try{out.close();}catch(Exception e){e.printStackTrace();};
			}
		}
		// logger.info(url+"?"+param.toString());
		return sb.toString();
	}
	public static String doPost1(final String url, final Map<String, String> p) {

		StringBuilder param = new StringBuilder();
		if (p != null) {
			// Set<Entry<String, String>> set = p.entrySet();

			Iterator<Entry<String, String>> iterator = p.entrySet().iterator();
			while (iterator.hasNext()) {
				if (param.length() > 0) {
					param.append('&');
				}
				Entry<String, String> entry = iterator.next();
				String key = entry.getKey();
				String value = entry.getValue();
				try {
					if(value!=null){
						param.append(key).append('=').append(URLEncoder.encode(value, "UTF-8"));
					}
				}
				catch (UnsupportedEncodingException e) {
					logger.info(e.getMessage(), e);
				}
			}
		}

		// logger.info(param.toString());

		StringBuilder sb = new StringBuilder();
		BufferedReader in = null;
		try {
			URL postURL = new URL(url);
			HttpURLConnection con = (HttpURLConnection) postURL.openConnection();

			con.setUseCaches(false); // do not use cache
			con.setDoOutput(true); // use for output
			con.setDoInput(true); // use for Input
			con.setConnectTimeout(15000);
			con.setReadTimeout(15000);
			con.setRequestMethod("POST"); // use the POST method to submit the
			// form

			// con.setRequestProperty("referer", "http://bbs.163.com");
			PrintWriter out = new PrintWriter(con.getOutputStream());

			out.print(param.toString()); // send to server
			out.close(); // close outputstream

			in = new BufferedReader(new InputStreamReader(con.getInputStream()));

			String aline;
			while (null != (aline = in.readLine())) {
				sb.append(aline).append('\n');
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			logger.info(url+"|"+e.getMessage(), e);
			return "";
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
					logger.info(e.getMessage(), e);
				}
				in = null;
			}
		}
		// logger.info(url+"?"+param.toString());
		return sb.toString();
	}

	public static String doPost2(final String url, Object obj) {

		StringBuilder param = new StringBuilder();




		// logger.info(param.toString());

		StringBuilder sb = new StringBuilder();
		BufferedReader in = null;
		try {
			URL postURL = new URL(url);
			HttpURLConnection con = (HttpURLConnection) postURL.openConnection();

			con.setUseCaches(false); // do not use cache
			con.setDoOutput(true); // use for output
			con.setDoInput(true); // use for Input
			con.setConnectTimeout(15000);
			con.setReadTimeout(15000);
			con.setRequestMethod("POST"); // use the POST method to submit the
			// form

			// con.setRequestProperty("referer", "http://bbs.163.com");
			PrintWriter out = new PrintWriter(con.getOutputStream());
			String str = FastJsonUtils.toJsonString(obj);

			out.print(str); // send to server
			out.close(); // close outputstream

			in = new BufferedReader(new InputStreamReader(con.getInputStream()));

			String aline;
			while (null != (aline = in.readLine())) {
				sb.append(aline).append('\n');
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			logger.info(url+"|"+e.getMessage(), e);
			return "";
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
					logger.info(e.getMessage(), e);
				}
				in = null;
			}
		}

		System.out.println("#################################");
		// logger.info(url+"?"+param.toString());
		return sb.toString();
	}

	public static void main(String[] args) throws HttpException, IOException {
		Map params = new HashMap();
		params.put("appkey", "af2f2e5c-bc00-783383");
		params.put("secretkey", "ff55d7e395f5c2f8e0e25e89d17e146d");
		params.put("url", "http://baidu.com");
		params.put("uid", "47281");
		params.put("token", "123456789");

		String url = "http://hs.beidouecs.com:8184/set_config";

		String str = HttpUtils.doPost2(url,params);

		System.out.println(str);
	}

}
