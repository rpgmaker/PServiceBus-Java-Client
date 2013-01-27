package psb;

import java.util.Map;

public class RestHandler implements Runnable {

	private String methodName;
	private Map<String, Object> value;
	private Action<String> callback;
	
	public RestHandler(String methodName, Map<String, Object> value, Action<String> callback){
		this.methodName = methodName;
		this.value = value;
		this.callback = callback;
	}
	
	public void run(){
		String result = RestHelper.postRequest(methodName, value);
        if(callback != null)
        	callback.execute(result);
	}
}
