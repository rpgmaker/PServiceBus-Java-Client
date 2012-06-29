package pservicebus.runtime;

import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;
import flexjson.*;

import pservicebus.*;
import pservicebus.flexjson.transformers.*;
import pservicebus.extensions.*;


public class RestHelper {
	public final static JSONSerializer _serializer =
		new JSONSerializer()
			.transform(new TransportFormatTransformer(), TransportFormat.class)
			.transform(new MSSQLCommandTypeTransformer(), pservicebus.runtime.mssql.MSSQLCommandType.class);

	public static <T> T fromJson(String json){
		return new JSONDeserializer<T>().deserialize(json);
	}

	@SuppressWarnings("unchecked")
	public static <T> T fromJson(String json, Class<?> type){
		return (T)new JSONDeserializer().deserialize(json, type);
	}

	public static Map<String, Object> fromJsonToMap(String json){
		return RestHelper.<Map<String, Object>>fromJson(json);
	}

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
			ESB.getEndpointAddress(), methodName,
			ESB.getThrowException(), ESB.getUserName(), ESB.getPassword());
		StringBuilder sb = new StringBuilder();
		if(value != null){
			for(Map.Entry<String, Object> entry : value.entrySet()){
				sb.append(String.format("&%s=%s",
				 entry.getKey(), URLEncoder.encode(toJson(entry.getValue()),"UTF-8") ));
			}
		}
		address += sb.toString();
		URL url = new URL(address);
		URLConnection connection = url.openConnection();
		BufferedReader buffer = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		String text;
		StringBuilder bufferSb = new StringBuilder();
		while((text = buffer.readLine()) != null){
			bufferSb.append(text);
		}
		buffer.close();

		result = bufferSb.toString();
		result = !StringExtension.isNullOrEmpty(result) ?
			result.substring(1, Math.max(result.length() - 1, 0)).replaceAll("\\\\\"","\"") : result;
		return result;
	}
}
