package pservicebus;

import pservicebus.extensions.*;

public class ESB {
	private static String _userName;
	private static String _password;
	private static Boolean _throwException;
	private static String _endpointAddress;
	private static Topics _topics;

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

	public static void configWithAddress(String address){
		_endpointAddress = address;
	}

	public static void authenticate(String userName, String password){
		_userName = userName;
		_password = password;
	}

	public static Topics getTopics(){
		if(_topics == null) _topics = new Topics();
		return _topics;
	}
}
