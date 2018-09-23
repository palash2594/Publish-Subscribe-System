/**
 * Class to store all the data including list of - subscribers, publishers, topics, events
 *
 * @author Maha Krishnan Krishnan
 * @author Palash Jain
 */

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ManageInfo {

	// List of all the Clients, which came online and has pending messages
	public ArrayList<InetAddress> addClients = new ArrayList<>();

	// List of all the Topics available
	public ArrayList<Topic> topics = new ArrayList<>();
	// List of all the topics yet to be advertised to all the clients
	public ArrayList<Topic> tempTopics = new ArrayList<>();

	// List of all the Events yet to be delivered
	public ArrayList<Event> events = new ArrayList<>();

	// List of all the Subscribers
	public ArrayList<InetAddress> subscribers = new ArrayList<>();
	// Map for subscribers where a list of subscribed topics is maintained for all the subscribers 
	public HashMap<InetAddress, ArrayList<Topic>> topicListForSubscriber = new HashMap<>();
	// Map where list of subscribers is maintained for each topic
	public HashMap<Topic, ArrayList<InetAddress>> subscriberForTopics = new HashMap<>();
	
	/**
	 * to get the list of topics a subscriber has subscribed to.
	 * 
	 * @param subscriber
	 * @return
	 */
	
	public ArrayList<Topic> getSubscribedTopics(InetAddress subscriber) {
		if (topicListForSubscriber.containsKey(subscriber)) {
			return topicListForSubscriber.get(subscriber);
		}
		return null;
	}
	
	/**
	 * to add the topic in the corresponding subscriber's map
	 * 
	 * @param topic instance of the topic
	 * @param subscriber IP address of the subscriber
	 */

	public void setSubscribedTopics(Topic topic, InetAddress subscriber) {
		if (topicListForSubscriber.containsKey(subscriber)) {
			ArrayList<Topic> topics = topicListForSubscriber.get(subscriber);
			topics.add(topic);
			topicListForSubscriber.put(subscriber, topics);
		} else {
			ArrayList<Topic> topics = new ArrayList<>();
			topics.add(topic);
			topicListForSubscriber.put(subscriber, topics);
		}
	}
	
	/**
	 * to add the subscriber in the corresponding topic's map
	 *  
	 * @param topic instance of the topic
	 * @param subscriber IP address of the subscriber
	 */

	public void setSubscriberForTopics(Topic topic, InetAddress subscriber) {

		boolean flag = false;

		for (Map.Entry<Topic, ArrayList<InetAddress>> entry : subscriberForTopics.entrySet()) {
			if (entry.getKey().getName().equalsIgnoreCase(topic.getName())) {
				ArrayList<InetAddress> ip = subscriberForTopics.get(entry.getKey());
				ip.add(subscriber);
				subscriberForTopics.put(entry.getKey(), ip);
				flag = true;
				break;
			}
		}

		if (!flag) {
			ArrayList<InetAddress> ip = new ArrayList<>();
			ip.add(subscriber);
			subscriberForTopics.put(topic, ip);
		}
	}
	
	/**
	 * to check if the given subscriber has subscriber for the given topic
	 * 
	 * @param topic instance of the topic
	 * @param subscriber IP address of the subscriber
	 * @return true if it has subscribed else no
	 */

	public boolean isTopicSubscriberSync(Topic topic, InetAddress subscriber) {
		if (topicListForSubscriber.containsKey(subscriber)) {
			ArrayList<Topic> topics = topicListForSubscriber.get(subscriber);
			for (Topic topic2 : topics) {
				if (topic2.getName().equalsIgnoreCase(topic.getName())) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * to remove a particular topic for a subscriber
	 * 
	 * @param topic instance of the topic
	 * @param subscriber IP address of the subscriber
	 */

	public void removeSubscribedTopics(Topic topic, InetAddress subscriber) {
		if (topicListForSubscriber.containsKey(subscriber)) {
			ArrayList<Topic> topics = topicListForSubscriber.get(subscriber);
			int index = -1;
			for (Topic topic2 : topics) {
				if (topic2.getName().equalsIgnoreCase(topic.getName())) {
					index = topics.indexOf(topic2);
					break;
				}
			}

			if (index >= 0) {
				topics.remove(index);
				topicListForSubscriber.put(subscriber, topics);
			}
		}
	}
	
	/**
	 * remove the subscriber from the topic map that corresponding topic
	 * 
	 * @param topic instance of the topic
	 * @param subscriber IP address of the subscriber
	 */

	public void removeSubscriberFromTopics(Topic topic, InetAddress subscriber) {
		ArrayList<InetAddress> ip = new ArrayList<>();

		for (Map.Entry<Topic, ArrayList<InetAddress>> entry : subscriberForTopics.entrySet()) {
			if (entry.getKey().getName().equalsIgnoreCase(topic.getName())) {
				ip = entry.getValue();
				int index = ip.indexOf(subscriber);
				if (index >= 0) {
					ip.remove(index);
					subscriberForTopics.put(entry.getKey(), ip);
				}
				break;
			}
		}
	}
	
	/**
	 * remove subscribed topics for a given subscriber
	 * 
	 * @param subscriber IP address of the subscriber
	 * @return list of topics unsubscribed from
	 */

	public ArrayList<Topic> removeAllSubscribers(InetAddress subscriber) {

		ArrayList<Topic> topics = new ArrayList<>();

		if (topicListForSubscriber.containsKey(subscriber)) {
			topics = topicListForSubscriber.get(subscriber);
			topicListForSubscriber.remove(subscriber);
		}

		for (Map.Entry<Topic, ArrayList<InetAddress>> entry : subscriberForTopics.entrySet()) {
			ArrayList<InetAddress> ip = entry.getValue();
			if (ip.contains(subscriber)) {
				int index = ip.indexOf(subscriber);
				ip.remove(index);
				subscriberForTopics.put(entry.getKey(), ip);
			}
		}

		return topics;
	}

	// BUFFER for events
	// Map used as a buffer, which stores if any user has a pending events to be delivered
	public HashMap<InetAddress, ArrayList<Event>> hasEvents = new HashMap<>();
	// List of subscribers if they have any pending events yet to be delivered 
	public ArrayList<InetAddress> activeSubscribers = new ArrayList<>();

	public void setHasEvents(InetAddress subscriber, Event event) {
		if (hasEvents.containsKey(subscriber)) {
			ArrayList<Event> events = hasEvents.get(subscriber);
			events.add(event);
			hasEvents.put(subscriber, events);
		} else {
			ArrayList<Event> events = new ArrayList<>();
			events.add(event);
			hasEvents.put(subscriber, events);
		}
	}

	public void removeHasEvents(InetAddress subscriber, Event event) {
		ArrayList<Event> events = hasEvents.get(subscriber);
		for (Event e : events) {
			if (e.getId() == event.getId()) {
				int index = events.indexOf(e);
				events.remove(index);
				hasEvents.put(subscriber, events);
				break;
			}
		}
	}
	
	// BUFFER for topic
	// Map used as a buffer, which stores if any user has a pending topics to be delivered
	public HashMap<InetAddress, ArrayList<Topic>> hasTopics = new HashMap<>();
	// List of subscribers if they have any pending topics yet to be delivered 
	public ArrayList<InetAddress> activeClients= new ArrayList<>();
	
	public void setHasTopics(InetAddress client, Topic topic) {
		if (hasTopics.containsKey(client)) {
			ArrayList<Topic> topics = hasTopics.get(client);
			topics.add(topic);
			hasTopics.put(client, topics);
		} else {
			ArrayList<Topic> topics = new ArrayList<>();
			topics.add(topic);
			hasTopics.put(client, topics);
		}
	}
	
	public void removeHasTopics(InetAddress client, Topic topic) {
		ArrayList<Topic> topics = hasTopics.get(client);
		for (Topic t : topics) {
			if (t.getName().equalsIgnoreCase(topic.getName())) {
				topics.remove(topics.indexOf(t));
				hasTopics.put(client, topics);
				break;
			}
		}
	}
}
