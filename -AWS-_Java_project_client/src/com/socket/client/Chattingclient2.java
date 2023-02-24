package com.socket.client;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.google.gson.Gson;
import com.socket.dto.JoinReqDto;
import com.socket.dto.MessageReqDto;
import com.socket.dto.RequestDto;
import com.socket.dto.RoomReqDto;

import lombok.Getter;
import javax.swing.SwingConstants;

@Getter
public class Chattingclient2 extends JFrame {
	private static Chattingclient2 instance;

	// 싱글톤 만들기
	public static Chattingclient2 getInstance() {
		if (instance == null) {
			instance = new Chattingclient2();
		}
		return instance;

	}

	private Socket socket;
	private String username;
	private String roomName;
	private Gson gson;

	private DefaultListModel<String> chattingUserListModel;
	private JScrollPane chattingUserScroll;
	private JList<String> chattingUserList;
	private JPanel chattingPanel;
	private JTextArea chattingRoomArea;
	private JTextArea chattingArea;
	private CardLayout mainCard;
	private JPanel MainPanel;
	private JPanel chattingListPanel;
	private JTextField usernameInput;
	private JTextField messageInput;

	// EventQueue.invokeLater() 메소드는 새로운 익명 Runable 객체의 run()메소드 내의 코드를 스레드를
	// 실행하도록한다. 이벤트 핸들러가 제대로 작동하도록 한다
	// run()메소드 내에서는 ChattingClient.getInstance()메소드를 사용, ChattingClient클래스의 인스턴스를
	// 생성, frame 변수에 할당한다
	// frame의 setVisible()메소드를 사용하여 ChattingClient창을 화면에 표시한다.
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Chattingclient2 frame = Chattingclient2.getInstance();
					frame.setVisible(true);
//				}catch (ConnectException e) {
//					JOptionPane.showMessageDialog(null, "서버에 연결 할 수 없습니다", "접속실패", JOptionPane.ERROR_MESSAGE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// 디자인으로 만든부분
	public Chattingclient2() {
		gson = new Gson();
		chattingArea = new JTextArea(); // JTextArea 초기화
		try {
			socket = new Socket("127.0.0.1", 9090);

			JOptionPane.showMessageDialog(null, socket.getInetAddress() + "서버접속", "접속성공",
					JOptionPane.INFORMATION_MESSAGE);

			ClientReceive clientReceive = new ClientReceive(socket);
			clientReceive.start();
		} catch (ConnectException e1) {

			JOptionPane.showMessageDialog(null, "서버 접속실패", "접속실패", JOptionPane.ERROR_MESSAGE);

		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 480, 800);
		MainPanel = new JPanel();
		MainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		mainCard = new CardLayout();
		MainPanel.setLayout(mainCard);
		setContentPane(MainPanel);

		JPanel userPanel = new JPanel();
		MainPanel.add(userPanel, "userPanel");
		userPanel.setLayout(null);

		JPanel backgroundPanel = new JPanel();
		userPanel.setForeground(new Color(239, 228, 16));
		userPanel.setBackground(new Color(250, 225, 5));
		getContentPane().add(userPanel, "name_2531257643730900");
		userPanel.setLayout(null);

		// 유저 닉네임을 입력하는 부분
		usernameInput = new JTextField();
		usernameInput.setBounds(89, 365, 240, 39);
		userPanel.add(usernameInput);
		usernameInput.setColumns(10);

		// 1번 판넬 아이콘 로고버튼
		ImageIcon logo_icon = new ImageIcon(getClass().getResource("/socket_icon/logo.png"));
		JButton iconButton = new JButton(logo_icon);

		iconButton.setBounds(148, 141, 118, 109);
		userPanel.add(iconButton);
		// 마우스를 클릭하면 연결이되도록 만들었음

		// 1번 판넬 '카카오로 시작하기' 버튼
		ImageIcon start_button = new ImageIcon(getClass().getResource("/socket_icon/start_button.png"));
		JButton startButton = new JButton(start_button);
		startButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				// 유저이름과 조인요청
				username = usernameInput.getText();
				JoinReqDto joinReqDto = new JoinReqDto(username);
				String joinReqDtoJson = gson.toJson(joinReqDto);
				RequestDto requestDto = new RequestDto("join", joinReqDtoJson);
				String requestDtoJson = gson.toJson(requestDto);

				try {
					OutputStream outputStream = socket.getOutputStream();
					PrintWriter out = new PrintWriter(outputStream, true);
					out.println(requestDtoJson);

				} catch (IOException e1) {

					e1.printStackTrace();
				}

			}
		});

		startButton.setBounds(89, 432, 240, 39);
		userPanel.add(startButton);

		// 2번 판넬 (리스트부분)
		chattingListPanel = new JPanel();
		chattingListPanel.setBackground(new Color(254, 222, 1));
		MainPanel.add(chattingListPanel, "chattingListPanel");
		chattingListPanel.setLayout(null);

		ImageIcon logo3 = new ImageIcon(getClass().getResource("/socket_icon/logo3.png"));
		JButton iconbutton2 = new JButton(logo3);

		iconbutton2.setBounds(12, 10, 54, 58);
		chattingListPanel.add(iconbutton2);

		// 2번 판넬 리스트 플러스버튼
		// 플러스버튼을 누르면 채팅방이 생성이 되게끔 구현
		ImageIcon plus = new ImageIcon(getClass().getResource("/socket_icon/plus.png"));
		JButton btnNewButton = new JButton(plus);
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				roomName = JOptionPane.showInputDialog(null, "방의 제목을 입력하시오.", "방 생성", JOptionPane.INFORMATION_MESSAGE);

				RequestDto requestDto = new RequestDto("create", roomName);
				String requestDtoJson = gson.toJson(requestDto);

				try {
					OutputStream outputStream = socket.getOutputStream();
					PrintWriter out = new PrintWriter(outputStream, true);
					out.println(requestDtoJson);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		});

		btnNewButton.setBounds(12, 78, 54, 58);
		chattingListPanel.add(btnNewButton);

		// 채팅방을 보여주는 부분
		chattingUserScroll = new JScrollPane();
		chattingUserScroll.setBounds(78, 0, 376, 751);
		chattingListPanel.add(chattingUserScroll);

		chattingUserListModel = new DefaultListModel<>();
		chattingUserListModel.clear();
		chattingUserList = new JList<String>(chattingUserListModel);
		chattingUserList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// 문제의 부분...구글링과 chatGpt로 검색을 하였으나 ...
				// 더블클릭시 선택한 방으로 들어가져야하는데 왜 우린 공백을 클릭해도 들어가지는 걸까....
				// chatGpt 에게 한 질문: 다중 채팅방을 구현하여 chattingUserList 안에 있는 유저가 채팅방을 임의로 선택을 한다고 했을때
				// 공백을 클릭하면
				// 방이 들어가 지지 않는 오류를 소스코드로 보여주겠니??
				if (e.getClickCount() == 2) {
					String selectedUser = chattingUserList.getSelectedValue();
					System.out.println("test");
					if (selectedUser != null) { // 선택한 유저가 공백이 아닌경우에만 입장
						String roomName = JOptionPane.showInputDialog(null, "방이름을 입력하세요");
						if (roomName != null && !roomName.isEmpty()) {
							JoinReqDto reqDto = new JoinReqDto(username); // roomname도 같이 들고 와야하는 데 못들고 옴
							// why? : joinReqDto 안에는 username밖에 없으니까
							RequestDto requestDto = new RequestDto("roomjoin", gson.toJson(reqDto));
							String json = gson.toJson(requestDto);
							try {
								PrintWriter pr = new PrintWriter(socket.getOutputStream());
								pr.print(json);
								pr.flush();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							mainCard.show(MainPanel, "chattingPanel");
							chattingRoomArea.setText(roomName);
							

						}

						// chattingRoomArea.setText(chattingUserList.getSelectedValue());
					}
//					System.out.println(chattingRoomArea + ","+ username);
//					RoomReqDto roomreqDto = new RoomReqDto(roomName);
//					OutputStream outputStream;

					createRoom(roomName); // 메소드를 만들었음
				}
				joinRoom();
				// 방을 들어가는 것까지는 구현을 완료하였으나 nullpointException으로 계속 뜬다
			}
		});
		chattingUserScroll.setViewportView(chattingUserList);

		// 3번 채팅 판넬
		chattingPanel = new JPanel();
		chattingPanel.setBackground(new Color(253, 222, 2));
		MainPanel.add(chattingPanel, "chattingPanel");
		chattingPanel.setLayout(null);

		JButton iconbutton3 = new JButton(logo3);

		iconbutton3.setBounds(12, 10, 52, 44);
		chattingPanel.add(iconbutton3);

		// 채팅방명 입력하는 부분
		chattingRoomArea = new JTextArea();
		chattingRoomArea.setBackground(new Color(254, 222, 1));
		chattingRoomArea.setBounds(76, 10, 277, 34);
		chattingPanel.add(chattingRoomArea);

		// 방을 나가는 부분
		ImageIcon exsit = new ImageIcon(getClass().getResource("/socket_icon/exsit.png"));
		JButton existButton = new JButton(exsit);
		existButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});

		existButton.setBounds(402, 10, 52, 44);
		chattingPanel.add(existButton);

		// 메세지를 받는 부분
		JScrollPane chattingScroll = new JScrollPane();
		chattingScroll.setBounds(0, 64, 454, 619);
		chattingPanel.add(chattingScroll);

		JTextArea chattingArea = new JTextArea();
		chattingScroll.setViewportView(chattingArea);

		// 메세지를 보내는 부분
		JScrollPane messageScroll = new JScrollPane();
		messageScroll.setBounds(0, 682, 381, 69);
		chattingPanel.add(messageScroll);

		messageInput = new JTextField();
		messageScroll.setViewportView(messageInput);
		messageInput.setColumns(10);

		ImageIcon send = new ImageIcon(getClass().getResource("/socket_icon/send.png"));
		JButton sendButton = new JButton(send);
		sendButton.setText("전송");
		sendButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// 추가
				sendMessage();
			}
		});
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		sendButton.setContentAreaFilled(false);
		sendButton.setBackground(Color.WHITE);

		sendButton.setBounds(381, 682, 73, 69);
		chattingPanel.add(sendButton);

	}

	protected void createRoom(String roomName2) {
		// TODO Auto-generated method stub

	}

	private void joinRoom() {
		OutputStream outputStream;

		try {
			RequestDto requestDto = new RequestDto("joinRoom", chattingUserList.getSelectedValue());
			String requestDtoJson = gson.toJson(requestDto);

			outputStream = socket.getOutputStream();
			PrintWriter out = new PrintWriter(outputStream, true);
			out.println(requestDtoJson);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 추가
	private void sendRequest(String resource, String body) {

		OutputStream outputStream;
		try {
			outputStream = socket.getOutputStream();
			PrintWriter out = new PrintWriter(outputStream, true);

			RequestDto requestDto = new RequestDto(resource, body);

			out.println(gson.toJson(requestDto));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendMessage() {
		if (!messageInput.getText().isBlank()) {

			MessageReqDto messageReqDto = new MessageReqDto(username, messageInput.getText());

			sendRequest("sendMessage", gson.toJson(messageReqDto));

			messageInput.setText("");
			//
		}
	}

}