package org.portscan.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class JPortScan extends JFrame {
	private static final long serialVersionUID = 1L;

	private JTextField counterTextField;
	private JTextField ipTextField;
	private JTextPane infoTextPane;

	private boolean isStarted;
	private Lock startLock = new ReentrantLock();
	private Condition startCondition = startLock.newCondition();

	private Lock uiLock = new ReentrantLock();

	private AtomicInteger countDone = new AtomicInteger(0);
	private JButton startButton;

	public JPortScan() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("PortScan");

		JPanel mainPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) mainPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		Box verticalBox = Box.createVerticalBox();
		mainPanel.add(verticalBox);

		JPanel ipPanel = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) ipPanel.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEFT);
		verticalBox.add(ipPanel);

		JLabel ipLabel = new JLabel("IP:");
		ipPanel.add(ipLabel);

		ipTextField = new JTextField();
		ipPanel.add(ipTextField);
		ipTextField.setColumns(20);

		JPanel counterPanel = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) counterPanel.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		verticalBox.add(counterPanel);

		JLabel counterLabel = new JLabel("Counter:");
		counterPanel.add(counterLabel);

		counterTextField = new JTextField();
		counterTextField.setEditable(false);
		counterPanel.add(counterTextField);
		counterTextField.setColumns(20);

		JPanel infoPanel = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) infoPanel.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		verticalBox.add(infoPanel);

		JLabel infoLabel = new JLabel("Info:");
		infoPanel.add(infoLabel);

		infoTextPane = new JTextPane();
		infoTextPane.setPreferredSize(new Dimension(500, 300));
		infoTextPane.setEditable(false);

		JScrollPane infoScrollPane = new JScrollPane();
		infoScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		infoScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		infoScrollPane.setViewportView(infoTextPane);
		infoPanel.add(infoScrollPane);

		JPanel controlPanel = new JPanel();
		getContentPane().add(controlPanel, BorderLayout.SOUTH);

		startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				countDone.set(0);
				counterTextField.setText("");
				ipTextField.setEnabled(false);
				startButton.setEnabled(false);
				isStarted = true;
				startLock.lock();
				try {
					startCondition.signalAll();
				} finally {
					startLock.unlock();
				}
			}
		});
		controlPanel.add(startButton);
	}

	public void waitForStart() {
		setVisible(true);
		startLock.lock();
		try {
			while (!isStarted)
				startCondition.awaitUninterruptibly();
			isStarted = false;
		} finally {
			startLock.unlock();
		}
	}

	public String getIP() {
		return ipTextField.getText();
	}

	public void addOk(int port) {
		uiLock.lock();
		try {
			SwingUtilities.invokeLater(() -> {
				infoTextPane.setText(infoTextPane.getText() + "Ok: " + port + "\n");
			});
		} finally {
			uiLock.unlock();
		}
	}

	public void countDone() {
		uiLock.lock();
		try {
			int current = countDone.incrementAndGet();
			SwingUtilities.invokeLater(() -> {
				counterTextField.setText(current + "/65535");
				if (current == 65535) {
					ipTextField.setEnabled(true);
					startButton.setEnabled(true);
				}
			});
		} finally {
			uiLock.unlock();
		}
	}
}
