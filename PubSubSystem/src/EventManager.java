
/**
 * This is a server class and manages all the communications with the client.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventManager implements Runnable {

	private static ExecutorService threadPool;
	private Thread thread;
	private static Runnable[] runnableThreads;
	private static HashMap<Integer, ServerSocket> socketMap = new HashMap<>();
	private int numThreads;
	private static ManageInfo manageData;
	private ServerSocket serverSocket;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	/**
	 *
	 * EventManager constructer: Event Manager thread has been created, along with
	 * given number of runnable threads (5) each for ManageThread class. In each
	 * thread a different connection is established with different clients.
	 *
	 * @param numThreads the max number of runnable threads
	 * @throws IOException
	 */

	public EventManager(int numThreads) throws IOException {
		this.numThreads = numThreads;
		threadPool = Executors.newFixedThreadPool(numThreads);
		thread = new Thread(this);
		runnableThreads = new Runnable[numThreads];
		manageData = new ManageInfo();

		for (int i = 0; i < numThreads; i++) {
			socketMap.put(i, new ServerSocket(7000 + i + 1));
			runnableThreads[i] = new ManageThread(this, socketMap.get(i));
		}

		thread.start();
	}

	/**
	 * Start the repo service. When a new client joins it establishes its connection
	 * with the server, then server passes the port number for a new thread and
	 * client re-establishes
	 *
	 * @throws IOException
	 */
	private void startService() throws IOException {
		serverSocket = new ServerSocket(5000);
		int i = 0;
		while (true) {
			System.out.println("Waiting for Client request");
			Socket clientSocket = serverSocket.accept();

			System.out.println(
					"Connection Established between Server and Client and Clients: " + clientSocket.getInetAddress());

			synchronized (manageData) {
				if (!manageData.addClients.contains(clientSocket.getInetAddress())) {
					manageData.addClients.add(clientSocket.getInetAddress());
				} else {
					if (manageData.hasTopics.containsKey(clientSocket.getInetAddress())) {
						manageData.activeClients.add(clientSocket.getInetAddress());
					}

					if (manageData.hasEvents.containsKey(clientSocket.getInetAddress())) {
						manageData.activeSubscribers.add(clientSocket.getInetAddress());
					}
				}
			}

			outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
			inputStream = new ObjectInputStream(clientSocket.getInputStream());

			ServerSocket getSocket = socketMap.get(i);
			outputStream.writeObject(getSocket.getLocalPort());
			threadPool.execute(runnableThreads[i]);
			i = (i + 1) % numThreads;

		}
	}

	/**
	 * add new topic when received advertisement from a client(publisher), and add
	 * the topic to the server's data structure for topic.
	 *
	 * @param topic the topic object
	 * @return String -> success message.
	 */
	public String addTopic(Topic topic) {
		synchronized (manageData) {
			manageData.topics.add(topic);
			manageData.tempTopics.add(topic);
		}
		return "Topic '" + topic.getName() + "' Advertised sucessfully";
	}

	/**
	 * to get the list of all topics available in the topic data structure
	 *
	 * @return ArrayList of all the topics
	 */
	public ArrayList<Topic> getAllTopics() {
		synchronized (manageData) {
			return manageData.topics;
		}
	}

	/**
	 * to add the given client to the hashmap of topic for each subscriber as well
	 * as subscriber of each topic
	 *
	 * @param topic      the topic instance
	 * @param subscriber the IP address of the subscriber
	 * @return String -> success message
	 */
	public String addSubscriber(Topic topic, InetAddress subscriber) {

		synchronized (manageData) {
			manageData.setSubscribedTopics(topic, subscriber);
			manageData.setSubscriberForTopics(topic, subscriber);
		}

		return "You have susbcribed to the topic '" + topic.getName() + "'";
	}

	/**
	 *
	 * to get the list of those topics that a particular client(subscriber) has been
	 * subscribed to.
	 *
	 * @param subscriber IP address of the subscriber.
	 * @return ArrayList of topics
	 */

	public ArrayList<Topic> listSubscribedTopics(InetAddress subscriber) {
		synchronized (manageData) {
			return manageData.getSubscribedTopics(subscriber);
		}
	}

	/**
	 * remove subscriber from the list of topics for subscriber as well as from the
	 * subscriber for topics
	 *
	 * @param topic        instance of topic
	 * @param unSubscriber IP address of the subscriber
	 * @return true if the unsubscription is done successfully else false.
	 */
	public String removeSubscriber(Topic topic, InetAddress unSubscriber) {
		boolean flag = false;
		synchronized (manageData) {
			if (!manageData.isTopicSubscriberSync(topic, unSubscriber)) {
				flag = false;
			} else {
				manageData.removeSubscribedTopics(topic, unSubscriber);
				manageData.removeSubscriberFromTopics(topic, unSubscriber);
				flag = true;
			}
			
			if (flag)
				return "Unsubscribed from the Topic '" + topic.getName() +"'";
			else
				return "You Haven't subscribed to the Topic";
		}
	}

	/**
	 *
	 * to remove all the subscribed topics for a particular subscriber
	 *
	 * @param unSubscriber IP address of the subscriber
	 * @return ArrayList of the unsubscribed topic
	 */

	public ArrayList<Topic> removeSubscriber(InetAddress unSubscriber) {
		synchronized (manageData) {
			return manageData.removeAllSubscribers(unSubscriber);
		}
	}

	/**
	 * notify all subscribers of new event
	 *
	 * @param event instance of an event
	 */
	public void notifySubscribers(Event event) {
		synchronized (manageData) {
			manageData.events.add(event);
		}
	}

	/**
	 *
	 * When a new topic for advertisement, new event comes then a connection is
	 * established with the desired client and the topic/event is sent to the
	 * client. Also, if a user comes online and has pending messages then those
	 * messages will get delivered to the user.
	 */
	@Override
	public void run() {

		int port = 6000;

		while (true) {
			ArrayList<Event> events = new ArrayList<>();
			ArrayList<Topic> topics = new ArrayList<>();
			ArrayList<InetAddress> clients = new ArrayList<>();
			ArrayList<InetAddress> activeSubscribers = new ArrayList<>();
			ArrayList<InetAddress> activeClients = new ArrayList<>();

			// getting the data from ManageInfo class
			synchronized (manageData) {
				topics = manageData.tempTopics;
				events = manageData.events;
				clients = manageData.addClients;
				activeSubscribers = manageData.activeSubscribers;
				activeClients = manageData.activeClients;
			}

			// to check if any client came online and hasn pending topics advertisemnt yet
			// to be delivered
			while (!activeClients.isEmpty()) {
				ArrayList<Topic> topicToSend = new ArrayList<>();
				synchronized (manageData) {
					topicToSend = manageData.hasTopics.get(activeClients.get(0));
				}

				// keeping the count of the number of topic advertisements to be delivered
				int sendCount = 0;
				System.out.println("Size: " + topicToSend.size());
				while (sendCount < topicToSend.size()) {
					try {
						Socket socket = new Socket(activeClients.get(0), port);
						outputStream = new ObjectOutputStream(socket.getOutputStream());
						inputStream = new ObjectInputStream(socket.getInputStream());

						outputStream.writeObject("Topic");
						outputStream.writeObject(topicToSend.get(sendCount));
						System.out.println((String) inputStream.readObject());

						synchronized (manageData) {
							manageData.removeHasTopics(activeClients.get(0), topicToSend.get(sendCount));
						}

						inputStream.close();
						outputStream.close();
						socket.close();

					} catch (IOException e) {
						sendCount++;
						System.out.println("Connection Error with Client");
						break;
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				activeClients.remove(0);
			}

			// to check if any client came online and has any pending message yet to be
			// delivered
			while (!activeSubscribers.isEmpty()) {
				ArrayList<Event> eventsToSend = new ArrayList<>();
				synchronized (manageData) {
					eventsToSend = manageData.hasEvents.get(activeSubscribers.get(0));
				}
				int sendCount = 0;
				System.out.println("Size: " + eventsToSend.size());
				while (sendCount < eventsToSend.size()) {
					try {
						Socket socket = new Socket(activeSubscribers.get(0), port);
						outputStream = new ObjectOutputStream(socket.getOutputStream());
						inputStream = new ObjectInputStream(socket.getInputStream());

						outputStream.writeObject("Event");
						outputStream.writeObject(eventsToSend.get(sendCount));
						System.out.println((String) inputStream.readObject());

						synchronized (manageData) {
							manageData.removeHasEvents(activeSubscribers.get(0), eventsToSend.get(sendCount));
						}

						inputStream.close();
						outputStream.close();
						socket.close();

					} catch (IOException e) {
						System.out.println("Connection Error with Client");
						break;
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				}
				activeSubscribers.remove(0);
			}

			// checking if any new topic advertisement came, if yes, then send to all the
			// clients (publishers and subscribers)
			while (!topics.isEmpty()) {
				int i = 0;
				while (i < clients.size()) {
					try {
						Socket socket = new Socket(clients.get(i++), port);
						outputStream = new ObjectOutputStream(socket.getOutputStream());
						inputStream = new ObjectInputStream(socket.getInputStream());

						outputStream.writeObject("Topic");
						outputStream.writeObject(topics.get(0));
						System.out.println((String) inputStream.readObject());

						outputStream.close();
						inputStream.close();
						socket.close();

					} catch (IOException e) {

						System.out.println("Server unreachable at " + clients.get(i - 1).getHostAddress());
						synchronized (manageData) {
							manageData.setHasTopics(clients.get(i - 1), topics.get(0));
						}

					} catch (ClassNotFoundException e) {

						System.out.println("Class Cast Exception while sending TOPICS to clients");
					}
				}

				topics.remove(0);
				synchronized (manageData) {
					if (!manageData.tempTopics.isEmpty())
						manageData.tempTopics.remove(0);
				}
			}

			// checking if any new event came, if yes, then send to all the subscribed
			// clients
			while (!events.isEmpty()) {
				String topicName = events.get(0).getTopic().getName();
				ArrayList<InetAddress> subscribers = new ArrayList<>();

				synchronized (manageData) {
					HashMap<Topic, ArrayList<InetAddress>> subscriberTopics = manageData.subscriberForTopics;

					for (Map.Entry<Topic, ArrayList<InetAddress>> entry : subscriberTopics.entrySet()) {
						if (entry.getKey().getName().equalsIgnoreCase(topicName)) {
							subscribers = entry.getValue();
						}
					}
				}

				int subscriberCount = 0;
				while (subscriberCount < subscribers.size()) {
					try {
						Socket socket = new Socket(subscribers.get(subscriberCount), port);
						outputStream = new ObjectOutputStream(socket.getOutputStream());
						inputStream = new ObjectInputStream(socket.getInputStream());

						outputStream.writeObject("Event");
						outputStream.writeObject(events.get(0));
						System.out.println((String) inputStream.readObject());

						outputStream.close();
						inputStream.close();
						socket.close();

					} catch (IOException e) {
						System.out.println("Server unreachable at " + subscribers.get(subscriberCount).getHostAddress()
								+ " while sending the event notifications to the subscriber");
						
						System.out.println(subscribers.get(subscriberCount));
						System.out.println(events.get(0));
						synchronized (manageData) {
							manageData.setHasEvents(subscribers.get(subscriberCount), events.get(0));
						}

					} catch (ClassNotFoundException e) {
						System.out.println("Class Incompatible while acknowledging from subscribed client");
					} finally {
						subscriberCount++;
					}
				}
				events.remove(0);
			}

			// run this function in every one second
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * The main function.
	 */

	public static void main(String[] args) throws IOException {
		new EventManager(5).startService();
	}

}
