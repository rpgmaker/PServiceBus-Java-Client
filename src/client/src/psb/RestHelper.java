package psb;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;


public final class RestHelper {
	    private final static Gson gson = new Gson();
	    private final static ExecutorService threadPool = 
	    		Executors.newCachedThreadPool();

		public static <T> T fromJson(String json, Class<T> type){
			return (T)gson.fromJson(json, type);
		}
	    
		@SuppressWarnings("rawtypes")
		public static String toJson(Object value){
			if(value == null) return "null";
			Class type = value.getClass();
			if(type == String.class || type.isPrimitive() ||
				type == Integer.class ||
				type == Double.class || type == Float.class ||
				type == Long.class || type == Short.class)
				return value.toString();
			return gson.toJson(value);//serializer.transform(new IterableTransformer(), Object[].class).exclude("*.class").serialize(value);
		}
		
		public static void invoke(final String methodName, final Map<String, Object> value){
			invoke(methodName, value, null);
		}
		
		public static void invoke(String methodName, Map<String, Object> value, Action<String> callback){
			if(LocalStorage.getIsAndroid()){
				threadPool.execute(new RestHandler(methodName, value, callback));
			}else{
				String result = postRequest(methodName, value);
	            if(result != null && callback != null)
	            	callback.execute(result);
			}
		}
		
		public static String postRequest(String methodName, Map<String, Object> value) {
			String result = StringExtension.empty;
			String address = String.format("%s%s?ReThrowException=%s&ESBUserName=%s&ESBPassword=%s&ConnectionID=%s",
				PSBClient.getEndpoint(), methodName,
				PSBClient.getThrowException(), PSBClient.getApikey(), PSBClient.getPasscode(), PSBClient.getUserName());
			
			String json = toJson(value);
			try{
								
				URL url = new URL(address);
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.setInstanceFollowRedirects(false);
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setUseCaches(false);
				
				
				DataOutputStream ds = new DataOutputStream(connection.getOutputStream());
				ds.write(json.getBytes());
				ds.flush();
				
				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	
				String text;
				StringBuilder bufferSb = new StringBuilder();
				while((text = br.readLine()) != null)
					bufferSb.append(text);
				
				result = bufferSb.toString();
				result = !StringExtension.isNullOrEmpty(result) ?
					result.substring(1, Math.max(result.length() - 1, 0)).replaceAll("\\\\\"","\"") : result;
				
				connection.disconnect();	
			}
			catch(UnsupportedEncodingException ex){}
			catch(IOException ex){}
			return result;
		}
}
