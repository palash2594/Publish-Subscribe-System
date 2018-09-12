package impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import demo.*;

public class PubSubAgent implements Publisher, Subscriber{
	
	private Socket clientSocket;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private int serverPort;
	
	public PubSubAgent() throws UnknownHostException, IOException {
		
		connectToSocket(5000);
		serverPort = inputStream.readInt();
		disconnectFromSocket();
		
	}

	@Override
	public void subscribe(Topic topic) throws UnknownHostException, IOException {
		connectToSocket(serverPort);
		
		disconnectFromSocket();
		
	}

	@Override
	public void subscribe(String keyword) throws UnknownHostException, IOException {
		connectToSocket(serverPort);
		
		disconnectFromSocket();
		
	}

	@Override
	public void unsubscribe(Topic topic) throws UnknownHostException, IOException{
		connectToSocket(serverPort);
		
		disconnectFromSocket();
		
	}

	@Override
	public void unsubscribe() throws UnknownHostException, IOException{
		connectToSocket(serverPort);
		
	}

	@Override
	public void listSubscribedTopics() throws UnknownHostException, IOException{
		connectToSocket(serverPort);
		
		disconnectFromSocket();
		
	}

	@Override
	public void publish(Event event) throws UnknownHostException, IOException{
		connectToSocket(serverPort);
		
		disconnectFromSocket();
		
	}

	@Override
	public void advertise(Topic newTopic) throws UnknownHostException, IOException{
		connectToSocket(serverPort);
		
		disconnectFromSocket();
		
	}
	
	public void connectToSocket(int port) throws UnknownHostException, IOException {
		
		clientSocket = new Socket( "localhost", 5000 );
		outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
		inputStream = new ObjectInputStream(clientSocket.getInputStream());
		
	}
	
	public void disconnectFromSocket() throws IOException {
		inputStream.close();
		outputStream.close();
		clientSocket.close();
	}

	
	public static void main(String[] args) throws UnknownHostException, IOException {
		
		new PubSubAgent();
		
	}

	
}
