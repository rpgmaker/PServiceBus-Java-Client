package pservicebus.runtime.transports;

import pservicebus.interfaces.*;
import pservicebus.transports.*;
import pservicebus.runtime.messages.*;

public class RabbitMQTransportHandler implements ITransportHandler<RabbitMQTransport> {
	private int _interval;
	private int _batchSize;
	public int getInterval(){ return _interval; }
	public void setInterval(int interval){ _interval = interval; }
	public int getBatchSize(){ return _batchSize; }
	public void setBatchSize(int batchSize){ _batchSize = batchSize; }

	@SuppressWarnings("unchecked")
	public <TMessage> IMessageHandler<TMessage> createHandler(Class<TMessage> messageType, ITransport transport){
		RabbitMQTransport rabbitMQTransport = (RabbitMQTransport)transport;
		return MessageHandlerBase.<TMessage, RabbitMQTransport>create(messageType, this, new RabbitMQProvider(), rabbitMQTransport.Path);
	}
}
