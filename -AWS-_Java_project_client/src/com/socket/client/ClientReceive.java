package com.socket.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

import javax.swing.JOptionPane;

import com.google.gson.Gson;
import com.socket.dto.MessageRespDto;
import com.socket.dto.ResponseDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClientReceive extends Thread {
	
	private final Socket socket;
	private InputStream inputStream;
	private Gson gson;

	@Override
	public void run() {
		try {
			inputStream = socket.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			gson = new Gson();

			while (true) {
				String request = in.readLine();
				ResponseDto responseDto = gson.fromJson(request, ResponseDto.class);
				switch (responseDto.getResource()) {
					case "joinSuccess":
						Chattingclient2.getInstance().getMainCard().show(Chattingclient2.getInstance().getMainPanel(), "chattingListPanel");
						break;	
					case "reflashRoom":
						List<String> roomNameList = gson.fromJson(responseDto.getBody(), List.class);
						Chattingclient2.getInstance().getChattingUserListModel().clear();
						Chattingclient2.getInstance().getChattingUserListModel().addAll(roomNameList);
						Chattingclient2.getInstance().getChattingUserList().setSelectedIndex(0);
						break;
					case "usernameError" :
						String errorMessage = responseDto.getBody();
						JOptionPane.showMessageDialog(null, errorMessage, "카카오톡 알림", JOptionPane.ERROR_MESSAGE);
						break;
						
					case "createSuccess" :
						String roomName = responseDto.getBody();
						Chattingclient2.getInstance().getChattingRoomArea().setText(roomName);
						Chattingclient2.getInstance().getMainCard().show(Chattingclient2.getInstance().getMainPanel(), "chattingPanel");
						Chattingclient2.getInstance().getChattingArea().append(Chattingclient2.getInstance().getUsername()+"님이 방을 생성하셨습니다.\n");
						break;
						
					case "roomJoinSuccess" :
						
						Chattingclient2.getInstance().getMainCard().show(Chattingclient2.getInstance().getMainPanel(), "chattingPanel");
						Chattingclient2.getInstance().getChattingRoomArea().setText(responseDto.getBody());
						Chattingclient2.getInstance().getChattingArea().append(Chattingclient2.getInstance().getUsername()+"님이 입장하셨습니다.");
						break;		
						
					
//					 case "sendMessage":
//		                  MessageRespDto messagerespDto = gson.fromJson(responseDto.getBody(), MessageRespDto.class);
//		                  Chattingclient2.getInstance().getChattingArea().append(messagerespDto.getMessageValue()+"\n");
//		                  break;

				}
			}
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

}