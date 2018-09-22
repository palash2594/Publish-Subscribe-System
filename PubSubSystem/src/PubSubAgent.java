
//package impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

//import demo.*;

public class PubSubAgent implements Publisher, Subscriber, Runnable {

	private Socket clientSocket;
	private String ipAddress;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private int serverPort;
	private static ArrayList<Topic> topicList;
	private Thread thread;

	public PubSubAgent(String ipAddress) throws UnknownHostException, IOException {

		thread = new Thread(this);

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

		thread.start();
	}

	@Override
	public void subscribe(Topic topic) throws UnknownHostException, IOException {
		connectToSocket(serverPort);

		outputStream.writeObject("Subscribe");
		outputStream.writeObject(topic);
		outputStream.writeObject(clientSocket.getLocalAddress());
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

		System.out.println(clientSocket.getLocalAddress());

		outputStream.writeObject("UnSubscribe");
		outputStream.writeObject(topic);
		outputStream.writeObject(clientSocket.getLocalAddress());
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
		outputStream.writeObject(clientSocket.getLocalAddress());
		
		try {
			ArrayList<Topic> topics = (ArrayList<Topic>) inputStream.readObject();
			System.out.println("You have UnSubscribed to: ");
			for (Topic topic : topicList) {
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
	public void listSubscribedTopics() throws UnknownHostException, IOException {
		connectToSocket(serverPort);

		outputStream.writeObject("SubscribedTopics");
		outputStream.writeObject(clientSocket.getLocalAddress());

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

		System.out.println(event.getTopic());

		outputStream.writeObject("Publish");
		outputStream.writeObject(event);
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

	@Override
	public void run() {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(6000);
			Socket clientSocket;
			while (true) {
				clientSocket = serverSocket.accept();
				outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
				inputStream = new ObjectInputStream(clientSocket.getInputStream());

				String type = (String) inputStream.readObject();

				if (type.equalsIgnoreCase("Topic")) {
					Topic topic = (Topic) inputStream.readObject();
					System.out.println("New Topic '" + topic.getName() + "' advertised");
					outputStream.writeObject("Delivered topic to the client: " + clientSocket.getLocalAddress());
				} else {
					Event event = (Event) inputStream.readObject();
					System.out.println("Published " + event);
					outputStream.writeObject(
							"Delievered Event to the subscribed client: " + clientSocket.getLocalAddress());
				}
			}
		} catch (IOException e) {
			System.out.println("Connection Exception while connecting with the server to receive notifications");
		} catch (ClassNotFoundException e) {
			System.out
					.println("Class Cast Exception in the while reading the input notification object from the server");
		}
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
				System.out.println("Do you wish to see the list of topics yes / no: ");
				String answer = src.next();

				if (answer.equalsIgnoreCase("yes")) {
					psa.getAllTopics();
					psa.listAllTopics();
				}

				System.out.println("Enter topic: ");
				String topic = src.next().toLowerCase();
				psa.getAllTopics();

				Topic t = null;
				for (Topic topic2 : topicList) {
					if (topic2.getName().equalsIgnoreCase(topic)) {
						t = topic2;
						break;
					}
				}

				if (t == null) {
					System.out.println("Topic does not exist");
					break;
				}

				src.nextLine();
				System.out.println("Enter title: ");
				String title = src.nextLine();

				System.out.println("Enter Content: ");
				String content = src.nextLine();

				Event event = new Event();
				event.setId(title.hashCode());
				event.setTopic(t);
				event.setTitle(title);
				event.setContent(content);

				psa.publish(event);
				break;
			case 2:
				t = new Topic();

				System.out.print("Enter topic name: ");
				String topicName = src.next().toLowerCase();

				System.out.println();

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
				System.out.print("Do you wish to see the list of topics Y / N: ");
				answer = src.next();

				System.out.println();

				psa.getAllTopics();
				if (answer.equalsIgnoreCase("y")) {
					psa.listAllTopics();
				}

				System.out.print("Enter the Topic Name: ");
				topic = src.next().toLowerCase();
				System.out.println();
				
				for (Topic topic1 : topicList) {
					if (topic1.getName().equalsIgnoreCase(topic)) {
						psa.subscribe(topic1);
						flag = false;
						break;
					}
				}

				if (flag) {
					System.out.println("Topic '" + topic + "' never exists in the database");
				}

				break;
			case 4:
				System.out.print("Do you wish to see the list of topics Y / N: ");
				answer = src.next().toLowerCase();
				System.out.println();
				
				if (answer.equalsIgnoreCase("Y")) {
					psa.listSubscribedTopics();
				}

				System.out.print("Enter Topic Name: ");
				topic = src.next().toLowerCase();
				System.out.println();

				for (Topic topic1 : topicList) {
					if (topic1.getName().equalsIgnoreCase(topic)) {
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
