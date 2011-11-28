package pservicebus.exceptions;

import java.lang.*;

public class TopicNotRegisteredException extends Exception {
	public TopicNotRegisteredException(String message){
		super(message);
	}
}
