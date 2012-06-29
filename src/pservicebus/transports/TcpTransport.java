package pservicebus.transports;

import pservicebus.runtime.*;
import pservicebus.interfaces.*;

public class TcpTransport implements ITransport {

	public TransportFormat Format;
	public String IPAddress;
	public int Port;
	public Boolean UseSSL;
}