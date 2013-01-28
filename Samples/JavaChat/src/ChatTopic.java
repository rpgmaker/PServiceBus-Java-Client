

public class ChatTopic {
	public String UserName, Message;
	public ChatTopic(){}
	public ChatTopic(String userName, String message){
		UserName = userName;
		Message = message;
	}
	public static ChatTopic New(String userName, String message){
		return new ChatTopic(userName, message);
	}
}
