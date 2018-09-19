
package impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import demo.*;

public class EventManager implements Runnable {

	private static ExecutorService threadPool;
	private static Thread thread;
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
//		while (!true) {
//
//			// To extract the list of subscribers to whom the event needs to be delivered
//			Event event;
//			ArrayList<InetAddress> subscriberList = new ArrayList<>();
//			int count = 0;
//			synchronized (manageData) {
//				event = manageData.events.remove(0);
//				subscriberList = manageData.subscriberForTopics.get(event.getTopic());
//			}
//			int i = 0;
//			while (i < subscriberList.size()) {
//				try {
//					Socket socket = new Socket(subscriberList.get(i), port);
//
//					outputStream = new ObjectOutputStream(socket.getOutputStream());
////					inputStream = new ObjectInputStream(socket.getInputStream());
//
//					outputStream.writeObject(event);
//
//					socket.close();
//
//				} catch (IOException e) {
//					// If subscriber is offline, store the subscriber and event in the buffer
//					synchronized (manageData) {
//						if (manageData.hasEvents.containsKey(subscriberList.get(i))) {
//							ArrayList<Event> events = manageData.hasEvents.get(subscriberList.get(i));
//							events.add(event);
//							manageData.hasEvents.put(subscriberList.get(i), events);
//						} else {
//							ArrayList<Event> events = new ArrayList<>();
//							events.add(event);
//							manageData.hasEvents.put(subscriberList.get(i), events);
//						}
//						
//						
//					}
//				}
//				i++;
//			}
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}

	}

	public static void main(String[] args) throws IOException {
		new EventManager(5).startService();
	}

}
