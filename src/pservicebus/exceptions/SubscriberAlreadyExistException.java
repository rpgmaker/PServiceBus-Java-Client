package pservicebus.exceptions;

import java.lang.*;

public class SubscriberAlreadyExistException extends Exception {
	public SubscriberAlreadyExistException(String message) {
		super(message);
	}
}
