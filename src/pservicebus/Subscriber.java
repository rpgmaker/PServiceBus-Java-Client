package pservicebus;

import java.util.regex.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import pservicebus.*;
import pservicebus.exceptions.*;
import pservicebus.runtime.*;
import pservicebus.extensions.*;
import pservicebus.interfaces.*;
import pservicebus.runtime.transports.*;
import pservicebus.transports.*;

public class Subscriber {
	private static final Pattern _topicPatternRegex = Pattern.compile("(\\*\\.)|(\\.\\*)|(\\*)");
	private List<ESBAction> _actions;
	private String _subscriberName;
	private ObjectState _state;
	private Map<String, Topic> _topics;
	private Long _subscriberID;
	private Boolean _isSubscriberDurable;
	private Map<String, Map<String, Object>> _transports;

	public String getSubscriberName(){ return _subscriberName; }

	public ObjectState getState(){ return _state; }

	public Map<String, Topic> getTopics() { return _topics; }

	public Long getSubscriberID() { return _subscriberID; }

	public Boolean getIsSubscriberDurable() { return _isSubscriberDurable; }

	private Subscriber(String name){
		this(name, true);
	}

	private Subscriber(String name, Boolean isNew){
		_subscriberName = name;
		_actions = new ArrayList<ESBAction>();
		_state = ObjectState.Valid;
		_topics = new HashMap<String, Topic>();
		_transports = new HashMap<String, Map<String, Object>>();
		_subscriberID = 0L;
		_isSubscriberDurable = false;
		if(isNew)
			_actions.add(new ESBAction(){
				public void execute() throws IOException, ESBException {
					String result = RestHelper.invoke("CreateSubscriber", HashBuilder.create().add("subscriber", _subscriberName).getHash());
					ESBHelper.throwExceptionIfNeeded(result);
				}
			});
	}

	protected Subscriber id(Long value){
		_subscriberID = value;
		return this;
	}

	protected Subscriber setState(ObjectState state){
		_state = state;
		return this;
	}

	private void validateObjectState() throws SubscriberNotExistException{
		if(_state == ObjectState.InValid) throw new SubscriberNotExistException(ExceptionConstants.subscriberExceptionStr(_subscriberName));
	}

	public Subscriber durable(Boolean value) throws SubscriberNotExistException{
		validateObjectState();
		_isSubscriberDurable = value;
		_actions.add(new ESBAction(){
			public void execute() throws IOException, ESBException{
				String result = RestHelper.invoke("ChangeSubscriberDurability",
					HashBuilder.create()
						.add("subscriber", _subscriberName)
						.add("durable", _isSubscriberDurable).getHash());
				ESBHelper.throwExceptionIfNeeded(result);
			}
		});
		return this;
	}

	public Subscriber subscribeTo(Topic topic) throws SubscriberNotExistException, IOException, TopicNotRegisteredException, ESBException{
		validateObjectState();
		Exception ex = null;
		String topicFilters = StringExtension.empty;
		final String topicName = topic.getTopicName();
		if(_topicPatternRegex.matcher(topicName).matches()){
			if(topic.getState() == ObjectState.InValid){
				topicFilters = topic.getTopicFilter();
				topic = Topic.create(topicName).filter(topicFilters).setState(ObjectState.Valid);
				try{ topic.register(); }
				catch(Exception e){ ex = e; }
			}
		}
		final String filters = topic.getTopicFilter();
		if(ex != null && ex instanceof TopicAlreadyExistException) topic = Topic.select(topicName);
		if(topic.getState() == ObjectState.InValid) throw new TopicNotRegisteredException(ExceptionConstants.topicExceptionStr(topicName));
		_topics.put(topic.getTopicName(), topic);
		_actions.add(new ESBAction(){
			public void execute() throws IOException, ESBException {
				String result = RestHelper.invoke("SubscribeTo",
					HashBuilder.create()
						.add("subscriber",_subscriberName)
						.add("topicName", topicName)
						.add("filter", filters).getHash());
				ESBHelper.throwExceptionIfNeeded(result);
			}
		});
		return this;
	}

	public <TMessage> IMessageHandler<TMessage> onMessageReceived(Class<TMessage> messageType, String transportName, ESBMessageAction<TMessage> messageAction, ESBMessageAction<Exception> errorAction){
		IMessageHandler<TMessage> messageHandler = null;
		try{
			messageHandler = onMessageReceived(messageType, transportName, messageAction, errorAction, 0, 0);
		}catch(ESBException ex){}
		return messageHandler;
	}

	public <TMessage> IMessageHandler<TMessage> onMessageReceived(Class<TMessage> messageType, String transportName, ESBMessageAction<TMessage> messageAction){
		return onMessageReceived(messageType, transportName, messageAction, null);
	}

	public <TMessage> IMessageHandler<TMessage> onMessageReceived(Class<TMessage> messageType, String transportName, ESBMessageAction<TMessage> messageAction, ESBMessageAction<Exception> errorAction, int interval){
		IMessageHandler<TMessage> messageHandler = null;
		try{
			messageHandler = onMessageReceived(messageType, transportName, messageAction, errorAction, interval, 0);
		}catch(ESBException ex){}
		return messageHandler;
	}

	@SuppressWarnings("unchecked")
	public <TMessage> IMessageHandler<TMessage> onMessageReceived(Class<TMessage> messageType, String transportName, ESBMessageAction<TMessage> messageAction,
		ESBMessageAction<Exception> errorAction, int interval, int batchSize) throws ESBException {
		IMessageHandler<TMessage> messageHandler = null;
		try{
			if(!_transports.containsKey(transportName))
				throw new ESBException(String.format("The specified transport[%s] does not exist for the current subscriber", transportName));

			Map<String, Object> transportInfo = _transports.get(transportName);
			Map<String, Object> transportTypeInfo = RestHelper.fromJsonToMap(RestHelper.invoke("GetSubscriberTransportTypeID",
				HashBuilder.create().add("subscriberName",_subscriberName).add("transportName", transportName).getHash()));
			int transportTypeID = (Integer)transportTypeInfo.get("TransportTypeID");
			Class<?> transportType = TransportHelper.getTransportType(transportTypeID);

			ITransport transport = TransportProvider.<ITransport>getTransportFromDict(transportType,
				(Map<String, Object>)transportInfo.get("Transport"));

			Class<?> transportHandlerType = TransportHandlerHelper.getTransportHandlerType(transportTypeID);
			ITransportHandler<?> transportHandler = (ITransportHandler<?>)transportHandlerType.newInstance();
			transportHandler.setInterval(interval == 0 ? 100 : interval);
			transportHandler.setBatchSize(batchSize == 0 ? 1 : batchSize);

			messageHandler = transportHandler.<TMessage>createHandler(messageType, transport);

			if(messageHandler != null){
				messageHandler.setMessageReceived(messageAction);
				messageHandler.setErrorReceived(errorAction);
				messageHandler.start();
				TransportHandlers.addHandler(messageHandler);
			}
		}
		catch(Exception ex){
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			System.out.println(sw.toString());
		}
		return messageHandler;
	}

	public Subscriber addTransport(final String transportName, final ITransport transport, final String topicName) throws SubscriberNotExistException, IllegalAccessException, InvocationTargetException {
		validateObjectState();
		Map<String, Object> transportData = ESBHelper.toDict(transport);
		final Map<String, Object> transportDict = transportData;
		_actions.add(new ESBAction(){
			public void execute() throws IOException, ESBException, IllegalAccessException, InvocationTargetException {
				String result = RestHelper.invoke("AddTransport",
					HashBuilder.create()
						.add("subscriber", _subscriberName)
						.add("transportName", transportName)
						.add("transportType", TransportHelper.getTransportTypeID(transport.getClass()))
						.add("topicName", topicName)
						.add("transportData", transportDict).getHash());
				ESBHelper.throwExceptionIfNeeded(result);
			}
		});
		return this;
	}

	public Subscriber addTransport(final String transportName, final ITransport transport) throws SubscriberNotExistException, IllegalAccessException, InvocationTargetException {
		return addTransport(transportName, transport, null);
	}

	public Subscriber deleteTransport(final String transportName) throws SubscriberNotExistException{
		validateObjectState();
		_actions.add(new ESBAction(){
			public void execute() throws IOException, ESBException {
				String result = RestHelper.invoke("DeleteTransport",
					HashBuilder.create()
						.add("subscriber", _subscriberName)
						.add("transportName", transportName).getHash());
				ESBHelper.throwExceptionIfNeeded(result);
			}
		});
		return this;
	}

	public Subscriber unSubscribeFrom(final String topicName) throws SubscriberNotExistException {
		validateObjectState();
		_topics.remove(topicName);
		_actions.add(new ESBAction(){
			public void execute() throws IOException, ESBException {
				String result = RestHelper.invoke("UnSubscribeFrom",
					HashBuilder.create()
						.add("subscriber", _subscriberName)
						.add("topicName", topicName).getHash());
				ESBHelper.throwExceptionIfNeeded(result);
			}
		});
		return this;
	}

	public void delete() throws SubscriberNotExistException, IOException, ESBException {
		validateObjectState();
		String result = RestHelper.invoke("DeleteSubscriber", HashBuilder.create().add("name", _subscriberName).getHash());
		ESBHelper.throwExceptionIfNeeded(result);
	}

	public void save() throws SubscriberNotExistException, IOException, ESBException, TopicNotRegisteredException, SubscriberNotExistException, IllegalAccessException, InvocationTargetException {
		validateObjectState();
		for(ESBAction action : _actions) action.execute();
		_actions.clear();
	}

	public static Subscriber create(String name){
		return new Subscriber(name);
	}

	@SuppressWarnings("unchecked")
	public static Subscriber select(String name) throws IOException, TopicNotRegisteredException, ESBException {
		String json = RestHelper.invoke("SelectSubscriber", HashBuilder.create().add("name", name).getHash());
		Map<String, Object> data = RestHelper.fromJsonToMap(json);
		if(data == null) {
			ESBHelper.throwExceptionIfNeeded(json);
			return Subscriber.create(name).setState(ObjectState.InValid);
		}
		Subscriber subscriber =
			new Subscriber((String)data.get("SubscriberName"), false)
				.id(new Long(data.get("SubscriberID").toString()))
				.setState(ObjectState.get((Integer)data.get("State")));
		subscriber._isSubscriberDurable = (Boolean)data.get("IsSubscriberDurable");
		subscriber._transports = (Map<String, Map<String, Object>>)data.get("Transports");
		Map<String, Object> topics = (Map<String, Object>)data.get("Topics");
		for(Map.Entry<String, Object> entry : topics.entrySet())
			subscriber._topics.put(entry.getKey(), Topic.dictionaryToTopic((Map<String, Object>)entry.getValue()));
		subscriber._actions.clear();
		return subscriber;
	}
}
