package pservicebus.runtime;

import java.util.*;

public enum TransportFormat {
	Xml(0),
	Json(1),
	Text(2),
	Csv(3);

	private static final Map<Integer, TransportFormat> lookup
		= new HashMap<Integer, TransportFormat>();

	static{
		for(TransportFormat format : EnumSet.allOf(TransportFormat.class)){
			lookup.put(format.getCode(), format);
		}
	}

	private int code;

	private TransportFormat(int code){
		this.code = code;
	}

	public static TransportFormat get(int code){
		return lookup.get(code);
	}

	public int getCode(){
		return code;
	}
}
