package psb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class HttpStreaming {

	private Action<String> onReceived;
	private final static ExecutorService threadPool = 
    		Executors.newCachedThreadPool();
	private BufferedReader bf;
	private InputStream in;
	private volatile Boolean running = false;
	private String url;
	
	public HttpStreaming(String url){
		this.url = url;
				
	}
	
	private void setup(){
		try {
			in = new URL(url).openConnection().getInputStream();
		} catch (MalformedURLException e) {} 
		catch (IOException e) {}
		if(in == null)
			throw new RuntimeException("Could not connect to url");
		bf = new BufferedReader(new InputStreamReader(in));
	}
	
	public void start(){
		if(running)
			throw new RuntimeException("Streaming is already in progress");
		running = true;
		setup();
		threadPool.submit(new Runnable() {			
			@Override
			public void run() {
				checkIfAvailable();
			}
		});
		threadPool.submit(new Runnable() {			
			@Override
			public void run() {
				poll();
			}
		});
	}
	
	public void stop(){
		running = false;
		try { bf.close(); } catch (IOException e) {}
	}
	
	private int read(){
		try {
			return bf.read();
		} catch (IOException e) {}
		return -1;
	}
	
	public void poll() {
		StringBuffer reader = new StringBuffer();
		int eNum = 0, bNum = 7, cChar1 = 0;
		while(true){			
			if((cChar1 = read()) == -1){
				pulse();
				continue;
			}
			reader.append((char)cChar1);						
			if((eNum = reader.indexOf("</comet>")) != -1){	
				if(onReceived != null)
					onReceived.execute(reader.substring(bNum, eNum));
				reader.delete(0, reader.length());				
			}
		}
	}
	
	private void pulse(){
		notifyAll();
		try { wait(); } catch (InterruptedException e) {}
	}
	
	private Boolean hasData(){
		try {
			return in.available() > 0;
		} catch (IOException e) {}
		return false;
	}
	
	private void checkIfAvailable() {
		while(running) {
			if(hasData()){
				pulse();
			}
		}		
	}
	
	public void setOnReceived(Action<String> action){
		onReceived = action;
	}
}
