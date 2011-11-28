package pservicebus.runtime.transformations;

import java.util.*;
import pservicebus.interfaces.*;
import pservicebus.runtime.serializers.*;

public class XmlTransformation implements IDataTransformation {
	public <TMessage> List<TMessage> transform(String data, Class<TMessage> messageType){
		Class<?> arrayType = null;
		try{arrayType = Class.forName("[L" + messageType.getName() + ";");} catch(ClassNotFoundException ex){}
		TMessage[] arrayOfMessages = XmlSerializer.<TMessage[]>deserialize(data, arrayType);
		return Arrays.asList(arrayOfMessages);
	}
}
