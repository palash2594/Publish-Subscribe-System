package impl;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import demo.*;

public class ManageInfo {

	// class to store all general information

	ArrayList<Topic> topics = new ArrayList<>();

	Map<String, ArrayList<String>> keywords = new HashMap<>();

	ArrayList<Event> events = new ArrayList<>();

	ArrayList<InetAddress> addClients = new ArrayList<>();

	ArrayList<InetAddress> subscribers = new ArrayList<>();

	ArrayList<InetAddress> publishers = new ArrayList<>();

	Map<Topic, ArrayList<InetAddress>> subscriberForTopics = new HashMap<>();

	Map<PubSubAgent, ArrayList<Event>> hasEvents = new HashMap<>();

	Map<Event, Integer> pendingEventsCount = new HashMap<>();

	public ArrayList<Topic> getTopics() {
		return topics;
	}

	public void setTopics(Topic topic) {
		this.topics.add(topic);
	}

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

	public boolean removeSubscriberFromTopics(Topic topic, InetAddress subscriber) {
		if (subscriberForTopics.containsKey(topic)) {
			ArrayList<InetAddress> ip = subscriberForTopics.get(topic);
			int index = ip.indexOf(subscriber);
			if (index >= 0) {
				ip.remove(index);
				subscriberForTopics.put(topic, ip);
				return true;
			}
		}
		return false;
	}

	public Map<PubSubAgent, ArrayList<Event>> getHasEvents() {
		return hasEvents;
	}

	public void setHasEvents(Map<PubSubAgent, ArrayList<Event>> hasEvents) {
		this.hasEvents = hasEvents;
	}

	public Map<Event, Integer> getPendingEventsCount() {
		return pendingEventsCount;
	}

	public void setPendingEventsCount(Map<Event, Integer> pendingEventsCount) {
		this.pendingEventsCount = pendingEventsCount;
	}

}
