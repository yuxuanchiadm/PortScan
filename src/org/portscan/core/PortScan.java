package org.portscan.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.portscan.gui.JPortScan;

public class PortScan implements Runnable {
	private JPortScan jPortScan;

	private String ip;
	private Executor executor = Executors.newCachedThreadPool();

	public PortScan() {
		jPortScan = new JPortScan();
		jPortScan.pack();
	}

	public static void main(String[] args) {
		PortScan portScan = new PortScan();
		portScan.run();
	}

	@Override
	public void run() {
		while (true) {
			jPortScan.waitForStart();
			ip = jPortScan.getIP();
			int currentIdx = 1;
			while (currentIdx <= 65535) {
				int startPort = currentIdx;
				executor.execute(() -> {
					for (int port = startPort; port < startPort + 1000 && port <= 65535; port++) {
						Socket socket = new Socket();
						try {
							socket.connect(new InetSocketAddress(ip, port), 50);
							socket.close();
						} catch (IOException e) {
							continue;
						} finally {
							jPortScan.countDone();
						}
						jPortScan.addOk(port);
					}
				});
				currentIdx += 1000;
			}
		}
	}
}
