package pservicebus.runtime.transports;

import pservicebus.interfaces.*;
import pservicebus.transports.*;
import pservicebus.runtime.messages.*;

public class TcpTransportHandler implements ITransportHandler<TcpTransport> {
	private int _interval;
	private int _batchSize;
	public int getInterval(){ return _interval; }
	public void setInterval(int interval){ _interval = interval; }
	public int getBatchSize(){ return _batchSize; }
	public void setBatchSize(int batchSize){ _batchSize = batchSize; }

	@SuppressWarnings("unchecked")
	public <TMessage> IMessageHandler<TMessage> createHandler(Class<TMessage> messageType, ITransport transport){
		TcpTransport tcpTransport = (TcpTransport)transport;
		return MessageHandlerBase.<TMessage, TcpTransport>create(messageType, this, new TcpProvider(), String.format("%s:%d", tcpTransport.IPAddress, tcpTransport.Port));
	}
}
