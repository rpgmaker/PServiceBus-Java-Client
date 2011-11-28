package pservicebus.runtime;

import java.util.*;
import java.lang.*;
import java.lang.reflect.*;
import pservicebus.interfaces.*;
import pservicebus.runtime.transformations.*;

public class TransportProvider {

	private static Map<TransportFormat, IDataTransformation> lookup;

	static{
		lookup = new HashMap<TransportFormat, IDataTransformation>();
		buildTransformations();
	}

	private static void buildTransformations(){
		lookup.put(TransportFormat.Json, new JsonTransformation());
		lookup.put(TransportFormat.Xml, new XmlTransformation());
		lookup.put(TransportFormat.Text, new TextTransformation());
		lookup.put(TransportFormat.Csv, new CsvTransformation());
	}

	public static TransportFormat getMessageFormat(String data){
		return data.startsWith("<List>") ? TransportFormat.Xml :
			data.startsWith("[") ? TransportFormat.Json :
			data.indexOf("-EOF-") >= 0 ? TransportFormat.Text :
			TransportFormat.Csv;
	}

	@SuppressWarnings("unchecked")
	public static <TTransport> TTransport getTransportFromDict(Class<?> type, Map<String, Object> dict){
		Object obj = null;
		try{
			obj = type.newInstance();
			Field[] fields = type.getFields();
			for(Field field : fields){
				String fieldName = field.getName();
				if(fieldName.equals("Format")) field.set(obj, TransportFormat.get((Integer)dict.get(fieldName)));
				else field.set(obj, dict.get(fieldName));
			}
		}
		catch(InstantiationException ex){}
		catch(IllegalAccessException ex){}
		return (TTransport) obj;
	}

	public static IDataTransformation getTransformation(TransportFormat format){
		return lookup.get(format);
	}
}
