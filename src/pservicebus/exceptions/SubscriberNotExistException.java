package pservicebus.exceptions;

import java.lang.*;

public class SubscriberNotExistException extends Exception {
	public SubscriberNotExistException(String message){
		super(message);
	}
}
