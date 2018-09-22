
//package impl;

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

//import demo.*;

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

	public EventManager(int numThreads) throws IOException {
		this.numThreads = numThreads;
		threadPool = Executors.newFixedThreadPool(numThreads);
		thread = new Thread(this);
		runnableThreads = new Runnable[numThreads];
		manageData = new ManageInfo();

		for (int i = 0; i <= numThreads; i++) {
			socketMap.put(i, new ServerSocket(7000 + i + 1));
		}

		for (int i = 0; i < numThreads; i++) {
			runnableThreads[i] = new ManageThread(this, socketMap.get(i));
		}

		System.out.println("Starts");
		thread.start();
	}

	/*
	 * Start the repo service
	 */
	private void startService() throws IOException {
		serverSocket = new ServerSocket(5000);
		int i = 0;
		while (true) {
			System.out.println("Waiting for CLient request");
			Socket clientSocket = serverSocket.accept();

			System.out.println(
					"Connection Established between Server and Client and Clients: " + clientSocket.getInetAddress());
			synchronized (manageData) {

				if (manageData.addClients.contains(clientSocket.getInetAddress())) {
					manageData.addClients.add(clientSocket.getInetAddress());
				} else {
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

	/*
	 * add new topic when received advertisement of new topic
	 */
	public void addTopic(Topic topic) {
		synchronized (manageData) {
			manageData.topics.add(topic);
			manageData.tempTopics.add(topic);
		}
	}

	public ArrayList<Topic> getAllTopics() {
		synchronized (manageData) {
			return manageData.topics;
		}
	}

	/*
	 * add subscriber to the internal list
	 */
	public void addSubscriber(Topic topic, InetAddress subscriber) {
		synchronized (manageData) {
//			manageData.subscribers.add(subscriber);
			manageData.setSubscribedTopics(topic, subscriber);
			manageData.setSubscriberForTopics(topic, subscriber);
		}
	}

	public ArrayList<Topic> listSubscribedTopics(InetAddress subscriber) {
		synchronized (manageData) {
			return manageData.getSubscribedTopics(subscriber);
		}
	}

	/*
	 * remove subscriber from the list
	 */
	public boolean removeSubscriber(Topic topic, InetAddress unSubscriber) {
		synchronized (manageData) {
			if (!manageData.isTopicSubscriberSync(topic, unSubscriber))
				return false;

			manageData.removeSubscribedTopics(topic, unSubscriber);
			manageData.removeSubscriberFromTopics(topic, unSubscriber);
			return true;
		}
	}
//	manageData.removeHasEvents(activeSubscribers.get(0), eventsToSend.get(sendCount));
	

	public boolean removeSubscriber(InetAddress unSubscriber) {
		synchronized (manageData) {
			return manageData.removeAllSubscribers(unSubscriber);
		}
	}

	/*
	 * notify all subscribers of new event
	 */
	public void notifySubscribers(Event event) {
		synchronized (manageData) {
			manageData.events.add(event);
		}
	}

	/*
	 * show the list of subscriber for a specified topic
	 */
	public void showSubscribers(Topic topic) {

	}

	@Override
	public void run() {

		int port = 6000;

		while (true) {
			ArrayList<Event> events = new ArrayList<>();
			ArrayList<Topic> topics = new ArrayList<>();
			ArrayList<InetAddress> clients = new ArrayList<>();
			ArrayList<InetAddress> activeSubscribers = new ArrayList<>();

			synchronized (manageData) {
				topics = manageData.tempTopics;
				events = manageData.events;
				clients = manageData.addClients;
				activeSubscribers = manageData.activeSubscribers;
			}

//			int activeCount = 0;
			while (!activeSubscribers.isEmpty()) {
				ArrayList<Event> eventsToSend = new ArrayList<>();
				synchronized (manageData) {
					eventsToSend = manageData.hasEvents.get(activeSubscribers.get(0));
				}
				int sendCount = 0;
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
					} finally {
						sendCount++;
					}
				}
				activeSubscribers.remove(0);
			}

			int topicCount = 0;
			while (topicCount < topics.size()) {
				int i = 0;
				while (i < clients.size()) {
					try {
						Socket socket = new Socket(clients.get(i++), port);
						outputStream = new ObjectOutputStream(socket.getOutputStream());
						inputStream = new ObjectInputStream(socket.getInputStream());

						outputStream.writeObject("Topic");
						outputStream.writeObject(topics.get(0));
						System.out.println((String) inputStream.readObject());
						topics.remove(0);
						synchronized (manageData) {
							manageData.tempTopics.remove(0);
						}

						outputStream.close();
						inputStream.close();
						socket.close();

					} catch (IOException e) {

						System.out.println("Server unreachable at " + clients.get(i - 1).getHostAddress());

					} catch (ClassNotFoundException e) {

						System.out.println("Class Cast Exception while sending TOPICS to clients");
					}
				}
			}

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

				while (!subscribers.isEmpty()) {
					try {

						Socket socket = new Socket(subscribers.get(0), port);
						outputStream = new ObjectOutputStream(socket.getOutputStream());
						inputStream = new ObjectInputStream(socket.getInputStream());

						outputStream.writeObject("Event");
						outputStream.writeObject(events.get(0));
						System.out.println((String) inputStream.readObject());

						outputStream.close();
						inputStream.close();
						socket.close();

					} catch (IOException e) {
						System.out.println("Server unreachable at " + subscribers.get(0).getHostAddress()
								+ " while sending the event notifications to the subscriber");

						synchronized (manageData) {
							manageData.setHasEvents(subscribers.get(0), events.get(0));
						}

					} catch (ClassNotFoundException e) {
						System.out.println("Class Incompatible while acknowledging from subscribed client");
					} finally {
						subscribers.remove(0);
						events.remove(0);
					}
				}
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new EventManager(5).startService();
	}

}
