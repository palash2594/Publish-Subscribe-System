
package impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import demo.*;

public class PubSubAgent implements Publisher, Subscriber {

	private Socket clientSocket;
	private String ipAddress;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private int serverPort;
	private static ArrayList<Topic> topicList;

	public PubSubAgent(String ipAddress) throws UnknownHostException, IOException {

		this.ipAddress = ipAddress;

		connectToSocket(5000);
		try {
			serverPort = (int) inputStream.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Class Not Found Error Exception");
//			e.printStackTrace();
		}
		disconnectFromSocket();

	}

	@Override
	public void subscribe(Topic topic) throws UnknownHostException, IOException {
		connectToSocket(serverPort);
		outputStream.writeObject("Subscribe");
		outputStream.writeObject(topic);
		outputStream.writeObject(clientSocket.getInetAddress());
		try {
			System.out.println(inputStream.readObject());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		disconnectFromSocket();

	}

	@Override
	public void subscribe(String keyword) throws UnknownHostException, IOException {
		connectToSocket(serverPort);

		disconnectFromSocket();

	}

	@Override
	public void unsubscribe(Topic topic) throws UnknownHostException, IOException {
		connectToSocket(serverPort);
		outputStream.writeObject("UnSubscribe");
		outputStream.writeObject(topic);
		outputStream.writeObject(clientSocket.getInetAddress());
		try {
			System.out.println(inputStream.readObject());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		disconnectFromSocket();

	}

	@Override
	public void unsubscribe() throws UnknownHostException, IOException {
		connectToSocket(serverPort);

		outputStream.writeObject("UnSubscribeALL");
		outputStream.writeObject(clientSocket.getInetAddress());

		disconnectFromSocket();

	}

	@Override
	public void listSubscribedTopics() throws UnknownHostException, IOException {
		connectToSocket(serverPort);

		outputStream.writeObject("SubscribedTopics");
		outputStream.writeObject(clientSocket.getInetAddress());

		try {
			ArrayList<Topic> subscribedTopics = (ArrayList<Topic>) inputStream.readObject();
			for (Topic topic : subscribedTopics) {
				System.out.println(topic.getName());
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException exception) {
			System.out.println("You have not been subscribed to any topic");
		}
		disconnectFromSocket();

	}

	@Override
	public void publish(Event event) throws UnknownHostException, IOException {
		connectToSocket(serverPort);

		outputStream.writeObject("Publish");
		try {
			System.out.println((String) inputStream.readObject());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		disconnectFromSocket();

	}

	public void getAllTopics() throws UnknownHostException, IOException {
		connectToSocket(serverPort);

		outputStream.writeObject("AllTopics");
		try {
			topicList = (ArrayList<Topic>) inputStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		disconnectFromSocket();
	}

	public void listAllTopics() {

		System.out.println("Topics List are: ");
		for (Topic topic : topicList) {
			System.out.println(topic.getName());
		}
	}

	@Override
	public void advertise(Topic newTopic) throws UnknownHostException, IOException {
		connectToSocket(serverPort);

		outputStream.writeObject("Topic");
		outputStream.writeObject(newTopic);
		try {
			System.out.println(inputStream.readObject());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		disconnectFromSocket();

	}

	public void connectToSocket(int port) throws UnknownHostException, IOException {

		clientSocket = new Socket(ipAddress, port);
		outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
		inputStream = new ObjectInputStream(clientSocket.getInputStream());

	}

	public void disconnectFromSocket() throws IOException {
		inputStream.close();
		outputStream.close();
		clientSocket.close();
	}

	public static void main(String[] args) throws UnknownHostException, IOException {

		Scanner src = new Scanner(System.in);

		String ipAddress = src.next();

		PubSubAgent psa = new PubSubAgent(ipAddress);
		while (true) {
			System.out.println("What do want to do \n 1. Puslish an event\n "
					+ "2. Advertise a topic \n 3. Subscribe to a topic \n 4. Unsubscribe from a topic \n "
					+ "5. Unsubscribe from all topic \n 6. Show list of all subscribed topics \n "
					+ "7. List of all available topics");

			int ch = src.nextInt();
			boolean flag = true;

			switch (ch) {
			case 1:
				System.out.println("Do you wish to see the list of topics yes / no");
				String answer = src.next();
				if (answer.equalsIgnoreCase("yes")) {
					psa.listAllTopics();
				}
				System.out.println("Enter topic, title and content");
				String topic = src.next();
				String title = src.nextLine();
				String content = src.nextLine();

				Topic t = new Topic();
				t.setId(topic.hashCode());
				t.setName(topic);
				t.setKeywords(null);

				Event event = new Event();
				event.setId(title.hashCode());
				event.setTopic(t);
				event.setTitle(title);
				event.setContent(content);

				psa.publish(event);
				break;
			case 2:
				System.out.println("Enter topic name and keywords");

				t = new Topic();
				String topicName = src.next().toLowerCase();

				psa.getAllTopics();
				for (Topic topic2 : topicList) {
					if (topic2.getName().equalsIgnoreCase(topicName)) {
						System.out.println("Topic '" + topicName + "' already exists");
						flag = false;
						break;
					}
				}

				if (flag) {
					t.setId(topicName.hashCode());
					t.setName(topicName);
					t.setKeywords(null);

					psa.advertise(t);
				}
				break;
			case 3:
				System.out.println("Do you wish to see the list of topics yes / no");

				answer = src.next();
				psa.getAllTopics();

				if (answer.equalsIgnoreCase("yes")) {
					psa.listAllTopics();
				}

				topic = src.next();

				for (Topic topic1 : topicList) {
					if (topic1.getName().equals(topic)) {
						psa.subscribe(topic1);
						flag = false;
						break;
					}
				}

				if (flag) {
					System.out.println("Topic '" + answer + "' never exists in the database");
				}

				break;
			case 4:
				System.out.println("Do you wish to see the list of topics yes / no");
				answer = src.next();
				if (answer.equalsIgnoreCase("yes")) {
					psa.getAllTopics();
				}

				topic = src.next();

				for (Topic topic1 : topicList) {
					if (topic1.getName().equals(topic)) {
						psa.unsubscribe(topic1);
						flag = false;
						break;
					}
				}
				if (flag)
					System.out.println("No such topic exists");
				break;
			case 5:
				psa.unsubscribe();
				break;
			case 6:
				psa.listSubscribedTopics();
				break;
			case 7:
				psa.getAllTopics();
				psa.listAllTopics();
				break;

			}
		}

	}

}
