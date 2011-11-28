package pservicebus.transports;

import java.util.*;
import pservicebus.runtime.*;
import pservicebus.interfaces.*;

public class WebServiceTransport implements ITransport {
	public String Namespace;
	public String Url;
	public String Method;
	public Map<String, String> MethodParameters;
	public TransportFormat Format;
}
