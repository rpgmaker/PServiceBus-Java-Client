

import java.util.Scanner;

import psb.*;


public class ChatConsole {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);

		String text = null;

		PSBClient.subscribe(ChatTopic.class, new Action<ChatTopic>(){
			public void execute(ChatTopic msg){
				System.out.println(String.format("%s: %s",msg.UserName, msg.Message));
			}
		});
		
		System.out.println("Waiting for messages....");
		PSBClient.publish(ChatTopic.New("Java", "add-user"));

		while( !(text = in.nextLine()).equals("exit") ){
			PSBClient.publish(ChatTopic.New("Java", text));
		}
	}

}
