package hfy;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client {// 私聊//name:mess 发图片 --地址

	public PrintStream printStream = null;
	FileInputStream fis = null;
	BufferedOutputStream bos = null;
	BufferedInputStream bis = null;
	public Socket socket = null;
	public JTextField address;
	public JTextField port;
	public JTextField name;
	public JTextArea tshow;
	JComboBox<String> cmb;

	public JTextField input;
	public JFrame frame;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		@SuppressWarnings("unused")
		Client client = new Client();
	}

	public Client() throws HeadlessException {
		Init();
	}

	public void Init() {

		frame = new JFrame("一口好牙客户端");
		frame.setSize(700, 520);
		frame.setLayout(null);

		JLayeredPane pane = new JLayeredPane();
		ImageIcon image = new ImageIcon("1.jpg");
		JLabel abc = new JLabel(image);
		abc.setBounds(0, 0, image.getIconWidth(), image.getIconHeight());
		JPanel panel1 = new JPanel();
		panel1.setBounds(0, 0, image.getIconWidth(), image.getIconHeight());
		panel1 = (JPanel) frame.getContentPane();
		panel1.add(abc);
		pane.add(panel1, JLayeredPane.DEFAULT_LAYER);
		frame.setLayeredPane(pane);

		JLabel label0 = new JLabel("牙位一览图:"); // 帮助
		label0.setLocation(550, 120);
		label0.setSize(100, 20);
		pane.add(label0, JLayeredPane.MODAL_LAYER);

		JLabel label1 = new JLabel("地址:"); // 客户地址
		label1.setLocation(5, 0);
		label1.setSize(35, 20);
		pane.add(label1, JLayeredPane.MODAL_LAYER);

		address = new JTextField("localhost");
		address.setLocation(40, 2);
		address.setSize(80, 20);
		pane.add(address, JLayeredPane.MODAL_LAYER);

		JLabel label2 = new JLabel("名字:"); // 客户聊天室名字
		label2.setLocation(135, 0);
		label2.setSize(35, 20);
		pane.add(label2, JLayeredPane.MODAL_LAYER);

		name = new JTextField(20);
		name.setLocation(170, 2);
		name.setSize(80, 20);
		pane.add(name, JLayeredPane.MODAL_LAYER);

		JLabel label3 = new JLabel("身份:"); // 客户身份
		label3.setLocation(265, 0);
		label3.setSize(35, 20);
		pane.add(label3, JLayeredPane.MODAL_LAYER);

		cmb = new JComboBox<String>();
		cmb.setLocation(300, 2);
		cmb.setSize(80, 20);
		cmb.addItem("就诊者");
		cmb.addItem("医生");
		cmb.addItem("技师");
		cmb.addItem("工厂");
		cmb.addItem("其他");
		pane.add(cmb, JLayeredPane.MODAL_LAYER);

		JButton bcon = new JButton("连接");// 连接按钮
		bcon.setLocation(400, 2);
		bcon.setSize(60, 20);
		bcon.addActionListener(new bconListener());

		pane.add(bcon, JLayeredPane.MODAL_LAYER);

		// JTextField port = new JTextField("30000", 5);
		tshow = new JTextArea();// 展示窗口
		JScrollPane panel = new JScrollPane(tshow, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.setLocation(0, 25);
		panel.setSize(550, 410);
		tshow.setLineWrap(true);
		tshow.setWrapStyleWord(true);

		tshow.setEditable(false);
		pane.add(panel, JLayeredPane.MODAL_LAYER);

		input = new JTextField(20);
		input.setLocation(80, 440);
		input.setSize(300, 20);
		pane.add(input, JLayeredPane.MODAL_LAYER);

		JButton bsend = new JButton("发送");// 发送按钮
		bsend.setLocation(400, 440);
		bsend.setSize(60, 20);
		bsend.addActionListener(new bsendListener());
		pane.add(bsend, JLayeredPane.MODAL_LAYER);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				PrintStream printStream;
				try {
					if (socket.getOutputStream() != null) {
						printStream = new PrintStream(socket.getOutputStream());
						printStream.println("quit");
						socket.shutdownInput();
					}

				} catch (IOException e2) {
					// TODO: handle exception
					e2.printStackTrace();
				}
				close();
				System.exit(1);
			}
		});

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public void close() {// 关闭
		if (printStream != null) {
			printStream.close();
			try {
				if (socket != null) {
					socket.close();
					fis.close(); 
					/*bos.close(); bis.close();
					 */

				}
			} catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

	// 连接按钮监听
	class bconListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			connect();
			sendName();
			tshow.append("连接成功 name:"+(String) cmb.getSelectedItem()+" " + name.getText() + "\n");
			Recive r = new Recive(); // 监听服务器消息
			new Thread(r).start();
		}
	}

	class Recive implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			recive();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

	// 连接
	public void connect() {
		try {
			socket = new Socket(address.getText(), 42900);
			frame.setTitle("一口好牙聊天室 "+(String) cmb.getSelectedItem()+" "+name.getText());
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	// 向服务器申明名字
	public void sendName() {
		String name = (String) cmb.getSelectedItem() + " " + this.name.getText();
		PrintStream printStream;
		try {
			printStream = new PrintStream(socket.getOutputStream());
			printStream.println(name);
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void recive() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String string = "";
			while ((string = br.readLine()) != null) {
				tshow.append(string + "\n");
			}
		} catch (IOException e) {
			// TODO: handle exception
			System.exit(1);
		}
	}

	class bsendListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			try {
				PrintStream printStream = new PrintStream(socket.getOutputStream());

				// 私聊则添加私聊标志 //doctor:hello
				if (input.getText().indexOf(":") > 0 && input.getText().startsWith("//")) {
					String s = input.getText();
					s = s.substring(2);
					printStream.println(SockPro.PRIVATE_SEND + s.split(":")[0] + SockPro.SPLIT_SIGN + s.split(":")[1]
							+ SockPro.PRIVATE_SEND);
					// 发送图片则添加图片标志 --地址
				} else if (input.getText().startsWith("--")) {
					System.out.println("pt");
					String s = "发送图片:"+input.getText();
					printStream.println(
							SockPro.SEND_MESS + s +  SockPro.SEND_MESS);
					fis = new FileInputStream(new File(s));
					byte[] b = new byte[1024];
					int len;
					while ((len = fis.read(b)) != -1) {
						printStream.write(b, 0, len);
					}
					socket.shutdownOutput();
				} else {//默认-公聊
					printStream.println(SockPro.SEND_MESS + input.getText() + SockPro.SEND_MESS);
				}
				tshow.append("我说:" + input.getText() + "\n");
				input.setText("");
			} catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
}
