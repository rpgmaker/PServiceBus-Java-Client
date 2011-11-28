package pservicebus.runtime.transformations;

import java.util.*;
import java.lang.reflect.*;
import java.util.regex.*;
import java.io.*;
import java.lang.*;
import pservicebus.interfaces.*;
import pservicebus.runtime.*;
import pservicebus.extensions.*;
import pservicebus.runtime.serializers.XmlSerializer;

public class TextTransformation implements IDataTransformation {
	private static final Pattern _keyValueTextReg = Pattern.compile("(.*?)\\s*:\\s*(.*)", Pattern.MULTILINE);
	private static final Class _dictionaryType = Map.class;
	private static final Class _collectionType = Collection.class;

	@SuppressWarnings("unchecked")
	public <TMessage> List<TMessage> transform(String data, Class<TMessage> messageType){
		List<TMessage> messages = new ArrayList<TMessage>();
		String[] tokens = data.split("-EOF-");
		try{
			for(String token : tokens){
				if(StringExtension.isNullOrEmpty(token)) continue;
				if(token.startsWith("\r\n")) continue;
				TMessage obj = (TMessage)ReflectionHelper.createInstance(messageType);
				Matcher match = _keyValueTextReg.matcher(token);
				while(match.find()){
					Field field = ReflectionHelper.getField(match.group(1), messageType);
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
					String value = StringExtension.trimEnd(match.group(2), "\r");
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
