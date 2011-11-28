package pservicebus.runtime.serializers;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.lang.*;
import org.apache.commons.codec.binary.Base64;
import pservicebus.extensions.*;
import pservicebus.runtime.*;


public class XmlSerializer {

	private static final Map<String, Boolean> _hasTypeProperties;
	private static final String ESBTOPIC_HEADERS = "ESBTOPIC_HEADERS";
	private static final String _xmlBase64Tag = "[xmlbase64]";

	static{
		_hasTypeProperties = new HashMap<String, Boolean>();
	}

	@SuppressWarnings("unchecked")
	private static Document getDocument(String xml){
		Document doc = null;
		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
			doc.getDocumentElement().normalize();
		}
		catch(ParserConfigurationException ex){}
		catch(SAXException ex){}
		catch(IOException ex){}
		return doc;
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserialize(String xml, Class<?> type){
		return (T)deserializeObject(xml, type);
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserialize(String xml, Class<?> type, Class<?> paramType){
		return (T)deserialize(getDocument(xml), type, paramType, null);
	}

	@SuppressWarnings("unchecked")
	public static Object deserializeObject(String xml, Class<?> type, Class<?> paramType, Class<?> optionalParamType){
		return deserialize(getDocument(xml), type, paramType, optionalParamType);
	}

	@SuppressWarnings("unchecked")
	public static Object deserializeObject(String xml, Class<?> type){
		Class<?> paramType = null;
		if(type.isArray()) paramType = type.getComponentType();
		return deserialize(getDocument(xml), type, paramType, null);
	}

	@SuppressWarnings("unchecked")
	private static Object deserialize(Document doc, Class<?> type, Class<?> paramType, Class<?> optionalParamType){
		Element element = (Element)doc.getChildNodes().item(0);
		return readObject(element, type, paramType, optionalParamType);
	}

	private static final Class _collectionType = Collection.class;
	private static final Class _dictionaryType = Map.class;
	private static final Class _objectType = Object.class;

	@SuppressWarnings("unchecked")
	private static Object readObject(Element element, Class<?> type, Class<?> paramType, Class<?> optionalParamType){
		Object obj = null;
		String elementName = element.getNodeName();
		Boolean isNil = element.getAttributes().getLength() > 0 &&
			element.getAttribute("type").equals("nil");
		if(hasProperties(type)){
			if(type.isArray()){
				Element itemsElement = (Element)element.getFirstChild();
				obj = Array.newInstance(type.getComponentType(),
					itemsElement.getChildNodes().getLength());
				readItemsField(obj, itemsElement, paramType, optionalParamType);
			}else{
				obj = ReflectionHelper.createInstance(type);
				Node node = element.getFirstChild();
				while(node != null){
					if(node.getNodeType() == Node.ELEMENT_NODE)
						readField(obj, (Element)node, paramType, optionalParamType);
					node = node.getNextSibling();
				}
			}
		} else obj = isNil ? null : ReflectionHelper.changeType(element.getTextContent(), type);
		return obj;
	}


	@SuppressWarnings("unchecked")
	private static void readField(Object obj, Element element, Class<?> paramType, Class<?> optionalParamType){
		try{
			String elementName = element.getNodeName();
			Boolean isItems = elementName.equals("Items") &&
				element.getAttributes().getLength() > 0 &&
				element.getAttribute("type").equals("Items");
			Class<?> objType = obj.getClass();
			Field field = ReflectionHelper.getField(elementName, objType);
			Class type = field != null ? field.getType() : null;
			if(field == null && elementName.equals(ESBTOPIC_HEADERS)) return;
			if(field != null){
				if(_collectionType.isAssignableFrom(type)){
					paramType = (Class<?>)(((ParameterizedType)field.getGenericType())
					.getActualTypeArguments()[0]);
				}else if(type.isArray()){
					paramType = field.getType().getComponentType();
				}else if(_dictionaryType.isAssignableFrom(type)){
					Type[] types = ((ParameterizedType)field.getGenericType())
						.getActualTypeArguments();
					paramType = (Class<?>)types[0];
					optionalParamType = (Class<?>)types[1];
				}
			}
			if(!isItems){
				if(hasProperties(type)) field.set(obj, readObject(element, type, paramType, optionalParamType));
				else
					field.set(obj, ReflectionHelper.changeType(element.getTextContent(), type));
			} else {
				readItemsField(obj, element, paramType, optionalParamType);
			}
		}
		catch(IllegalAccessException ex){}
	}

	@SuppressWarnings("unchecked")
	private static void readItemsField(Object obj, Element element, Class<?> paramType, Class<?> optionalParamType){
		Class<?> type = obj.getClass();
		if(paramType == null) paramType = type;
		Node parentNode = element.getParentNode();
		Field field = paramType != null && parentNode != null ? ReflectionHelper.getField(parentNode.getNodeName(), paramType) : null;
		int count = 0;
		Boolean isArray = type.isArray();
		Boolean isDict = _dictionaryType.isAssignableFrom(type);
		Class<?> arryType = isArray ? type.getComponentType() : null;
		Class<?> dictKeyType = _objectType, dictValueType = _objectType;
		Class<?> listType = null;
		if(!isArray && !isDict){
			listType = field != null ? (Class<?>)(((ParameterizedType)field.getGenericType())
				.getActualTypeArguments()[0]) : paramType;
		}
		if(isDict){
			Type[] types = field != null ? ((ParameterizedType)field.getGenericType())
				.getActualTypeArguments() : null;
			if(types != null && types.length > 0){
				dictKeyType = (Class<?>)types[0];
				dictValueType = (Class<?>)types[1];
			}else{
				dictKeyType = paramType;
				dictValueType = optionalParamType;
			}
		}
		NodeList elementChildNodes = element.getChildNodes();
		for(int i = 0 ; i < elementChildNodes.getLength(); i++){
			Element item = (Element)elementChildNodes.item(i);
			if(isDict){
				NodeList itemNodes = item.getChildNodes();
				Object key = readObject((Element)itemNodes.item(0), dictKeyType, paramType, null);
				Object value = readObject((Element)itemNodes.item(1), dictValueType, paramType, null);
				((Map)obj).put(key, value);
			}
			else if(isArray){
				Object value = readObject(item, arryType, paramType, null);
				Array.set(obj, count++, value);
			}
			else ((Collection)obj).add(readObject(item, listType, paramType, null));
		}
	}

	@SuppressWarnings("unchecked")
	private static Boolean hasProperties(Class<?> type){
		String key = type.getName();
		Boolean value = _hasTypeProperties.get(key);
		if(_hasTypeProperties.containsKey(key)) return value;
		value = !type.isPrimitive() &&
			 	type != Integer.class &&
			 	type != String.class &&
			 	type != Long.class &&
			 	type != Float.class &&
			 	type != Short.class &&
			 	type != Byte.class &&
			 	type != Boolean.class &&
			 	type != Character.class &&
			 	type != byte[].class &&
				(type.isArray() || _collectionType.isAssignableFrom(type) ||
				_dictionaryType.isAssignableFrom(type) ||
				(ReflectionHelper.canInitType(type) && type.getFields().length > 0) );
		_hasTypeProperties.put(key, value);
		return value;
	}

	public static Boolean isXmlBase64(String xml){
		return !StringExtension.isNullOrEmpty(xml) && xml.endsWith(_xmlBase64Tag);
	}

	public static String xmlBase64ToXml(String xml){
		String value = xml.replace(_xmlBase64Tag, StringExtension.empty);
		return new String(Base64.decodeBase64(value.getBytes()));
	}
}