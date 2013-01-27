package pservicebus;

import java.util.Scanner;

import psb.*;


public class ChatConsole {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);

		String text = null;

		System.out.println("Waiting for messages....");

		while( !(text = in.nextLine()).equals("exit") ){
			PSBClient.publish(ChatTopic.New("Java", text));
		}
	}

}
