
//package impl;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//import demo.*;

public class ManageInfo {

	// All Clients
	public static ArrayList<InetAddress> addClients = new ArrayList<>();

	// Topics
	public static ArrayList<Topic> topics = new ArrayList<>();
	public static ArrayList<Topic> tempTopics = new ArrayList<>();

	// Events
	public static ArrayList<Event> events = new ArrayList<>();

	// Subscribers
	public static ArrayList<InetAddress> subscribers = new ArrayList<>();
	public static HashMap<InetAddress, ArrayList<Topic>> topicListForSubscriber = new HashMap<>();
	public static HashMap<Topic, ArrayList<InetAddress>> subscriberForTopics = new HashMap<>();

	public ArrayList<Topic> getSubscribedTopics(InetAddress subscriber) {
		if (topicListForSubscriber.containsKey(subscriber)) {
			System.out.println("Inside getSubscribedTopics::" + subscriber);
			return topicListForSubscriber.get(subscriber);
		}
		return null;
	}

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

	// Buffer for events
	public static HashMap<InetAddress, ArrayList<Event>> hasEvents = new HashMap<>();
	public static ArrayList<InetAddress> activeSubscribers = new ArrayList<>();

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
	
	// Buffer for topics
	
	public static HashMap<InetAddress, ArrayList<Topic>> hasTopics = new HashMap<>();
	public static ArrayList<InetAddress> activeClients= new ArrayList<>();
	
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

	// =========================================================================================================================================================
//	// TO DO
//	Map<String, ArrayList<String>> keywords = new HashMap<>();
//
//	
//
//	ArrayList<InetAddress> publishers = new ArrayList<>();
//
//	Map<InetAddress, ArrayList<Event>> hasEvents = new HashMap<>();
//
//	Map<Event, Integer> pendingEventsCount = new HashMap<>();
//
//	public ArrayList<String> getKeywords(String keyword) {
//		return keywords.get(keyword);
//	}
//
//	public void setKeywords(String keyword, String topic) {
//
//		if (keywords.containsKey(keyword)) {
//			ArrayList<String> topicsFor = keywords.get(keyword);
//			topicsFor.add(topic);
//			keywords.put(keyword, topicsFor);
//		} else {
//			ArrayList<String> topicsFor = new ArrayList<>();
//			topicsFor.add(topic);
//			keywords.put(keyword, topicsFor);
//		}
//	}
//
//	public ArrayList<Event> getEvents() {
//		return events;
//	}
//
//	public void setEvents(Event event) {
//		this.events.add(event);
//	}
//
//	public ArrayList<InetAddress> getSubscribers() {
//		return subscribers;
//	}
//
//	public void setSubscribers(InetAddress subscriber) {
//		this.subscribers.add(subscriber);
//	}
//
//	public List<InetAddress> getPublishers() {
//		return publishers;
//	}
//
//	public void setPublishers(InetAddress publisher) {
//		this.publishers.add(publisher);
//	}
//
//	// DOubt
//	public Map<Topic, ArrayList<InetAddress>> getSubscriberForTopics() {
//		return subscriberForTopics;
//	}
//
//	public Map<InetAddress, ArrayList<Event>> getHasEvents() {
//		return hasEvents;
//	}
//
//	public void setHasEvents(Map<InetAddress, ArrayList<Event>> hasEvents) {
//		this.hasEvents = hasEvents;
//	}
//
//	public Map<Event, Integer> getPendingEventsCount() {
//		return pendingEventsCount;
//	}
//
//	public void setPendingEventsCount(Map<Event, Integer> pendingEventsCount) {
//		this.pendingEventsCount = pendingEventsCount;
//	}

}
