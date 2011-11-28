package pservicebus.interfaces;

import java.util.*;
import pservicebus.runtime.messages.*;

public interface IObjectProvider {
	public String getEndpoint();
	public void setEndpoint(String endpoint);
	public void process(final MessageProcessor messageProcessor);
	public void dispose();
}
