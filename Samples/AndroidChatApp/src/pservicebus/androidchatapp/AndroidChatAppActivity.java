package pservicebus.androidchatapp;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import pservicebus.ESB;
import pservicebus.Subscriber;
import pservicebus.Topic;
import pservicebus.exceptions.ESBException;
import pservicebus.exceptions.SubscriberNotExistException;
import pservicebus.exceptions.TopicNotRegisteredException;
import pservicebus.runtime.ESBMessageAction;
import pservicebus.runtime.ObjectState;
import pservicebus.runtime.transports.TransportHandlers;
import pservicebus.transports.RabbitMQTransport;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

public class AndroidChatAppActivity extends Activity {
	private Subscriber subscriber = null;
	private String userName = "Android" +  UUID.randomUUID().toString().replace("-", "");
	private Handler windowHandler = null;
	private void setup(){
    		try {
    			ESB.configWithAddress("http://74.208.226.12:8087/ESBRestService");
    			Topic.register(ChatTopic.class);
    			subscriber = Subscriber.select(userName);
    			if(subscriber.getState() == ObjectState.InValid){
        			final RabbitMQTransport rabbitTransport = new RabbitMQTransport();
        			rabbitTransport.Path = 
        					String.format("74.208.226.12:5672;userID=guest;password=guest;queue=%schat", userName);
        			
    					Subscriber.create(userName)
    						.subscribeTo(Topic.select(ChatTopic.class).notEqual("UserName", userName))
    						.addTransport("RabbitMQ", rabbitTransport, "ChatTopic")
    						.save();
    					Topic.publishMessage(new ChatTopic(userName, "add-user"));
    				
        		}
				subscriber = Subscriber.select(userName);
				subscriber.onMessageReceived(ChatTopic.class, "RabbitMQ", new ESBMessageAction<ChatTopic>(){
	    			public void handle(ChatTopic chatTopic){
	    				Message msg = new Message();
	    				Bundle data = new Bundle();
	    				//data.putCharSequence("esbMessage",
	    				//		String.format("%s: %s",chatTopic.UserName, chatTopic.Message));
	    				data.putSerializable("esbMessage", chatTopic);
	    				msg.setData(data);
	    				windowHandler.sendMessage(msg);
	    			}
	    		},
	    			new ESBMessageAction<Exception>(){
	    				public void handle(Exception ex){
	    					ex.printStackTrace();
	    				}
	    			}
	    		);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TopicNotRegisteredException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ESBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SubscriberNotExistException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	private Spanned formatMessage(String username, String message){
		String userText = username != null ? ("<span style='font-weight:strong;color:#0f0;'>" + username + ":</span> ") : "";
		return Html.fromHtml("<div>" + userText + message + "</div>");
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final EditText messageText = (EditText)findViewById(R.id.messageText);
        final EditText inputText = (EditText)findViewById(R.id.inputText);
        final Button sendBtn = (Button) findViewById(R.id.sendBtn);
        final Button endChatBtn = (Button) findViewById(R.id.endChatBtn);
        final ScrollView scroller = (ScrollView) findViewById(R.id.scroller);
        
        setup();
        
        windowHandler= new Handler(){
            @Override
            public void  handleMessage(Message msg){ 
            	ChatTopic chatTopic = (ChatTopic)msg.getData().get("esbMessage");
            	if(chatTopic == null) return;
            	if(chatTopic.Message == null) return;
            	Spanned msgValue = null;
            	if(chatTopic.Message.equals("add-user"))
            		msgValue = formatMessage(null, String.format("%s has joined the chat room", chatTopic.UserName));
            	else if(chatTopic.Message.equals("remove-user"))
            		msgValue = formatMessage(null, String.format("%s has left the chat room", chatTopic.UserName));
            	else
            		msgValue = formatMessage(chatTopic.UserName, chatTopic.Message);
            	messageText.append(msgValue);
            	scroller.smoothScrollTo(0, messageText.getBottom());
            }
        };
        
        inputText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                  // Perform action on key press
                  sendBtn.performClick();
                  return true;
                }
                return false;
            }
        });
        
        sendBtn.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		try {
        			String text = inputText.getText().toString();
					Topic.publishMessage(new ChatTopic(userName, text));
					messageText.append(formatMessage("You", text));
					inputText.setText("");
				} catch (TopicNotRegisteredException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ESBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        });

        endChatBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(endChatBtn.getText() == "Continue"){
            		endChatBtn.setText("End Chat");
            		setup();
            		sendBtn.setEnabled(true);
            		return;
            	}
            	try {
            		if(subscriber == null) return;
					Topic.publishMessage(new ChatTopic(userName, "remove-user"));
					messageText.setText("");
	            	sendBtn.setEnabled(false);
	            	endChatBtn.setText("Continue");
	            	TransportHandlers.shutdown();
	            	subscriber.delete();
	            	subscriber = null;
				} catch (TopicNotRegisteredException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ESBException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SubscriberNotExistException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
    }
}