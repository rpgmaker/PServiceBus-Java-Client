package pservicebus.runtime.mssql;

import java.util.*;

public enum MSSQLCommandType {
	TableInsert (0),
	InvokeStoreProcedure (1);

	private static final Map<Integer, MSSQLCommandType> lookup
		= new HashMap<Integer, MSSQLCommandType>();

	static{
		for(MSSQLCommandType type  : EnumSet.allOf(MSSQLCommandType.class)){
			lookup.put(type.getCode(), type);
		}
	}

	private int code;

	private MSSQLCommandType(int code){
		this.code = code;
	}

	public static MSSQLCommandType get(int code){
		return lookup.get(code);
	}

	public int getCode(){
		return code;
	}
}

