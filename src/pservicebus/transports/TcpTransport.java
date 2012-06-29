package pservicebus.transports;

import pservicebus.runtime.*;
import pservicebus.interfaces.*;

public class TcpTransport implements ITransport {

	public TransportFormat Format;
	public String IPAddress;
	public int Port;
	public Boolean UseSSL;

	public TcpTransport(){
		Format = TransportFormat.Text;
		UseSSL = false;
		Port = 11111;
		IPAddress = "127.0.0.1";
	}
}