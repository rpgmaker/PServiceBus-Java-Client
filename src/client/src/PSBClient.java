import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


public final class PSBClient {

	private static final String USERNAME_KEY = "pservicebus_username_info",
       ESBTOPIC_HEADERS = "ESBTOPIC_HEADERS",
       STREAM_URL = "StreamSubscriberMessages/?subscriberName={0}&transportName={1}&batchSize={2}&interval={3}&ConnectionID={4}&transport=httpstreaming";
	private static String endpoint, apikey, passcode, address, username;
	private static Boolean throwException, durable;
	private static TransportType transport;
	private static ILocalStorage storage;
	private static Action<Object> onDisconnect;
	private static HashMap<String, HttpStreaming> handlers;
	
	static {
		endpoint = "http://localhost:8087/ESBRestService/";
		address = "endpoint://guest:guest@localhost:5672/";
		transport = TransportType.RabbitMQ;
		handlers = new HashMap<String, HttpStreaming>();
		storage = LocalStorage.getInstance();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){
			@Override
			public void run() {
				PSBClient.disconnect();
			}
		}));
	}
	
	private static void registerTopic(String name, String description, HashMap<String, String> contract){
		if(name == null) throw new NullPointerException("name");
		Map<String, Object> topic = RestHelper.fromJsonToMap(RestHelper.invoke("SelectTopic", 
				HashBuilder.create().add("name", name).getHash()));
		if(!Boolean.parseBoolean(topic.get("IsValid").toString())){
			RestHelper.invoke("RegisterTopic", HashBuilder.create()
				.add("topicData", 
					HashBuilder.create()
						.add("TopicName", name)
						.add("TopicDescription", description != null ? description : name)
						.add("ContractDict", contract != null ? contract : new HashMap<String, String>())
						.toJSON())
				.getHash());
		}
	}
	
	public static void unRegister(String name){
		if(name == null) throw new NullPointerException("name");
		RestHelper.invoke("DeleteTopic", 
			HashBuilder.create()
				.add("name", name).getHash());
	}
	
	public static void unRegister(Class<?> type){
		String name = type.getName();
		unRegister(name);
	}
	
	public static void register(String name){
		registerTopic(name, null, null);
	}
	
	public static void register(String name, String description){
		registerTopic(name, description, null);
	}
	
	public static void register(Class<?> type){
		String name = type.getName();
    	Description typeAnnotation = type.getAnnotation(Description.class);
    	String description = typeAnnotation != null ? typeAnnotation.description() : name;
    	Field[] fields = type.getFields();
    	HashMap<String, String> contract = new HashMap<String, String>();
    	for(Field field : fields){
    		TopicHeader fieldTopicHeaderAnnotation = field.getAnnotation(TopicHeader.class);
    		if(fieldTopicHeaderAnnotation != null) continue;
    		Description fieldAnnotation = field.getAnnotation(Description.class);
    		String fieldDesc = fieldAnnotation != null ? fieldAnnotation.description() : StringExtension.empty;
    		contract.put(field.getName(), fieldDesc);
    	}
    	registerTopic(name, description, contract);
	}
	
	private static String parseAddress(String topicName){
		if(transport == TransportType.MSMQ)
			return StringExtension.join(StringExtension.empty, new String[]{address, topicName, getUserName()});
		return StringExtension.join(StringExtension.empty, new String[]{address, ";queue=", topicName, getUserName()});
	}
	
	private static Map<String, Object> getTransportData(String topicName){
		switch(transport){
			case MSMQ:
			case RabbitMQ:
			case Redis:
				return HashBuilder.create()
						.add("Format", 0)
						.add("Path", parseAddress(topicName))
						.getHash();
			default:
				String[] tokens = address.split(":");
				Boolean useSSL = tokens.length > 2 && tokens[2].toLowerCase().equals("true");
				String ipAddress = tokens[0];
				int port = Integer.parseInt(tokens[1]);
				return HashBuilder.create()
						.add("Format", 0)
						.add("IPAddress", ipAddress)
						.add("Port", port)
						.add("UseSSL", useSSL).getHash();
		}
	}
	
	private static void subscribeToTopic(String username, String topicName, String filter, Boolean needHeader, Boolean caseSensitive){
		RestHelper.invoke("SubscribeTo",
			HashBuilder.create()
				.add("subscriber", username)
				.add("topicName", topicName)
				.add("filter", filter)
				.add("needHeader", needHeader)
				.add("caseSensitive", caseSensitive)
				.getHash());
		RestHelper.invoke("AddTransport",
				HashBuilder.create()
					.add("subscriber", username)
					.add("transportName", topicName)
					.add("topicName", topicName)
					.add("transportType", transport.getCode())
					.add("transportData", RestHelper.toJson(getTransportData(topicName)))
					.getHash());
	}
	
	@SuppressWarnings("unchecked")
	public static <T> void subscribe(Class<T> type, final Action<T> callback, String filter, long interval, int batchSize, Boolean caseSensitive){
		filter = filter == null ? StringExtension.empty : filter;
		interval = interval <= 0 ? 5 : interval;
		batchSize = batchSize <= 0 ? 1 : batchSize;
		String topicName = type.getName();
		Field[] fields = type.getFields();
		Field hField = null;
		String username = getUserName();
		for(Field field : fields){
			TopicHeader topicHeader = field.getAnnotation(TopicHeader.class);
    		if(topicHeader != null) {
    			hField = field;
    			break;
    		}
		}
		final Field headerField = hField;
		final Boolean needHeader = headerField != null;
		//Register topic if not exists
		register(topicName);
		Map<String, Object> subscriber = 
			RestHelper.fromJsonToMap(RestHelper.invoke("SelectSubscriber",
			HashBuilder.create().add("name", username).getHash()));
		Boolean isValid = (Boolean)subscriber.get("IsValid");
		Map<String, Object> topics = (Map<String, Object>)subscriber.get("Topics");
		HttpStreaming handler = null;
		if(!isValid)
			RestHelper.invoke("CreateSubscriber", 
				HashBuilder.create()
					.add("subscriber", username).getHash());
		if(!topics.containsKey(topicName)) 
			subscribeToTopic(username, topicName, filter, needHeader, caseSensitive);
		if(callback == null) return;
		handler = new HttpStreaming(
			StringExtension.format(STREAM_URL,
				username, topicName, batchSize,
				interval, username));
		handlers.put(topicName, handler);
		handler.setOnReceived(new Action<String>(){
			public void execute(String data){
				if(needHeader) 
					data = data.replace(ESBTOPIC_HEADERS, headerField.getName());
				callback.execute(RestHelper.<T>fromJson(data));
			}
		});	
	}
	
	public static <T> void subscribe(Class<T> type, Action<T> callback, String filter, long interval, int batchSize){
		PSBClient.<T>subscribe(type, callback, filter, interval, batchSize, true);
	}
	
	public static <T> void subscribe(Class<T> type, Action<T> callback, String filter, long interval){
		PSBClient.<T>subscribe(type, callback, filter, interval, 0);
	}
	
	public static <T> void subscribe(Class<T> type, Action<T> callback, String filter){
		PSBClient.<T>subscribe(type, callback, filter, 0, 0);
	}
	
	public static <T> void subscribe(Class<T> type, Action<T> callback){
		PSBClient.<T>subscribe(type, callback, null, 0, 0);
	}
	
	public static void unSubscribe(String topicName){
		if(topicName == null) throw new NullPointerException("topicName");
		RestHelper.invoke("UnSubscribeFrom", 
				HashBuilder.create()
					.add("subscriber", getUserName())
					.add("topicName", topicName).getHash());
		RestHelper.invoke("DeleteTransport", 
				HashBuilder.create()
					.add("subscriber", getUserName())
					.add("transportNme", topicName).getHash());
		if(handlers.containsKey(topicName)){
			handlers.get(topicName).stop();
			handlers.remove(topicName);
		}
	}
	
	public static void unSubscribe(Class<?> type){
		String topicName = type.getName();
		unSubscribe(topicName);
	}
	
	public static void publish(String topicName, Object message, String groupID, int sequenceID, long expiresIn, Map<String, String> headers){
		if(expiresIn == 0) expiresIn = (30 * 60 * 60) * 1000;
		headers = headers == null ? new HashMap<String, String>() : headers;
		if(StringExtension.isNullOrEmpty(groupID) && sequenceID > 0){
			headers.put("ESB_GROUP_ID", groupID);
			headers.put("ESB_SEQUENCE_ID", String.valueOf(sequenceID));
		}
		//Register topic if not exists
		register(topicName);
		RestHelper.invoke("PublishTopic", 
			HashBuilder.create()
				.add("topicName", topicName)
				.add("topicData", 
					HashBuilder.create()
						.add("Headers", headers)
						.add("ExpiresIn", expiresIn)
						.toJSON())
				.add("publishData", RestHelper.toJson(new Object[]{ message}))
			.getHash());
	}
	
	public static void publish(String topicName, Object message){
		publish(topicName, message, null, 0);
	}
	
	public static void publish(String topicName, Object message, String groupID, int sequenceID){
		publish(topicName, message, groupID, sequenceID, 0);
	}
	
	public static void publish(String topicName, Object message, String groupID, int sequenceID, long expiresIn){
		publish(topicName, message, groupID, sequenceID, expiresIn, null);
	}
	
	public static <T> void publish(T message, String groupID, int sequenceID, long expiresIn, Map<String, String> headers){
		Class<?> type = message.getClass();
		publish(type.getName(), message, groupID, sequenceID, expiresIn, headers);
	}
	
	public static <T> void publish(T message, String groupID, int sequenceID, long expiresIn){
		PSBClient.<T>publish(message, groupID, sequenceID, expiresIn);
	}
	
	public static <T> void publish(T message, String groupID, int sequenceID){
		PSBClient.<T>publish(message, groupID, sequenceID, 0);
	}
	
	public static <T> void publish(T message){
		PSBClient.<T>publish(message, null, 0);
	}
	
	private static void disconnect(){
		String username = getUserName();
		cleanUp();
		if(!durable)
			RestHelper.invoke("DeleteSubscriber",
				HashBuilder.create()
				.add("name", username).getHash());
		RestHelper.invoke("Disconnect", 
				HashBuilder.create()
				.add("subscriberName", username).getHash());		
	}
	
	protected final static String getUserName(){
		if(!StringExtension.isNullOrEmpty(username)) return username;
		username = storage.get(USERNAME_KEY);
		if(username != null) durable = true;
		
		if(StringExtension.isNullOrEmpty(username))
			username = storage.getType() + 
				java.util.UUID.randomUUID().toString();
		if(durable) storage.set(USERNAME_KEY, username);
		return username;
	}
	
	private static void cleanUp(){
		if(durable) LocalStorage.getInstance().remove(USERNAME_KEY);
		for(Map.Entry<String, HttpStreaming> kv : handlers.entrySet()){
			HttpStreaming handler = kv.getValue();
			handler.stop();
		}
		handlers.clear();
		if(onDisconnect != null) onDisconnect.execute("Disconnecting");
	}

	public static String getEndpoint() {
		return endpoint + (endpoint.endsWith("/") ? StringExtension.empty : "/");
	}

	public static String getApikey() {
		return apikey;
	}

	public static void setApikey(String apikey) {
		PSBClient.apikey = apikey;
	}

	public static void setEndpoint(String endpoint) {
		PSBClient.endpoint = endpoint;
	}

	public static String getPasscode() {
		return passcode;
	}

	public static void setPasscode(String passcode) {
		PSBClient.passcode = passcode;
	}

	public static String getAddress() {
		return address;
	}

	public static void setAddress(String address) {
		PSBClient.address = address;
	}

	public static TransportType getTransport() {
		return transport;
	}

	public static void setTransport(TransportType transport) {
		PSBClient.transport = transport;
	}

	public static Boolean getThrowException() {
		return throwException;
	}

	public static void setThrowException(Boolean throwException) {
		PSBClient.throwException = throwException;
	}

	public static Boolean getDurable() {
		return durable;
	}

	public static void setDurable(Boolean durable) {
		PSBClient.durable = durable;
	}

	public static void setOnDisconnect(Action<Object> onDisconnect) {
		PSBClient.onDisconnect = onDisconnect;
	}
}
