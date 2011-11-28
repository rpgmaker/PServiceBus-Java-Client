package pservicebus.transports;

import java.util.*;
import pservicebus.runtime.*;
import pservicebus.interfaces.*;

public class BasicHttpTransport implements ITransport {
	public String Url;
	public Map<String,String> Headers;
	public String ContentType;
	public Integer ContentLength;
	public String Method;
	public TransportFormat Format;
}
