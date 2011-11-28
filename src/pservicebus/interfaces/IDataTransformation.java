package pservicebus.interfaces;

import java.util.*;

public interface IDataTransformation {
	public <TMessage> List<TMessage> transform(String data, Class<TMessage> messageType);
}
