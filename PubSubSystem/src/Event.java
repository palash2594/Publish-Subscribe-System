/**
 * The Event Class
 * 
 * @author Maha Krishnan Krishnan
 * @author Palash Jain
 */

import java.io.Serializable;

public class Event implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private Topic topic;
	private String title;
	private String content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

	@Override
	public String toString() {
		return "Event [topic=" + topic + ", title=" + title + ", content=" + content + "]";
	}
    
    
}
