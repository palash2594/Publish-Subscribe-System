package demo;

import java.io.IOException;
import java.net.UnknownHostException;

public interface Publisher {
	/*
	 * publish an event of a specific topic with title and content
	 */
	public void publish(Event event) throws UnknownHostException, IOException;
	
	/*
	 * advertise new topic
	 */
	public void advertise(Topic newTopic) throws UnknownHostException, IOException;
}
