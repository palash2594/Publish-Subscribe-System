
/**
 * This class interacts with clients on behalf of the server, with the available ports.
 * 
 * @author Maha Krishnan Krishnan
 * @author Palash Jain
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ManageThread implements Runnable {

	private EventManager eventManager;
	private ServerSocket serverSocket;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	/**
	 * Class constructor
	 * 
	 * @param eventManager
	 * @param serverSocket
	 */

	public ManageThread(EventManager eventManager, ServerSocket serverSocket) {
		this.eventManager = eventManager;
		this.serverSocket = serverSocket;
	}

	/**
	 * starts the thread and listens to clients requests
	 */

	@Override
	public void run() {
		while (true) {
			try {
				Socket clientSocket = serverSocket.accept();
				System.out
						.println("Connected with the server port: " + serverSocket.getLocalPort() + " with the client:"
								+ clientSocket.getInetAddress() + " client port: " + clientSocket.getLocalPort());

				outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
				inputStream = new ObjectInputStream(clientSocket.getInputStream());

				switch ((String) inputStream.readObject()) {
				case "Topic":
					outputStream.writeObject(eventManager.addTopic((Topic) inputStream.readObject()));
					break;
				case "Subscribe":
					outputStream.writeObject(eventManager.addSubscriber((Topic) inputStream.readObject(),
							(InetAddress) inputStream.readObject()));
					break;
				case "AllTopics":
					outputStream.writeObject(eventManager.getAllTopics());
					break;
				case "Publish":
					eventManager.notifySubscribers((Event) inputStream.readObject());
					outputStream.writeObject("Event updated in the server");
					break;
				case "UnSubscribe":
					outputStream.writeObject(eventManager.removeSubscriber((Topic) inputStream.readObject(),
							(InetAddress) inputStream.readObject()));
					break;
				case "SubscribedTopics":
					outputStream.writeObject(eventManager.listSubscribedTopics((InetAddress) inputStream.readObject()));
					break;
				case "UnSubscribeALL":
					outputStream.writeObject(eventManager.removeSubscriber((InetAddress) inputStream.readObject()));
					break;
				}
			} catch (IOException e) {
				System.out.println("Connection Error with server: " + serverSocket.getLocalPort());
			} catch (ClassNotFoundException e) {
				System.out.println("Class Not Found Exception in Manage Thread");
			}
		}
	}

}
