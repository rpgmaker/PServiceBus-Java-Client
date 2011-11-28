package pservicebus.androidchatapp;

import java.util.*;

import pservicebus.annotations.*;

public class ChatTopic implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	public String UserName, Message;
	@TopicHeader
	public Map<String, String> Headers;
	public ChatTopic(){}
	public ChatTopic(String userName, String message){
		UserName = userName;
		Message = message;
	}
}