package pservicebus.transports;

import pservicebus.runtime.*;
import pservicebus.runtime.mssql.*;
import pservicebus.interfaces.*;

public class MSSQLTransport implements ITransport {
	public String ConnectionString;
	public String DynamicQueryTemplate;
	public MSSQLCommand Command;
	public TransportFormat Format;
}
