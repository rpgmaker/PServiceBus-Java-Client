package pservicebus.runtime.transports;

import java.io.*;
import java.net.*;
import java.util.*;
import pservicebus.interfaces.*;
import pservicebus.runtime.messages.*;

public class TcpProvider implements IObjectProvider {
	private String _endpoint;
	private int _port;
	private String _host;
	private ServerSocket _socketServer;

	public TcpProvider(){
		_endpoint = null;
		_socketServer = null;
	}
	public String getEndpoint(){ return _endpoint; }
	public void setEndpoint(String endpoint){
		String[] tokens = endpoint.split(":");
		_endpoint = endpoint;
		_host = tokens[0];
		_port = Integer.parseInt(tokens[1]);
	}

	public void process(final MessageProcessor messageProcessor){
		try{
			if(_socketServer == null){
				_socketServer = new ServerSocket();
				_socketServer.bind(new InetSocketAddress(_host, _port));
			}
			Socket client = _socketServer.accept();
			BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));

			StringBuilder sb = new StringBuilder();
			String text = null;

			while((text = br.readLine()) != null)
				sb.append(text + "\r\n");

      		client.close();
      		messageProcessor.process(sb.toString());
		}
		catch(UnknownHostException ex){}
		catch(IOException ex){}
	}

	public void dispose() {
		try{
			if(_socketServer != null) _socketServer.close();
		}
		catch(IOException ex){}
	}
}
