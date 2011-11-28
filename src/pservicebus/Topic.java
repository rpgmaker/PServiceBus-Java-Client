package pservicebus;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import pservicebus.*;
import pservicebus.exceptions.*;
import pservicebus.runtime.*;
import pservicebus.extensions.*;
import pservicebus.annotations.*;

public class Topic {
	private String _topicName;
	private ObjectState _state;
	private Map<String, String> _contractDict;
	private Map<String, Object> _publishDict;
	private Long _topicID;
	private String _filter;
	private List<String> _filters;
	private Map<String, Object> _headers;
	private String _topicDescription;
	private int _expiresIn;

	public String getTopicFilter(){
		String[] filterArray = (String[])_filters.toArray(new String[0]);
		String[] array = !StringExtension.isNullOrEmpty(_filter) ? _filter.split(",") : new String[]{};
		return StringExtension.join(",", ArrayExtension.union(array, filterArray));
	}

	private void validateObjectState() throws TopicNotRegisteredException{
		if(_state == ObjectState.InValid) throw new TopicNotRegisteredException(ExceptionConstants.topicExceptionStr(_topicName));
	}

	private Topic(String name){
		_topicName = name;
		_state = ObjectState.Valid;
		_filters = new ArrayList<String>();
		_contractDict = new HashMap<String, String>();
		_publishDict = new HashMap<String, Object>();
		_headers = new HashMap<String, Object>();
		_topicDescription = StringExtension.empty;
	}

	public String getTopicName(){ return _topicName; }

	public ObjectState getState(){ return _state; }

	public Map<String, String> getContractDict(){ return _contractDict; }

	public Map<String, Object> getPublishDict(){ return _publishDict; }

	public Long getTopicID(){ return _topicID; }

	public List<String> getFilters(){ return _filters; }

	public Map<String, Object> getHeaders(){ return _headers; }

	public String getTopicDescription(){ return _topicDescription; }

	public int getExpiresIn(){ return _expiresIn; }

	public Topic filter(String topicFilter) throws TopicNotRegisteredException {
		validateObjectState();
		_filter = topicFilter;
		_filters = Arrays.asList(topicFilter.split(","));
		return this;
	}

	public Topic addParameter(String name, String description) throws TopicNotRegisteredException {
		validateObjectState();
		_contractDict.put(name, description);
		return this;
	}

	public Topic setParameter(String name, Object value) throws TopicNotRegisteredException {
		validateObjectState();
		_publishDict.put(name, value);
		return this;
	}

	public void register() throws TopicNotRegisteredException, ESBException, IOException {
		validateObjectState();
		String result =
			RestHelper.invoke("RegisterTopic",
			HashBuilder.create()
			.add("topicData",
				RestHelper.toJson(
					HashBuilder.create()
					.add("ContractDict", _contractDict)
					.add("TopicName", _topicName)
					.add("TopicDescription", _topicDescription)
					.getHash())
			).getHash());
		ESBHelper.throwExceptionIfNeeded(result);
	}

	public void delete() throws TopicNotRegisteredException, ESBException, IOException {
		validateObjectState();
		String result = RestHelper.invoke("DeleteTopic", HashBuilder.create().add("name", _topicName).getHash());
		ESBHelper.throwExceptionIfNeeded(result);
	}

	public Topic setHeader(String key, Object value) throws TopicNotRegisteredException {
		validateObjectState();
		_headers.put(key, value);
		return this;
	}

	public Topic description(String desc) throws TopicNotRegisteredException {
		validateObjectState();
		_topicDescription = desc;
		return this;
	}

	public Topic setMessageExpiration(int expiresIn) throws TopicNotRegisteredException {
		validateObjectState();
		_expiresIn = expiresIn;
		return this;
	}

	public Topic message(Object msg) throws TopicNotRegisteredException, IllegalAccessException, InvocationTargetException {
		validateObjectState();
		Map<String, Object> dict = ESBHelper.toDict(msg);
		for(Map.Entry<String, Object> entry : dict.entrySet()) _publishDict.put(entry.getKey(), entry.getValue());
		return this;
	}

	public void publish(Object message) throws TopicNotRegisteredException, ESBException, IOException, IllegalAccessException, InvocationTargetException {
		validateObjectState();
		List<Object> list = new ArrayList<Object>();
		list.add(message);
		internalPublish(list);
	}

	public void  publishMany(List<Object> messages) throws TopicNotRegisteredException, ESBException, IOException, IllegalAccessException, InvocationTargetException {
		validateObjectState();
		internalPublish(messages);
	}

	public void publish() throws TopicNotRegisteredException, ESBException, IOException, IllegalAccessException, InvocationTargetException {
		validateObjectState();
		internalPublish(null);
	}

	private void internalPublish(List<Object> messages) throws TopicNotRegisteredException, ESBException, IOException, IllegalAccessException, InvocationTargetException {
		List<Map<String, Object>> publishDicts = new ArrayList<Map<String, Object>>();
		if(messages != null){
			for(Object message : messages)
				publishDicts.add(ESBHelper.toDict(message));
		}
		if(_publishDict != null && _publishDict.size() > 0) publishDicts.add(_publishDict);
		String result = RestHelper.invoke("PublishTopic",
			HashBuilder.create()
				.add("topicName", _topicName)
				.add("topicData",
					RestHelper.toJson(
						HashBuilder.create()
						.add("Headers", _headers)
						.add("ExpiresIn", _expiresIn)
						.getHash()))
				.add("publishData", RestHelper.toJson(publishDicts))
				.getHash()
		);
		ESBHelper.throwExceptionIfNeeded(result);
	}

	private Topic filterByType(String propertyName, String value, String oper){
		_filters.add(String.format("%s %s %s", propertyName, oper, value));
        return this;
	}

	public Topic equal(String propertyName, String value) {
       return filterByType(propertyName, value, "=");
    }

    public Topic notEqual(String propertyName, String value) {
       return filterByType(propertyName, value, "!=");
    }

    public Topic lessThan(String propertyName, String value) {
        return filterByType(propertyName, value, "<");
    }

    public Topic lessThanOrEqual(String propertyName, String value) {
        return filterByType(propertyName, value, "<=");
    }


    public Topic greaterThan(String propertyName, String value) {
        return filterByType(propertyName, value, ">");
    }

    public Topic greaterThanOrEqual(String propertyName, String value) {
        return filterByType(propertyName, value, ">=");
    }

    public Topic contains(String propertyName, String value) {
        return filterByType(propertyName, value, "like");
    }

    public static Topic create(String name){
    	return new Topic(name);
    }

    @SuppressWarnings("unchecked")
    public static void register(Class<?> type) throws TopicNotRegisteredException, ESBException, IOException {
    	String typeName = type.getName();
    	Topic topic = Topic.create(parseTopicName(typeName));
    	Description typeAnnotation = type.getAnnotation(Description.class);
    	String topicDesc = typeAnnotation != null ? typeAnnotation.description() : typeName;
    	topic.description(topicDesc);
    	Field[] fields = type.getFields();
    	for(Field field : fields){
    		String fieldName = field.getName();
    		TopicHeader fieldTopicHeaderAnnotation = field.getAnnotation(TopicHeader.class);
    		if(fieldTopicHeaderAnnotation != null) continue;
    		Description fieldAnnotation = field.getAnnotation(Description.class);
    		String fieldDesc = fieldAnnotation != null ? fieldAnnotation.description() : StringExtension.empty;
    		topic.addParameter(fieldName, fieldDesc);
    	}
    	topic.register();
    }


	public static void publishMessage(Object message) throws TopicNotRegisteredException, ESBException, IOException, IllegalAccessException, InvocationTargetException {
    	Class type = message.getClass();
    	String topicName = type.getName();
    	Topic topic = Topic.select(parseTopicName(topicName));
    	topic.publish(message);
    }

    private static String parseTopicName(String name){
    	String[] tokens = name.split("\\.");
    	return tokens[tokens.length - 1];
    }

    public static void publishMessages(List<Object> messages) throws TopicNotRegisteredException, ESBException, IOException, IllegalAccessException, InvocationTargetException {
    	Class type = messages.get(0).getClass();
    	String topicName = type.getName();
    	Topic topic = Topic.select(parseTopicName(topicName));
    	topic.publishMany(messages);
    }

    public static Topic select(Class<?> type) throws IOException, TopicNotRegisteredException, ESBException {
    	String topicName = type.getName();
    	return select(parseTopicName(topicName));
    }

    protected Topic setState(ObjectState state){
    	_state = state;
    	return this;
    }

	@SuppressWarnings("unchecked")
    protected static Topic dictionaryToTopic(Map<String, Object> data) throws TopicNotRegisteredException{
    	Map<String, String> contractDict = (Map<String, String>)data.get("ContractDict");
    	Topic topic = Topic.create((String)data.get("TopicName")).description((String)data.get("TopicDescription"));
    	topic._topicID = new Long(data.get("TopicID").toString());
    	topic._state = ObjectState.get((Integer)data.get("State"));
    	for(Map.Entry<String, String> entry : contractDict.entrySet())
    		topic._contractDict.put(entry.getKey(), entry.getValue());
    	return topic;
    }

    public static Topic select(String name) throws IOException, TopicNotRegisteredException, ESBException {
    	Topic topic = null;
    	String result = RestHelper.invoke("SelectTopic", HashBuilder.create().add("name", name).getHash());

    	Map<String, Object> data = RestHelper.fromJsonToMap(result);
    	if(data != null) topic = dictionaryToTopic(data);
    	else ESBHelper.throwExceptionIfNeeded(result);
    	return topic;
    }
}
