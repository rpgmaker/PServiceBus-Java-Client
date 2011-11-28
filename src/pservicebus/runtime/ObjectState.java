package pservicebus.runtime;

import java.util.*;

public enum ObjectState {
	Valid (0),
	InValid (1);

	private static final Map<Integer, ObjectState> lookup
		= new HashMap<Integer, ObjectState>();

	static{
		for(ObjectState state  : EnumSet.allOf(ObjectState.class)){
			lookup.put(state.getCode(), state);
		}
	}

	private int code;

	private ObjectState(int code){
		this.code = code;
	}

	public static ObjectState get(int code){
		return lookup.get(code);
	}

	public int getCode(){
		return code;
	}
}
