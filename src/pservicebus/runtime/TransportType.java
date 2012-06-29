package pservicebus.runtime;

import java.util.*;

public enum TransportType {
   MSMQ (0),
   RabbitMQ (1),
   Tcp (2),
   WebService (3),
   Email (4),
   Ftp (5),
   Redis (6),
   Http (7),
   BasicHttp (8),
   MSSQL (9);

   private static final Map<Integer, TransportType> lookup
		= new HashMap<Integer, TransportType>();

	static{
		for(TransportType type  : EnumSet.allOf(TransportType.class)){
			lookup.put(type.getCode(), type);
		}
	}

	private int code;

	private TransportType(int code){
		this.code = code;
	}

	public static TransportType get(int code){
		return lookup.get(code);
	}

	public int getCode(){
		return code;
	}
}