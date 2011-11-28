package pservicebus.runtime.transports;

import java.util.*;
import pservicebus.interfaces.*;

public class TransportHandlers {
	private static List<IMessageHandler<?>> handlers;

	static{
		handlers = new ArrayList<IMessageHandler<?>>();
	}

	public static void addHandler(IMessageHandler<?> handler){
		handlers.add(handler);
	}

	public static void shutdown(){
		for(IMessageHandler<?> handler : handlers)
			shutdown(handler);
		handlers.clear();
	}

	public static void shutdown(IMessageHandler<?> handler){
		if(handler.getIsRunning()) handler.stop();
	}
}
