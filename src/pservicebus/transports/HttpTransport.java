package pservicebus.transports;

import java.util.*;
import pservicebus.runtime.*;
import pservicebus.interfaces.*;

public class HttpTransport implements ITransport {
	public String Url;
	public Map<String, String> Headers;
	public Integer ContentLength;
	public TransportFormat Format;
}
