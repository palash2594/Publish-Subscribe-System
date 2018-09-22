
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

	/*
	 * add new topic when received advertisement of new topic
	 */
	public String addTopic(Topic topic) {
		synchronized (manageData) {
			manageData.topics.add(topic);
			manageData.tempTopics.add(topic);
		}
		return "Topic '" + topic.getName() + "' Advertised sucessfully";
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
		System.out.println(subscriber);
		synchronized (manageData) {
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
			if (!manageData.isTopicSubscriberSync(topic, unSubscriber)) {
				return false;
			}

			manageData.removeSubscribedTopics(topic, unSubscriber);
			manageData.removeSubscriberFromTopics(topic, unSubscriber);
			return true;
		}
	}
//	manageData.removeHasEvents(activeSubscribers.get(0), eventsToSend.get(sendCount));
	

	public ArrayList<Topic> removeSubscriber(InetAddress unSubscriber) {
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
			ArrayList<InetAddress> activeClients = new ArrayList<>();

			synchronized (manageData) {
				topics = manageData.tempTopics;
				events = manageData.events;
				clients = manageData.addClients;
				activeSubscribers = manageData.activeSubscribers;
				activeClients = manageData.activeClients;
			}
			
			
			while(!activeClients.isEmpty()) {
				ArrayList<Topic> topicToSend = new ArrayList<>();
				synchronized (manageData) {
					topicToSend = manageData.hasTopics.get(activeClients.get(0));
				}
				
				int sendCount = 0;
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
						System.out.println("Connection Error with Client");
						
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						sendCount++;
//						topicToSend.remove(0);
					}
					
				}
				activeClients.remove(0);
			}
			

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
//						eventsToSend.remove(0);
					}
				}
				activeSubscribers.remove(0);
			}
			
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
				System.out.println(subscribers.size());
				while (subscriberCount < subscribers.size()) {
					System.out.println("Sub size" + subscribers.size());
					System.out.println("sub count" + subscriberCount);
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
