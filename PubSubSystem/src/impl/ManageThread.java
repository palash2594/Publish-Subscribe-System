package impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ManageThread implements Runnable {

	private EventManager eventManager;
	private ServerSocket serverSocket;

	public ManageThread(EventManager eventManager, ServerSocket serverSocket) {
		this.eventManager = eventManager;
		this.serverSocket = serverSocket;
	}

	@Override
	public void run() {
		try {
			Socket clientSocket = serverSocket.accept();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
