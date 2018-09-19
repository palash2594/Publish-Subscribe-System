
package impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import demo.*;

public class EventManager implements Runnable {

	private static ExecutorService threadPool;
	private Thread thread;
	private static Runnable[] runnableThreads;
	private static HashMap<Integer, ServerSocket> socketMap = new HashMap<>();
	private static int numThreads;
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
				manageData.addClients(clientSocket.getInetAddress());
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

			synchronized (manageData) {
				topics = manageData.topics;
				events = manageData.events;
				clients = manageData.addClients;
			}

//			while (topics.size() > 0) {
//				int i = 0;
//				while (i < clients.size()) {
//					try {
//						
//						Socket socket = new Socket(clients.get(i++), port);
//						outputStream = new ObjectOutputStream(socket.getOutputStream());
//						inputStream = new ObjectInputStream(socket.getInputStream());
//						
//						outputStream.writeObject(topics.get(0));
//						
//						outputStream.close();
//						inputStream.close();
//						socket.close();
//					} catch (Exception e) {
//						// TODO: handle exception
//					}
//				}
//				topics.remove(0);
//			}

			while (events.size() > 0) {
				int i = 0;
				Topic topic = events.get(0).getTopic();
				System.out.println(events);
				System.out.println(topic.getName());

				ArrayList<InetAddress> subscribers = new ArrayList<>();

				synchronized (manageData) {
					System.out.println(manageData.subscriberForTopics.size());
//					System.out.println(manageData.subscriberForTopics.containsKey(topic));

					HashMap<Topic, ArrayList<InetAddress>> subscriberTopics = manageData.subscriberForTopics;
					
					for (Map.Entry<Topic, ArrayList<InetAddress>> entry : subscriberTopics.entrySet()) {
						if (entry.getKey().getName().equalsIgnoreCase(topic.getName())) {
							subscribers = entry.getValue();
						}
					}
				}

				System.out.println(subscribers.get(0));

				while (!subscribers.isEmpty()) {
					try {
						Socket socket = new Socket(subscribers.remove(0), port);
						outputStream = new ObjectOutputStream(socket.getOutputStream());
						inputStream = new ObjectInputStream(socket.getInputStream());
						outputStream.writeObject(events.get(0));
						try {
							System.out.println((String) inputStream.readObject());
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						outputStream.close();
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				events.remove(0);
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
