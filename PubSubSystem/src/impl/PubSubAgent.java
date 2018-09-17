package impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import demo.*;

public class PubSubAgent implements Publisher, Subscriber{
	
	private Socket clientSocket;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private int serverPort;
    private static ArrayList<Topic> topicList;
	
	public PubSubAgent() throws UnknownHostException, IOException {
		
		connectToSocket(5000);
		serverPort = inputStream.readInt();
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
	public void unsubscribe(Topic topic) throws UnknownHostException, IOException{
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
	public void unsubscribe() throws UnknownHostException, IOException{
		connectToSocket(serverPort);
		
	}

	@Override
	public void listSubscribedTopics() throws UnknownHostException, IOException{
		connectToSocket(serverPort);


		
		disconnectFromSocket();
		
	}

	@Override
	public void publish(Event event) throws UnknownHostException, IOException{
		connectToSocket(serverPort);

        outputStream.writeObject("Publish");
        try {
            System.out.println((String) inputStream.readObject());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        disconnectFromSocket();
		
	}

    public void listAllTopics() throws UnknownHostException, IOException {
        connectToSocket(serverPort);

        outputStream.writeObject("List all topics");
        try {
            topicList = (ArrayList<Topic>) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        for (Topic topic : topicList) {
            System.out.println(topic);
        }
        disconnectFromSocket();
    }

	@Override
	public void advertise(Topic newTopic) throws UnknownHostException, IOException{
		connectToSocket(serverPort);

        outputStream.writeObject("Topic");
        outputStream.writeObject(newTopic);
        try {
            System.out.println((String) inputStream.readObject());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        disconnectFromSocket();
		
	}
	
	public void connectToSocket(int port) throws UnknownHostException, IOException {
		
		clientSocket = new Socket( "localhost", 5000 );
		outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
		inputStream = new ObjectInputStream(clientSocket.getInputStream());
		
	}
	
	public void disconnectFromSocket() throws IOException {
		inputStream.close();
		outputStream.close();
		clientSocket.close();
	}

	
	public static void main(String[] args) throws UnknownHostException, IOException {
		
		PubSubAgent psa = new PubSubAgent();
        System.out.println("What do want to do \n 1. Puslish an event\n " +
                "2. Advertise a topic \n 3. Subscribe to a topic \n 4. Unsubscribe from a topic \n" +
                "5. Unsubscribe from all topic \n 6. Show list of all subscribed topics \n" +
                "7. List of all available topics");

        Scanner src = new Scanner(System.in);
        int ch = src.nextInt();

        // TODO: 15-09-2018 put a while loop here   
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
            case 2: System.out.println("Enter topic name and keywords");
                t = new Topic();
                String topicName = src.next();
                t.setId(topicName.hashCode());
                t.setName(topicName);
                t.setKeywords(null);
                psa.advertise(t);
                break;
            case 3:  System.out.println("Do you wish to see the list of topics yes / no");
                answer = src.next();
                if (answer.equalsIgnoreCase("yes")) {
                    psa.listAllTopics();
                }

                topic = src.next();

                for (Topic topic1 : topicList) {
                    if (topic1.getName().equals(topic)) {
                        psa.subscribe(topic1);
                        break;
                    }
                }
                break;
            case 4: System.out.println("Do you wish to see the list of topics yes / no");
                answer = src.next();
                if (answer.equalsIgnoreCase("yes")) {
                    psa.listAllTopics();
                }

                topic = src.next();

                for (Topic topic1 : topicList) {
                    if (topic1.getName().equals(topic)) {
                        psa.unsubscribe(topic1);
                        break;
                    }
                }
                System.out.println("No such topic exists");
                break;
            case 5: // do something
                break;
            case 6: // do something
                break;
            case 7: psa.listAllTopics();
                break;

        }

		
	}

	
}
