package pservicebus.runtime.transformations;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.*;
import pservicebus.interfaces.*;
import pservicebus.runtime.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class JsonTransformation implements IDataTransformation {
	private static final Pattern _dateRegex = Pattern.compile("\\\\/Date\\((\\d+)\\)\\\\/", Pattern.MULTILINE);
	private static final SimpleDateFormat _dateFormat = new SimpleDateFormat("MM/dd/yyyy");

	public JsonTransformation(){}

	public <TMessage> List<TMessage> transform(String data, Class<TMessage> messageType){
		data = normalize(data);
		Class arrayType = null;
		try{arrayType = Class.forName("[L" + messageType.getName() + ";");} catch(ClassNotFoundException ex){}
		TMessage[] arrayOfMessages = RestHelper.<TMessage[]>fromJson(data, arrayType);
		return Arrays.asList(arrayOfMessages);
	}

	private String normalize(String data){
		Matcher match = _dateRegex.matcher(data);
		while(match.find()){
			Long dateMilliseconds = new Long(match.group(1));
			String dateText = _dateFormat.format(new Date(dateMilliseconds));
			data = data.replace(match.group(0), dateText);
		}
		return data;
	}
}
