package psb;

public class MessageHandler<T> implements Runnable {

	private String data;
	private Class<T> type;
	private Action<T> callback;
	
	public MessageHandler(String data, Class<T> type, Action<T> callback){
		this.data = data;
		this.type = type;
		this.callback = callback;
	}
	
	public void run(){
		callback.execute(RestHelper.<T>fromJson(data, type));
	}
}
