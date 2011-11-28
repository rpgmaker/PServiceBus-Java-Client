package pservicebus.runtime;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import pservicebus.exceptions.*;
import pservicebus.extensions.*;
import pservicebus.annotations.*;

public class ESBHelper {
	public static void throwExceptionIfNeeded(String result) throws ESBException {
		if(!StringExtension.isNullOrEmpty(result))
			throw new ESBException(result);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> toDict(Object message) throws IllegalAccessException, InvocationTargetException {
		Map<String, Object> dict = new HashMap<String, Object>();
		Class type = message.getClass();
		Field[] fields = type.getFields();
		for(Field field : fields){
			TopicHeader fieldAnnotation = field.getAnnotation(TopicHeader.class);
			if(fieldAnnotation != null) continue;
			dict.put(field.getName(), field.get(message));
		}
		return dict;
	}
}
