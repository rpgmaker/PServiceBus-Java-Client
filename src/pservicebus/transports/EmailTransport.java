package pservicebus.transports;

import pservicebus.runtime.*;
import pservicebus.interfaces.*;

public class EmailTransport implements ITransport {
	public String From;
	public String To;
	public String Subject;
	public String Host;
	public String UserName;
	public String Password;
	public String ReplyTo;
	public Integer Port;
	public Boolean IsSSL;
	public Boolean SendMessageAsAttachment;
	public String AttachmentFileName;
	public String BodyTemplate;
	public TransportFormat Format;
}
