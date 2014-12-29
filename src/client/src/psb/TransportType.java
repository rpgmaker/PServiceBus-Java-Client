package psb;


public enum TransportType {
   MSMQ (0),
   RabbitMQ (1),
   Tcp (2),
   Redis (6);

   private static final java.util.HashMap<Integer, TransportType> lookup
		= new java.util.HashMap<Integer, TransportType>();

	static{
		lookup.put(0, TransportType.MSMQ);
		lookup.put(1, TransportType.RabbitMQ);
		lookup.put(2, TransportType.Tcp);
		lookup.put(6, TransportType.Redis);
	}

	private int code;

	private TransportType(int code){
		this.code = code;
	}

	public static TransportType get(int code){
		return lookup.get(code);
	}

	public int getCode(){
		return code;
	}
}