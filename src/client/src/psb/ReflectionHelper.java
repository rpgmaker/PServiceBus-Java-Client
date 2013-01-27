package psb;

import java.lang.reflect.Method;

public class ReflectionHelper {
	public static Method getMethod(Class<?> type, String methodName){
		if(type == null) return null;
		Method method = null;
		try {
			method = type.getDeclaredMethod(methodName);
		} catch (NoSuchMethodException e) {} 
		catch (SecurityException e) {} 
		catch (IllegalArgumentException e) {} 
		
		if(method == null){
			Method[] methods = type.getMethods();
			for(Method meth : methods){
				if(meth.getName().equals(methodName))
					return meth;
			}
		}
		return method;
	}
}
