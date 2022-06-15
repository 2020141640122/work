package hfy;

import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Server {

	private JTextArea tfile1;
	private JTextArea tshow;
	private Socket socket = null;
	private Socket socket1 = null;
	private ServerSocket sSock;
	private FileOutputStream fos = null;
	protected static Hashtable<String, Socket> hashtable = new Hashtable<String, Socket>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		@SuppressWarnings("unused")
		Server server = new Server();
	}

	public Server() throws HeadlessException {
		Init();
		Start();
	}

	public void Init() {
		JFrame frame = new JFrame("一口好牙服务器");
		frame.setSize(514, 583);
		frame.setLayout(null);

		JLabel label1 = new JLabel("现有人数:"); // 在线客户数
		label1.setLocation(0, 0);
		label1.setSize(60, 20);
		frame.add(label1);

		tfile1 = new JTextArea(); // 在线客户数文本框
		tfile1.setLocation(60, 2);
		tfile1.setSize(30, 20);
		tfile1.setEditable(false);
		frame.add(tfile1);

		tshow = new JTextArea();// 运行记录文本框
		JScrollPane panel = new JScrollPane(tshow, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.setLocation(0, 25);
		panel.setSize(500, 520);
		tshow.setLineWrap(true);
		tshow.setWrapStyleWord(true);
		tshow.setEditable(false);
		frame.add(panel);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(-1);
			}
		});
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	// 启动服务器
	public void Start() {
		try {
			sSock = new ServerSocket(42900);
			while (true) {
				socket = sSock.accept();
				Service ser = new Service(socket);
				new Thread(ser).start();
			}
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void boradCast(String mess, Socket self) {
		Enumeration<String> enumeration = hashtable.keys();
		System.out.println("本聊天室共有" + hashtable.size() + "人");
		PrintStream printStream = null;
		tshow.append(mess + "\n");
		while (enumeration.hasMoreElements()) {
			String s = (String) enumeration.nextElement();
			socket1 = (Socket) hashtable.get(s);
			if (socket1 != self) {
				try {
					printStream = new PrintStream(socket1.getOutputStream());
					printStream.println(mess);
				} catch (IOException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
	}

	public void SendP(String filename, Socket self) {
		Enumeration<String> enumeration = hashtable.keys();
		PrintStream printStream = null;
		while (enumeration.hasMoreElements()) {
			String s = (String) enumeration.nextElement();
			socket1 = (Socket) hashtable.get(s);
			if (socket1 != self) {
				try {
					printStream = new PrintStream(socket1.getOutputStream());
					FileInputStream fis = new FileInputStream(new File(s));
					byte[] b = new byte[1024];
					int len;
					while ((len = fis.read(b)) != -1) {
						printStream.write(b, 0, len);
					}
				} catch (IOException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
	}

	// 私聊 客户端名字 信息
	public void PrivateC(String name, String userMsg, Socket self) throws IOException {
		String user = userMsg.split(SockPro.SPLIT_SIGN)[0];
		String msg = userMsg.split(SockPro.SPLIT_SIGN)[1];
		Enumeration<String> enumeration = hashtable.keys();
		boolean has = false;
		
			while (enumeration.hasMoreElements()) {
				String s = (String) enumeration.nextElement();
				if (s.contains(user)) {
					Socket sock = (Socket) hashtable.get(s);
					PrintStream ps = new PrintStream(sock.getOutputStream());
					ps.println(name + "对你发送消息:" + msg);
					has = true;
					break;
				}
			}
			if (!has) {
				PrintStream ps = new PrintStream(self.getOutputStream());
				ps.println(user + "未连接 ，请确认后输入");
			} 
	}

	// 去sock
	private String DSock(String line) {
		return line.substring(SockPro.PROTOCOL_LEN, line.length() - SockPro.PROTOCOL_LEN);
	}

	class Service implements Runnable {
		Socket socket = null;
		String name;

		public Service(Socket socket) {
			// TODO Auto-generated constructor stub
			this.socket = socket;
			try {
				BufferedReader b1 = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				name = b1.readLine();
				hashtable.put(name, socket);
			} catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

		public Service() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				boradCast(name + "进入聊天室\n", socket);
				tfile1.setText(Integer.toString(hashtable.size()));// 更新在线人数
				while (true) {
					String line = "";
					if ((line = br.readLine()) != null) {
						System.out.println("l:" + line + "\n");
						// 私聊
						if (line.startsWith(SockPro.PRIVATE_SEND) && line.endsWith(SockPro.PRIVATE_SEND)) {
							String userMsg = DSock(line);
							System.out.println("s:" + userMsg + "\n");
							PrivateC(name, userMsg, socket);
							// 公聊
						} else if (line.startsWith(SockPro.SEND_MESS) && line.endsWith(SockPro.SEND_MESS)) {
							String msg = DSock(line);
							System.out.println("p" + msg + "\n");
							boradCast(name + "说:" + msg, socket);
						} else { // 上传图片
							fos = new FileOutputStream(new File("get.jpg"));// 接受图片 存到get.jpg里
							byte[] b = new byte[1024];
							int len;
							InputStream is = socket.getInputStream();
							while ((len = is.read(b)) != -1) {
								fos.write(b, 0, len);
							}
							// SendP("get.jpg",socket);

						}

					}
					// quit退出
					if ("quit".equals(DSock(line))) {
						hashtable.remove(name);
						tfile1.setText(Integer.toString(hashtable.size()));
						break;
					}
				}
				br.close();
				socket.close();
				tshow.append(name + "退出聊天室\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
