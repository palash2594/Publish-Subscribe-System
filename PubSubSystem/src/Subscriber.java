/**
 * The Subscriber Interface
 * 
 * @author Maha Krishnan Krishnan
 * @author Palash Jain
 */

import java.io.IOException;
import java.net.UnknownHostException;

public interface Subscriber {
	/*
	 * subscribe to a topic
	 */
	public void subscribe(Topic topic) throws UnknownHostException, IOException;
	/*
	 * unsubscribe from a topic 
	 */
	public void unsubscribe(Topic topic) throws UnknownHostException, IOException;
	
	/*
	 * unsubscribe to all subscribed topics
	 */
	public void unsubscribe() throws UnknownHostException, IOException;
	
	/*
	 * show the list of topics current subscribed to 
	 */
	public void listSubscribedTopics() throws UnknownHostException, IOException;
	
}
