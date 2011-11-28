package pservicebus.runtime.transformations;

import java.util.*;
import java.lang.reflect.*;
import java.io.*;
import java.lang.*;
import pservicebus.interfaces.*;
import pservicebus.runtime.*;
import pservicebus.extensions.*;
import pservicebus.runtime.serializers.XmlSerializer;

public class CsvTransformation implements IDataTransformation  {
	private static final Class _dictionaryType = Map.class;
	private static final Class _collectionType = Collection.class;

	@SuppressWarnings("unchecked")
	public <TMessage> List<TMessage> transform(String data, Class<TMessage> messageType){
		List<TMessage> messages = new ArrayList<TMessage>();
		String[] sData = data.split("\r\n");
		Boolean hasData = sData.length > 1;
		String[] header = hasData ? sData[0].split(",") : null;
		int headerLength = header != null ? header.length : 0;
		try{
			for(int i = 1; i < sData.length && hasData; i++){
				String sDataText = sData[i];
				if(StringExtension.isNullOrEmpty(sDataText)) continue;
				String[] tokens = sDataText.split(",");
				TMessage obj = (TMessage)ReflectionHelper.createInstance(messageType);
				for(int j = 0; j < headerLength; j++){
					Field field = ReflectionHelper.getField(header[j], messageType);
					if(field == null) continue;
					Class fieldType = field.getType();
					Boolean isDict = _dictionaryType.isAssignableFrom(fieldType);
					Boolean isList = _collectionType.isAssignableFrom(fieldType);
					Boolean isArray = fieldType.isArray();
					Boolean hasParamTypes = isDict || isList || isArray;
					Type[] types = hasParamTypes ?
						(isArray ? new Type[]{ fieldType.getComponentType() }
						: ((ParameterizedType)field.getGenericType()).getActualTypeArguments())
						 : null;
					String value = tokens[j];
					Boolean isXmlBase64 = XmlSerializer.isXmlBase64(value);
					value = isXmlBase64 ? String.format("<Data>%s</Data>", XmlSerializer.xmlBase64ToXml(value)) : value;
					field.set(obj, isXmlBase64 ?
							XmlSerializer.deserializeObject(value, fieldType, hasParamTypes ? (Class<?>)types[0] : null,
								isDict ? (Class<?>)types[1] : null) :
							ReflectionHelper.changeType(value, fieldType));
				}
				messages.add(obj);
			}
		}
		catch(IllegalAccessException ex){}
		return messages;
	}
}
