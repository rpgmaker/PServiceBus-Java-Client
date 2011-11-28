package pservicebus.runtime;

import java.lang.reflect.*;

public class Generic<T> {
	private final Class<T> type;

	@SuppressWarnings("unchecked")
	public Generic(){
		type = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}
	public Class<T> getType(){
		return type;
	}
}
