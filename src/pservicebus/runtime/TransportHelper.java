package pservicebus.runtime;

import java.util.*;
import pservicebus.transports.*;

public class TransportHelper {

	private static final Map<Class<?>, TransportType> lookup
		= new HashMap<Class<?>, TransportType>();

	static{
		buildTransportLookup();
	}

	private static void buildTransportLookup(){
		lookup.put(EmailTransport.class, TransportType.Email);
		lookup.put(FtpTransport.class, TransportType.Ftp);
		lookup.put(HttpTransport.class, TransportType.Http);
		lookup.put(RabbitMQTransport.class, TransportType.RabbitMQ);
		lookup.put(RedisTransport.class, TransportType.Redis);
		lookup.put(TcpTransport.class, TransportType.Tcp);
		lookup.put(WebServiceTransport.class, TransportType.WebService);
		lookup.put(BasicHttpTransport.class, TransportType.BasicHttp);
		lookup.put(MSSQLTransport.class, TransportType.MSSQL);
	}

	public static Class<?> getTransportType(int typeID){
		Class<?> type = null;
		for(Map.Entry<Class<?>, TransportType> entry : lookup.entrySet()){
			if(entry.getValue().getCode() == typeID){
				type = entry.getKey();
				break;
			}
		}
		return type;
	}

	public static int getTransportTypeID(Class<?> type){
		TransportType transportType = lookup.get(type);
		return transportType.getCode();
	}
}
