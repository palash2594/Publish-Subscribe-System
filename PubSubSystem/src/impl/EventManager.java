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
			socketMap.put(i, new ServerSocket(8000 + i + 1));
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
					"Connection Established between Server and Client and Clients" + clientSocket.getInetAddress());
			synchronized (manageData) {
				manageData.addClients(clientSocket.getInetAddress());
			}

			outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
			inputStream = new ObjectInputStream(clientSocket.getInputStream());

			ServerSocket getSocket = socketMap.get(i);
			System.out.println(getSocket.getLocalPort());
			outputStream.writeObject(getSocket.getLocalPort());
			threadPool.execute(runnableThreads[i]);
			i = (i + 1) % numThreads;

		}
	}

	/*
	 * notify all subscribers of new event
	 */
	public void notifySubscribers(Event event) {

	}

	/*
	 * add new topic when received advertisement of new topic
	 */
	public void addTopic(Topic topic) {
		synchronized (manageData) {
			manageData.topics.add(topic);
		}
	}

	/*
	 * add subscriber to the internal list
	 */
	public void addSubscriber(Topic topic, InetAddress subscriber) {
		synchronized (manageData) {
			manageData.subscribers.add(subscriber);
			manageData.setSubscriberForTopics(topic, subscriber);
		}
	}

	/*
	 * remove subscriber from the list
	 */
	public boolean removeSubscriber(Topic topic, InetAddress unSubscriber) {
		synchronized (manageData) {
			return manageData.removeSubscriberFromTopics(topic, unSubscriber);
		}
	}

	/*
	 * show the list of subscriber for a specified topic
	 */
	public void showSubscribers(Topic topic) {

	}

	public ArrayList<Topic> getAllTopics() {
		synchronized (manageData) {
			return manageData.getTopics();
		}
	}

	@Override
	public void run() {

		int port = 6000;

	}

	public static void main(String[] args) throws IOException {
		new EventManager(5).startService();
	}

}
