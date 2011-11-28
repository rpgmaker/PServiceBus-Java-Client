package pservicebus.interfaces;

import  pservicebus.runtime.*;
import java.util.*;

public interface IMessageHandler<TMessage> {
	public ESBMessageAction<TMessage> getMessageReceived();
	public ESBMessageAction<Exception> getErrorReceived();
	public void setMessageReceived(ESBMessageAction<TMessage> messageAction);
	public void setErrorReceived(ESBMessageAction<Exception> errorAction);
	public Boolean getIsRunning();
	public int getBatchSize();
	public void setBatchSize(int batchSize);
	public String getEndpoint();
	public void setEndpoint(String endpoint);
	public int getInterval();
	public void setInterval(int interval);
	public void start();
	public void stop();
}
