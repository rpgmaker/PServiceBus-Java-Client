package pservicebus.runtime;

import java.util.*;

public enum TransportType {
   MSMQ (0),
   RabbitMQ (1),
   RavenDB (2),
   Tcp (3),
   WebService (4),
   Email (5),
   Ftp (6),
   Redis (7),
   Http (8),
   BasicHttp (9),
   MSSQL (10);

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