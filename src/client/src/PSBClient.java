
public final class PSBClient {

	private static final String USERNAME_KEY = "pservicebus_username_info";
	private static String endpoint, apikey, passcode, address, username;
	private static Boolean throwException, durable;
	private static TransportType transport;
	private static ILocalStorage storage;
	private static Action<Object> onDisconnect;
	
	static {
		endpoint = "http://localhost:8087/ESBRestService/";
		address = "localhost:5672;userID=guest;password=guest";
		transport = TransportType.RabbitMQ;
		storage = LocalStorage.getInstance();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){
			@Override
			public void run() {
				PSBClient.disconnect();
			}
		}));
	}
	
	private final static void disconnect(){
		if(onDisconnect != null) onDisconnect.execute("Disconnecting");
	}
	
	@SuppressWarnings("unused")
	private static String getUserName(){
		if(!StringExtension.isNullOrEmpty(username)) return username;
		username = storage.get(USERNAME_KEY);
		if(username != null) durable = true;
		
		if(StringExtension.isNullOrEmpty(username))
			username = storage.getType() + 
				java.util.UUID.randomUUID().toString();
		if(durable) storage.set(USERNAME_KEY, username);
		return username;
	}

	public static String getEndpoint() {
		return endpoint;
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
