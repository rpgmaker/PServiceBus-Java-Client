package pservicebus.runtime;

import java.io.*;
import java.lang.reflect.*;
import pservicebus.exceptions.*;


public interface ESBAction {
	public void execute() throws IOException, ESBException, TopicNotRegisteredException, SubscriberNotExistException, IllegalAccessException, InvocationTargetException;
}
