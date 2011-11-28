import java.util.*;

import pservicebus.runtime.*;
import pservicebus.annotations.*;

public class ChatTopic {
	public String UserName, Message;
	//public ArrayList<String> Strings;
	//public TData Data;
	@TopicHeader
	public HashMap<String, String> Headers;
	public ChatTopic(){}
	public ChatTopic(String userName, String message){
		UserName = userName;
		Message = message;
	}
}
