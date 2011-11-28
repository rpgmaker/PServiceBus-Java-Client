package pservicebus.transports;

import pservicebus.runtime.*;
import pservicebus.interfaces.*;

public class RabbitMQTransport implements ITransport {
	public String Path;
	public TransportFormat Format;
	public RabbitMQTransport(){
		Path = "localhost:5672;userID=guest;password=guest;queue=javaRabbitQueue";
		Format = TransportFormat.Xml;
	}
}
