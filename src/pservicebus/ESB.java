package pservicebus;


import java.util.*;
import java.io.*;
import pservicebus.*;
import pservicebus.exceptions.*;
import pservicebus.runtime.*;
import pservicebus.extensions.*;

public class ESB {
	private static String _userName;
	private static String _password;
	private static Boolean _throwException;
	private static String _endpointAddress;

	static{
		_endpointAddress = "http://localhost:8087/ESBRestService";
		_userName = StringExtension.empty;
		_password = StringExtension.empty;
		_throwException = false;
	}

	public static String getUserName(){
		return _userName;
	}

	public static String getPassword(){
		return _password;
	}

	public static Boolean getThrowException(){
		return _throwException;
	}

	public static String getEndpointAddress(){
		return _endpointAddress;
	}

	public static void reThrowException(Boolean reThrowException){
		_throwException = reThrowException;
	}

	public static void connect(String address){
		_endpointAddress = address;
	}

	public static void authenticate(String userName, String password){
		_userName = userName;
		_password = password;
	}

	@SuppressWarnings("unchecked")
	public static List<Topic> getTopics() throws ESBException, TopicNotRegisteredException, IOException{
		List<Topic> topics = new ArrayList<Topic>();
		String json = RestHelper.invoke("GetTopics", null);
		List<Map<String, Object>> data = RestHelper.<List<Map<String, Object>>>fromJson(json);
		if(data != null){
			for(Map<String, Object> topicDict : data)
				topics.add(Topic.dictionaryToTopic(topicDict));
		}else throw new ESBException(json);
		return topics;
	}
}
