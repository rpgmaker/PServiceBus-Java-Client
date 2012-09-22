import java.io.*;
import java.net.*;
import java.util.*;

import flexjson.*;


public final class RestHelper {
	public final static JSONSerializer _serializer = new JSONSerializer();

		public static <T> T fromJson(String json){
			return new JSONDeserializer<T>().deserialize(json);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static <T> T fromJson(String json, Class<?> type){
			return (T)new JSONDeserializer().deserialize(json, type);
		}

		public static Map<String, Object> fromJsonToMap(String json){
			return RestHelper.<Map<String, Object>>fromJson(json);
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
			return _serializer.exclude("*.class").serialize(value);
		}

		
		public static String invoke(String methodName, Map<String, Object> value) throws IOException, UnsupportedEncodingException {
			String result;
			String address = String.format("%s/%s?ReThrowException=%s&ESBUserName=%s&ESBPassword=%s",
				PSBClient.getEndpoint(), methodName,
				PSBClient.getThrowException(), PSBClient.getApikey(), PSBClient.getPasscode());
			StringBuilder sb = new StringBuilder();
			if(value != null){
				for(Map.Entry<String, Object> entry : value.entrySet()){
					sb.append(String.format("&%s=%s",
					 entry.getKey(), URLEncoder.encode(toJson(entry.getValue()),"UTF-8") ));
				}
			}
			URL url = new URL(address);
			byte[] buffer = sb.toString().getBytes();
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setInstanceFollowRedirects(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("charset", "utf-8");
			connection.setUseCaches(false);
			
			DataOutputStream  ds = new DataOutputStream(connection.getOutputStream());
			ds.write(buffer);
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
			return result;
		}
}
