package pservicebus.transports;

import pservicebus.runtime.*;
import pservicebus.interfaces.*;

public class FtpTransport implements ITransport {
	public String Url;
	public String UserName;
	public String Password;
	public String AliasToAddToFileName;
	public String FileExtension;
	public String FileNameTemplate;
	public String FolderName;
	public TransportFormat Format;
}
