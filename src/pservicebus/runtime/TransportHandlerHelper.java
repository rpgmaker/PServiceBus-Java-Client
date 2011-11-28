package pservicebus.runtime;

import java.util.*;
import pservicebus.runtime.transports.*;

public class TransportHandlerHelper {

	private static final Map<Integer, Class<?>> lookup
		= new HashMap<Integer, Class<?>>();

	static{
		buildTransportHandlerLookup();
	}

	private static void buildTransportHandlerLookup(){
		lookup.put(TransportType.Tcp.getCode(), TcpTransportHandler.class);
		lookup.put(TransportType.RabbitMQ.getCode(), RabbitMQTransportHandler.class);
	}

	public static Class<?> getTransportHandlerType(int typeID){
		return lookup.get(typeID);
	}
}
