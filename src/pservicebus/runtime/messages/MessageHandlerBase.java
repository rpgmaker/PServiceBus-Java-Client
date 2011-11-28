package pservicebus.runtime.messages;

import java.util.*;
import java.lang.*;
import java.lang.reflect.*;
import pservicebus.interfaces.*;
import pservicebus.runtime.*;
import pservicebus.extensions.*;
import pservicebus.annotations.*;

public class MessageHandlerBase<TMessage> implements IMessageHandler<TMessage> {

	private Thread _thread;
	private static final String ESBTOPIC_HEADERS = "ESBTOPIC_HEADERS";
	private static final Class _dictionaryType = Map.class;
	private volatile Boolean _running;
	private volatile Boolean _completed;
	private volatile int _batchCounter;
	private int _batchSize;
	private String _endpoint;
	private int _interval;
	private Class<TMessage> _messageType;
	private ESBMessageAction<TMessage> _messageAction;
	private ESBMessageAction<Exception> _errorAction;
	private IObjectProvider _provider;
	protected void setObjectProvider(IObjectProvider provider){ _provider = provider; }
	protected void setMessageType(Class<TMessage> messageType) { _messageType = messageType; }
	public ESBMessageAction<TMessage> getMessageReceived(){ return _messageAction; }
	public ESBMessageAction<Exception> getErrorReceived(){ return _errorAction; }
	public void setMessageReceived(ESBMessageAction<TMessage> messageAction){ _messageAction = messageAction; }
	public void setErrorReceived(ESBMessageAction<Exception> errorAction){ _errorAction = errorAction; }
	public Boolean getIsRunning(){ return _running && !_completed; }
	public int getBatchSize(){ return _batchSize; }
	public void setBatchSize(int batchSize){ _batchSize = batchSize; }
	public String getEndpoint(){ return _endpoint; }
	public void setEndpoint(String endpoint){ _endpoint = endpoint; }
	public int getInterval(){ return _interval; }
	public void setInterval(int interval){ _interval = interval; }

	public static <TMessage, TTransport extends ITransport> MessageHandlerBase create(Class<TMessage> messageType, ITransportHandler<TTransport> transportHandler, IObjectProvider provider, String endpoint){
		provider.setEndpoint(endpoint);
		MessageHandlerBase<TMessage> handler = new MessageHandlerBase<TMessage>();
		handler.setBatchSize(transportHandler.getBatchSize());
		handler.setInterval(transportHandler.getInterval());
		handler.setObjectProvider(provider);
		handler.setMessageType(messageType);
		return handler;
	}

	public void init(){
	}

	public void shutdown(){
		if(_provider != null) _provider.dispose();
	}

	public void start(){
		_running = true;
		_completed = false;
		try{ init(); }catch(Exception ex){}
		_thread = new Thread(new Runnable(){
			public void run(){
				poll();
			}
		});
		_thread.start();
	}

	public void stop(){
		_running = false;
		shutdown();
	}

	private List<TMessage> getMessages(String data){
		data = updateHeaderProperty(data);
		TransportFormat format = TransportProvider.getMessageFormat(data);
		IDataTransformation transformation = TransportProvider.getTransformation(format);
		return transformation.<TMessage>transform(data, _messageType);
	}

	@SuppressWarnings("unchecked")
	private String updateHeaderProperty(String data){
		Field[] fields = _messageType.getFields();
		for(Field field : fields){
			TopicHeader fieldAnnotation = field.getAnnotation(TopicHeader.class);
			if(fieldAnnotation != null && _dictionaryType.isAssignableFrom(field.getType())){
				data = data.replaceAll(ESBTOPIC_HEADERS, field.getName());
				break;
			}
		}
		return data;
	}

	private void poll(){
		while(_running){
			_batchCounter = 0;
			Exception ex = null;
			try{
				Thread.sleep(_interval);
				if(_provider != null){
					_provider.process(new MessageProcessor(){
						public Boolean process(String value){
							if(value == null) return true;
							List<TMessage> messages = getMessages(value);
							for(TMessage message : messages)
								if(_messageAction != null)
									_messageAction.handle(message);
							return (++_batchCounter != _batchSize);
						}
					});
				}
			}
			catch(InterruptedException e){ ex = e; }
			catch(Exception e){ ex = e; }
			if(ex != null && _errorAction != null) _errorAction.handle(ex);
		}
		_completed = true;
		if(_thread == null) return;
		try{ _thread.interrupt(); }
		catch(Exception e){ }
		finally{ _thread = null; }
	}
}
