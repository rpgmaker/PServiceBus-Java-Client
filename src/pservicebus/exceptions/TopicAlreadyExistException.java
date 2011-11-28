package pservicebus.exceptions;

import java.lang.*;

public class TopicAlreadyExistException extends Exception {
	public TopicAlreadyExistException(String message){
		super(message);
	}
}
