package pservicebus;

import java.util.*;
import java.io.*;
import pservicebus.*;
import pservicebus.exceptions.*;
import pservicebus.runtime.*;


public class Topics {
	@SuppressWarnings("unchecked")
	public List<Topic> getAll() throws ESBException, TopicNotRegisteredException, IOException{
		List<Topic> topics = new ArrayList<Topic>();
		String json = RestHelper.invoke("GetTopics", null);
		List<Map<String, Object>> data = RestHelper.<List<Map<String, Object>>>fromJson(json);
		if(data != null){
			for(Map<String, Object> topicDict : data)
				topics.add(Topic.dictionaryToTopic(topicDict));
		}else throw new ESBException(json);
		return topics;
	}
}
