package pservicebus.runtime;

import java.lang.reflect.*;
import java.lang.*;
import java.util.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import org.apache.commons.codec.binary.Base64;

public class ReflectionHelper {
	private static final SimpleDateFormat _dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
	private static Class _objectType = Object.class;

	@SuppressWarnings("unchecked")
	public static Object changeType(String value, Class type){
		Object obj = null;
		try{
			Method method = getMethod("valueOf", type);
			obj =
				type == String.class ? value :
				type == Date.class ? _dateFormat.parse(value) :
				type.isEnum() ? Enum.valueOf(type, value) :
				type == byte[].class ? Base64.decodeBase64(value) :
				type == UUID.class ? UUID.fromString(value) :
				type != _objectType && canInitType(type) ? createInstance(type) :
				method != null ? method.invoke(null, value) : value;
		}
		catch(IllegalAccessException ex){}
		catch(InvocationTargetException ex){}
		catch(ParseException ex){}
		return obj;
	}

	public static Boolean canInitType(Class<?> type){
		Boolean success = false;
		try{ success = type.getConstructor(new Class[]{}) != null || type.isArray(); }
		catch(NoSuchMethodException ex){}
		catch(SecurityException ex){}
		return success;
	}

	public static Object createInstance(Class<?> type){
		Object obj = null;
		try{
			obj = type.newInstance();
		}
		catch(InstantiationException ex){}
		catch(IllegalAccessException ex){}
		return obj;
	}

	public static Field getField(String name, Class<?> type){
		Field field = null;
		try{ field = type.getField(name); }
		catch(NoSuchFieldException ex){}
		return field;
	}

	public static Method getMethod(String name, Class<?> type){
		Method method = null;
		try{method = type.getDeclaredMethod(name, String.class);}
		catch(NoSuchMethodException ex){}
		return method;
	}
}
