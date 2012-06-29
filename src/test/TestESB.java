import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import pservicebus.*;
import pservicebus.interfaces.*;
import pservicebus.exceptions.*;
import pservicebus.runtime.*;
import pservicebus.runtime.transports.*;
import pservicebus.Topic;
import pservicebus.Subscriber;
import pservicebus.transports.*;
import pservicebus.runtime.serializers.*;


public class TestESB {
	public static void configurationESBConnection(){
		//Configurate if not running locally
		ESB.authenticate("username", "password");//This is only need if you have setup authentication on the ESB server
		ESB.connect("http://localhost:8087/ESBRestService");
	}

	public static void getNumberOfTopicAvailable() throws ESBException, TopicNotRegisteredException, IOException {
		//Get all topic available in the ESB
		List<Topic> topics = ESB.getTopics();
		System.out.println(topics.size());
	}

	public static void publishChatTopicMessageUsingClass() throws ESBException, TopicNotRegisteredException, IOException, IllegalAccessException, InvocationTargetException {
		//Publish message to the ESB
		Topic.publishMessage(new ChatTopic("Java App", "Hello From Java"));
	}

	public static void publishChatTopicWithOutUsingClass() throws IOException, TopicNotRegisteredException, ESBException, IllegalAccessException, InvocationTargetException {
		//Publish message to the ESB
		Topic.select(ChatTopic.class)
			.setParameter("UserName", "Java App")
			.setParameter("Message", "Hello From Java")
			.publish();
	}

	public static void getTopicInformation() throws IOException, TopicNotRegisteredException, ESBException{
		//Retrieve ChatTopic information
		Topic topic = Topic.select(ChatTopic.class);
		System.out.println(topic.getTopicName());
	}

	public static void testTcpMessageHandler() throws IOException, ESBException, SubscriberNotExistException, TopicNotRegisteredException, IllegalAccessException, InvocationTargetException {
		//Register ChatTopic
		Topic.register(ChatTopic.class);

		//Subscribe "Olamide" to ChatTopic with TcpTransport using a filter on the UserName not equal to "Olamide"
		String userName = "JavaTcpUser";
		Subscriber subscriber = Subscriber.select(userName);
		if(subscriber.getState() == ObjectState.InValid){
			Subscriber.create(userName)
				.subscribeTo(Topic.select(ChatTopic.class).notEqual("UserName", userName))
				.addTransport("Tcp", new TcpTransport(), "ChatTopic")
				.save();
		}
		subscriber = Subscriber.select(userName);

		subscriber.onMessageReceived(ChatTopic.class, "Tcp", new ESBMessageAction<ChatTopic>(){
			public void handle(ChatTopic chatTopic){
				if(chatTopic.Headers != null){
					for(Map.Entry<String, String> entry : chatTopic.Headers.entrySet())
						System.out.println(String.format("%s: %s", entry.getKey(), entry.getValue()));
				}
				System.out.println(String.format("%s: %s",chatTopic.UserName, chatTopic.Message));
			}
		},
			new ESBMessageAction<Exception>(){
				public void handle(Exception ex){
					ExceptionHelper.printStackTrace(ex);
				}
			}
		);

		Scanner in = new Scanner(System.in);

		String text = null;

		System.out.println("Waiting for messages....");

		while( !in.nextLine().equals("exit") );

		TransportHandlers.shutdown();

		subscriber.delete();

		in.close();
	}

	public static void testRabbitMQMessageHandler() throws IOException, ESBException, SubscriberNotExistException, TopicNotRegisteredException, IllegalAccessException, InvocationTargetException {
		//Register ChatTopic
		Topic.register(ChatTopic.class);

		//Subscribe "Olamide" to ChatTopic with TcpTransport using a filter on the UserName not equal to "Olamide"
		String userName = "JavaRabbitMQUser";
		Subscriber subscriber = Subscriber.select(userName);
		if(subscriber.getState() == ObjectState.InValid){
			Subscriber.create(userName)
				.subscribeTo(Topic.select(ChatTopic.class).notEqual("UserName", userName))
				.addTransport("RabbitMQ", new RabbitMQTransport(), "ChatTopic")
				.save();
		}
		subscriber = Subscriber.select(userName);

		subscriber.onMessageReceived(ChatTopic.class, "RabbitMQ", new ESBMessageAction<ChatTopic>(){
			public void handle(ChatTopic chatTopic){
				if(chatTopic.Headers != null){
					for(Map.Entry<String, String> entry : chatTopic.Headers.entrySet())
						System.out.println(String.format("%s: %s", entry.getKey(), entry.getValue()));
				}
				System.out.println(String.format("%s: %s",chatTopic.UserName, chatTopic.Message));
			}
		},
			new ESBMessageAction<Exception>(){
				public void handle(Exception ex){
					ExceptionHelper.printStackTrace(ex);
				}
			}
		);

		Scanner in = new Scanner(System.in);

		String text = null;

		System.out.println("Waiting for messages....");

		while( !(text = in.nextLine()).equals("exit") ){
			Topic.publishMessage(new ChatTopic(userName, text));
		}

		TransportHandlers.shutdown();

		subscriber.delete();

		in.close();
	}

	public static void main(String[] args) throws IOException, ESBException, SubscriberNotExistException, TopicNotRegisteredException, IllegalAccessException, InvocationTargetException {
		//testTcpMessageHandler();
		testRabbitMQMessageHandler();
	}
}
