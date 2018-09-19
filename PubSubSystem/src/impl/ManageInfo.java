
package impl;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import demo.*;

public class ManageInfo {

	// All Clients
	ArrayList<InetAddress> addClients = new ArrayList<>();

	// Topics
	ArrayList<Topic> topics = new ArrayList<>();

	// Subscribers
	ArrayList<InetAddress> subscribers = new ArrayList<>();
	HashMap<InetAddress, ArrayList<Topic>> topicListForSubscriber = new HashMap<>();
	HashMap<Topic, ArrayList<InetAddress>> subscriberForTopics = new HashMap<>();

	public ArrayList<Topic> getSubscribedTopics(InetAddress subscriber) {
		if (topicListForSubscriber.containsKey(subscriber)) {
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
		if (subscriberForTopics.containsKey(topic)) {
			ArrayList<InetAddress> ip = subscriberForTopics.get(topic);
			ip.add(subscriber);
			subscriberForTopics.put(topic, ip);
		} else {
			ArrayList<InetAddress> ip = new ArrayList<>();
			ip.add(subscriber);
			subscriberForTopics.put(topic, ip);
		}
	}

	public boolean isTopicSubscriberSync(Topic topic, InetAddress subscriber) {
		if (topicListForSubscriber.containsKey(subscriber)) {
			ArrayList<Topic> topics = topicListForSubscriber.get(subscriber);
			if (topics.indexOf(topic) >= 0)
				return true;
		}
		return false;
	}

	public void removeSubscribedTopics(Topic topic, InetAddress subscriber) {
		if (topicListForSubscriber.containsKey(subscriber)) {
			ArrayList<Topic> topics = topicListForSubscriber.get(subscriber);
			int index = topics.indexOf(topic);
			if (index >= 0) {
				topics.remove(index);
				topicListForSubscriber.put(subscriber, topics);
//				return true;
			}
		}
//		return false;
	}

	public void removeSubscriberFromTopics(Topic topic, InetAddress subscriber) {
		if (subscriberForTopics.containsKey(topic)) {
			ArrayList<InetAddress> ip = subscriberForTopics.get(topic);
			int index = ip.indexOf(subscriber);
			if (index >= 0) {
				ip.remove(index);
				subscriberForTopics.put(topic, ip);
//				return true;
			}
		}
//		return false;
	}

	public boolean removeAllSubscribers(InetAddress subscriber) {
		boolean flag = false;
		if (topicListForSubscriber.containsKey(subscriber)) {
			topicListForSubscriber.remove(subscriber);
			flag = true;
		}
		if (!flag)
			return flag;

		for (int i = 0; i < subscriberForTopics.size(); i++) {
			ArrayList<InetAddress> list = subscriberForTopics.get(i);
			if (list.contains(subscriber)) {
				list.remove(list.indexOf(subscriber));
			}
		}

		return flag;
	}

	// =========================================================================================================================================================
	// TO DO
	Map<String, ArrayList<String>> keywords = new HashMap<>();

	ArrayList<Event> events = new ArrayList<>();

	ArrayList<InetAddress> publishers = new ArrayList<>();

	Map<InetAddress, ArrayList<Event>> hasEvents = new HashMap<>();

	Map<Event, Integer> pendingEventsCount = new HashMap<>();

	public ArrayList<String> getKeywords(String keyword) {
		return keywords.get(keyword);
	}

	public void setKeywords(String keyword, String topic) {

		if (keywords.containsKey(keyword)) {
			ArrayList<String> topicsFor = keywords.get(keyword);
			topicsFor.add(topic);
			keywords.put(keyword, topicsFor);
		} else {
			ArrayList<String> topicsFor = new ArrayList<>();
			topicsFor.add(topic);
			keywords.put(keyword, topicsFor);
		}
	}

	public ArrayList<Event> getEvents() {
		return events;
	}

	public void setEvents(Event event) {
		this.events.add(event);
	}

	public ArrayList<InetAddress> getClients() {
		return addClients;
	}

	public void addClients(InetAddress client) {
		this.addClients.add(client);
	}

	public ArrayList<InetAddress> getSubscribers() {
		return subscribers;
	}

	public void setSubscribers(InetAddress subscriber) {
		this.subscribers.add(subscriber);
	}

	public List<InetAddress> getPublishers() {
		return publishers;
	}

	public void setPublishers(InetAddress publisher) {
		this.publishers.add(publisher);
	}

	// DOubt
	public Map<Topic, ArrayList<InetAddress>> getSubscriberForTopics() {
		return subscriberForTopics;
	}

	public Map<InetAddress, ArrayList<Event>> getHasEvents() {
		return hasEvents;
	}

	public void setHasEvents(Map<InetAddress, ArrayList<Event>> hasEvents) {
		this.hasEvents = hasEvents;
	}

	public Map<Event, Integer> getPendingEventsCount() {
		return pendingEventsCount;
	}

	public void setPendingEventsCount(Map<Event, Integer> pendingEventsCount) {
		this.pendingEventsCount = pendingEventsCount;
	}

}
