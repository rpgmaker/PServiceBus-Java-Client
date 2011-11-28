import java.util.*;

import pservicebus.runtime.*;
import pservicebus.annotations.*;

public class ChatTopic {
	public String UserName, Message;
	@TopicHeader
	public Map<String, String> Headers;
	public ChatTopic(){}
	public ChatTopic(String userName, String message){
		UserName = userName;
		Message = message;
	}
}
