package impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import demo.Event;

public class ManageInfo {
	
	// class to store all general information
	
	List<String> Topics = new ArrayList<String>();
	Map<String, ArrayList<String>> keywords = new HashMap<>();
	Queue<Event> events = new LinkedList<Event>();
	List<PubSubAgent> subscribers = new ArrayList<>();
	List<PubSubAgent> publishers = new ArrayList<>();
	Map<String, ArrayList<PubSubAgent>> topicsForSubcribers = new HashMap<>();
	Map<PubSubAgent, ArrayList<Event>> hasEvents = new HashMap<>();
	Map<Event, Integer> pendingEventsCount = new HashMap<>();
	
	public ManageInfo() {
		
	}
	
	public List<String> getTopics() {
		return Topics;
	}
	public void setTopics(List<String> topics) {
		this.Topics = topics;
	}
	public Map<String, ArrayList<String>> getKeywords() {
		return keywords;
	}
	public void setKeywords(Map<String, ArrayList<String>> keywords) {
		this.keywords = keywords;
	}
	public Queue<Event> getEvents() {
		return events;
	}
	public void setEvents(Queue<Event> events) {
		this.events = events;
	}
	public List<PubSubAgent> getSubscribers() {
		return subscribers;
	}
	public void setSubscribers(List<PubSubAgent> subscribers) {
		this.subscribers = subscribers;
	}
	public List<PubSubAgent> getPublishers() {
		return publishers;
	}
	public void setPublishers(List<PubSubAgent> publishers) {
		this.publishers = publishers;
	}
	public Map<String, ArrayList<PubSubAgent>> getTopicsForSubcribers() {
		return topicsForSubcribers;
	}
	public void setTopicsForSubcribers(Map<String, ArrayList<PubSubAgent>> topicsForSubcribers) {
		this.topicsForSubcribers = topicsForSubcribers;
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
