package impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import demo.*;

public class ManageThread implements Runnable {

	private EventManager eventManager;
	private ServerSocket serverSocket;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	public ManageThread(EventManager eventManager, ServerSocket serverSocket) {
		this.eventManager = eventManager;
		this.serverSocket = serverSocket;
	}

	@Override
	public void run() {
		try {
			Socket clientSocket = serverSocket.accept();
			System.out.println("Connected with the server port: " + serverSocket.getLocalPort() + " with the client:"
					+ clientSocket.getInetAddress() + " client port: " + clientSocket.getLocalPort());

			outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
			inputStream = new ObjectInputStream(clientSocket.getInputStream());

			switch ((String) inputStream.readObject()) {
			case "Topic":
				eventManager.addTopic((Topic) inputStream.readObject());
				outputStream.writeObject("Topic Advertised Sucessfully");
			case "Subscribe":
				eventManager.addSubscriber((Topic) inputStream.readObject(), (InetAddress) inputStream.readObject());
				outputStream.writeObject("You have susbcribed to the topic");
			case "List all topics":
				outputStream.writeObject(eventManager.getAllTopics());
			case "Publish":
				eventManager.notifySubscribers((Event) inputStream.readObject());
				outputStream.writeObject("Event updated in the server");
			case "UnSubscribe":
				boolean flag = eventManager.removeSubscriber((Topic) inputStream.readObject(),
						(InetAddress) inputStream.readObject());
				if (flag)
					outputStream.writeObject("Unsubscribed from the Topic");
				else
					outputStream.writeObject("You Haven't subscribed to the Topic");
			}

		} catch (IOException e) {
			System.out.println("Connection Error with server: " + serverSocket.getLocalPort());
		} catch (ClassNotFoundException e) {
			System.out.println("Class Not Found Exception in Manage Thread");
		}
	}

}
