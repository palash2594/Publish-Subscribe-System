
/**
 * This is a client (publisher/ subscriber) class, where all the activities happen
 *
 * @author Maha Krishnan Krishnan
 * @author Palash Jain
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class PubSubAgent implements Publisher, Subscriber, Runnable {

	private Socket clientSocket;
	private String ipAddress;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private int serverPort;
	private static ArrayList<Topic> topicList;
	private static ArrayList<Topic> subscribedTopics;
	private Thread thread;

	/**
	 *
	 * PubSubAgent constructor: First establishes connection to the server is
	 * established, then after receiving the new port number the connection is
	 * re-established with the ManageThread class.
	 *
	 * @param ipAddress the IP address of the server
	 * @throws UnknownHostException
	 * @throws IOException
	 */

	public PubSubAgent(String ipAddress) throws UnknownHostException, IOException {

		thread = new Thread(this);
		this.ipAddress = ipAddress;

		connectToSocket(5000);
		try {
			serverPort = (int) inputStream.readObject();
		} catch (ClassNotFoundException e) {
			System.out.println("Class Not Found Error Exception");
		}
		disconnectFromSocket();

		thread.start();
	}

	/**
	 * to subscribe to a given topic
	 *
	 * @param topic instance of the topic
	 * @throws UnknownHostException
	 * @throws IOException
	 */

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

	/**
	 * to unsubscribe from a given topic
	 *
	 * @param topic instance of the topic
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	@Override
	public void unsubscribe(Topic topic) throws UnknownHostException, IOException {
		connectToSocket(serverPort);

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

	/**
	 * to unsubscribe from all the subscribed topics
	 *
	 * @throws UnknownHostException
	 * @throws IOException
	 */

	@SuppressWarnings("unchecked")
	@Override
	public void unsubscribe() throws UnknownHostException, IOException {
		connectToSocket(serverPort);

		outputStream.writeObject("UnSubscribeALL");
		outputStream.writeObject(clientSocket.getLocalAddress());

		try {
			ArrayList<Topic> topics = (ArrayList<Topic>) inputStream.readObject();

			if (!topics.isEmpty()) {
				System.out.println("You have UnSubscribed to: ");
				for (Topic topic : topicList) {
					System.out.println(topic.getName());
				}
			} else {
				System.out.println("You have not subscribed to any Topics");
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		disconnectFromSocket();

	}

	/**
	 * lists all the subscribed for a particular subscriber
	 *
	 * @throws UnknownHostException
	 * @throws IOException
	 */

	@SuppressWarnings("unchecked")
	@Override
	public void listSubscribedTopics() throws UnknownHostException, IOException {
		connectToSocket(serverPort);

		outputStream.writeObject("SubscribedTopics");
		outputStream.writeObject(clientSocket.getLocalAddress());

		try {
			subscribedTopics = (ArrayList<Topic>) inputStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		disconnectFromSocket();

	}

	/**
	 * to publish an event
	 *
	 * @param event instance of an event
	 * @throws UnknownHostException
	 * @throws IOException
	 */

	@Override
	public void publish(Event event) throws UnknownHostException, IOException {
		connectToSocket(serverPort);

		outputStream.writeObject("Publish");
		outputStream.writeObject(event);
		try {
			System.out.println((String) inputStream.readObject());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		disconnectFromSocket();

	}

	/**
	 * to get the list of all available topics
	 *
	 * @throws UnknownHostException
	 * @throws IOException
	 */

	@SuppressWarnings("unchecked")
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

	/**
	 * to advertise a new topic all the subscribers
	 * 
	 * @param newTopic
	 * @throws UnknownHostException
	 * @throws IOException
	 */

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

	/**
	 * makes the connection with server
	 *
	 * @param port port number of the server to connect with
	 * @throws UnknownHostException
	 * @throws IOException
	 */

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

	/**
	 * The main function .
	 *
	 * @throws UnknownHostException
	 * @throws IOException
	 */

	@SuppressWarnings("resource")
	public static void main(String[] args) throws UnknownHostException, IOException {

		if (args.length == 0 || args.length > 1) {
			System.err.println("Usage: Server's IP Address");
			System.exit(0);
		}

		String ipAddress = args[0];

		PubSubAgent psa = new PubSubAgent(ipAddress);
		while (true) {
			Scanner src = new Scanner(System.in);

			try {
				System.out.println("What do want to do \n 1. Puslish an event\n "
						+ "2. Advertise a topic \n 3. Subscribe to a topic \n 4. Unsubscribe from a topic \n "
						+ "5. Unsubscribe from all topic \n 6. Show list of all subscribed topics \n "
						+ "7. List of all available topics");

				int ch = src.nextInt();
				boolean flag = true;

				switch (ch) {
				case 1:
					System.out.print("Do you wish to see the list of topics Y / N: ");
					String answer = src.next();
					System.out.println();

					if (answer.equalsIgnoreCase("y")) {
						psa.getAllTopics();
						if (!topicList.isEmpty())
							psa.listAllTopics();
						else {
							System.out.println("No Topics Available");
							break;
						}
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

					if (answer.equalsIgnoreCase("y")) {
						psa.getAllTopics();
						if (!topicList.isEmpty())
							psa.listAllTopics();
						else {
							System.out.println("No Topics Available");
							break;
						}
					}

					System.out.print("Enter the Topic Name: ");
					topic = src.next().toLowerCase();
					System.out.println();

					psa.listSubscribedTopics();
					try {
						for (Topic topic2 : subscribedTopics) {
							if (topic2.getName().equalsIgnoreCase(topic)) {
								System.out.println("You already subscribed to the topic '" + topic + "'");
								flag = false;
								break;
							}
						}
					} catch (NullPointerException exception) {
						
					}

					if (!flag) {
						break;
					}
						

					psa.getAllTopics();
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

						try {
							if (!subscribedTopics.isEmpty()) {
								System.out.println("Subscribed Topics are: ");
								for (Topic topic1 : subscribedTopics) {
									System.out.println(topic1.getName());
								}
							} else {
								System.out.println("You have not subscribed to any Topics");
								break;
							}
						} catch (NullPointerException exception) {
							System.out.println("You have not been subscribed to any topic");
							break;
						}

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
					try {
						if (!subscribedTopics.isEmpty()) {
							System.out.println("Subscribed Topics are: ");
							for (Topic topic1 : subscribedTopics) {
								System.out.println(topic1.getName());
							}
						} else {
							System.out.println("You have not subscribed to any Topics");
							break;
						}
					} catch (NullPointerException exception) {
						System.out.println("You have not been subscribed to any topic");
						break;
					}
					break;
				case 7:
					psa.getAllTopics();
					if (!topicList.isEmpty())
						psa.listAllTopics();
					else
						System.out.println("No Topics Available");
					break;

				}
			} catch (InputMismatchException e) {
				System.out.println("Please provide the correct option number");
			}
		}
	}
}
