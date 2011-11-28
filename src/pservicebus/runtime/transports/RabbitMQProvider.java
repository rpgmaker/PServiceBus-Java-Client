package pservicebus.runtime.transports;

import java.lang.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import com.rabbitmq.client.*;
import pservicebus.interfaces.*;
import pservicebus.runtime.*;
import pservicebus.runtime.messages.*;

public class RabbitMQProvider implements IObjectProvider  {

	private static final Pattern _connRegex =
		Pattern.compile("(.*?)(?:\\:)?(\\d+);userID=(.*?)?;password=(.*?);queue=([^;]*)(?:;)?");
	private static final long TIMEOUT = 30;
	private volatile Boolean _disposed;
	private String _endpoint;
	private ConnectionFactory _factory;
	private String _queueName;
	private Boolean _queueCreated;

	public String getEndpoint(){ return _endpoint; }

	public void setEndpoint(String endpoint){
		_endpoint = endpoint;
		_queueCreated = false;
		_disposed = false;
		Matcher match = _connRegex.matcher(endpoint);
		Boolean matches = match.matches();
		String hostName = match.group(1);
		Integer port = Integer.parseInt(match.group(2));
		String userID = match.group(3);
		String password = match.group(4);
		_queueName = match.group(5);

		_factory = new ConnectionFactory();
 		_factory.setUsername(userID);
 		_factory.setPassword(password);
 		_factory.setVirtualHost("/");
 		_factory.setHost(hostName);
 		_factory.setPort(port);
	}

	private void createQueue(Channel channel, String queueName){
		if(_queueCreated) return;
		try{
			channel.exchangeDeclare("ex" + queueName, "fanout", true);
      		channel.queueDeclare(queueName, true, false, false, null);
      		channel.queueBind(queueName, "ex" + queueName, queueName);
      		_queueCreated = true;
		}catch(IOException ex){
			ExceptionHelper.printStackTrace(ex);
		}
	}

	public void process(final MessageProcessor messageProcessor){
		if(_disposed) return;
		try{
			Connection connection = _factory.newConnection();
			Channel channel = connection.createChannel();

			createQueue(channel, _queueName);

			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(_queueName, false, consumer);

			QueueingConsumer.Delivery delivery;
			while(true){
            	try {
            		delivery = consumer.nextDelivery(TIMEOUT);
            		byte[] body = delivery.getBody();
            		byte[] data = new byte[body.length - 25];
					System.arraycopy(body, 24, data, 0, data.length);
            		String message = new String(data);
            		Boolean processNext = messageProcessor.process(message);
            		channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            		if(!processNext) break;
            	}
            	catch (InterruptedException ex) { break; }
            	catch(Exception ex){ break; }
			}
			if(connection.isOpen() && channel != null) channel.close();
			connection.close();
		}
		catch(IOException ex){ }
	}

	public void dispose(){}
}
