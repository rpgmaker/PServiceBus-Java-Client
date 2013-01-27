package psb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PSBContext {
	
	private static Object context;
	private static ClassLoader classLoader =
			LocalStorage.class.getClassLoader();
	
	public static void setContext(Object context){
		PSBContext.context = context;
	}
	
	public static Object getContext(){
		return PSBContext.context;
	}
	
	public static <T> void invoke(String data, Class<T> type, Action<T> callback){
		if(getIsAndroid()){
			Method runOnUiMethod = ReflectionHelper.getMethod(context.getClass(), "runOnUiThread");
			try {
				runOnUiMethod.invoke(context, new MessageHandler<T>(data, type, callback));
			} catch (InvocationTargetException e) {} 
			catch (SecurityException e) {} 
			catch (IllegalArgumentException e) {} 
			catch (IllegalAccessException e) { } 
		}else {
			callback.execute(RestHelper.<T>fromJson(data, type));
		}
	}
	
	public static Boolean getIsAndroid(){
		try{
			classLoader.loadClass("android.app.Activity");
			return true;
		}
		catch(Exception ex){
			return false;
		}
	}
}
