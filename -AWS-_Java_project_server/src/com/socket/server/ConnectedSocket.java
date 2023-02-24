package com.socket.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import com.socket.dto.JoinReqDto;
import com.socket.dto.MessageReqDto;
import com.socket.dto.RequestDto;
import com.socket.dto.ResponseDto;
import com.socket.entity.Room;

import lombok.Data;


@Data
public class ConnectedSocket extends Thread {
	private static List<ConnectedSocket> socketList = new ArrayList<>();
	private static List<Room> roomList = new ArrayList<>();
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private Gson gson;
	
	
	private String username;
	private String roomName;
	
	public ConnectedSocket(Socket socket) {

		this.socket = socket;
		gson = new Gson();
		socketList.add(this);
	}
	
	@Override
	public void run() {
		try {
			inputStream = socket.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			
			while(true) {
				String request = in.readLine(); // requestDto(JSON)
				RequestDto requestDto = gson.fromJson(request, RequestDto.class);
				
				switch(requestDto.getResource()) {
					case "join" :
						JoinReqDto joinReqDto = gson.fromJson(requestDto.getBody(), JoinReqDto.class);
						
						List<String> connectedUsers = new ArrayList<>();
						socketList.forEach(connectedSocket -> {
							connectedUsers.add(connectedSocket.getUsername());
						}); 
						// 중복검사 
						if(joinReqDto.getUsername()== null || joinReqDto.getUsername().isEmpty() || connectedUsers.contains(joinReqDto.getUsername())) {
							ResponseDto usernameErrorResponseDto = new ResponseDto("usernameError", "no", "이미 존재하는 사용자명입니다.");
							sendToMe(usernameErrorResponseDto);
							continue;							
						}
						
						username = joinReqDto.getUsername();
						
						ResponseDto joinSuccessResponseDto = new ResponseDto("joinSuccess", "ok", "접속 성공!");
						sendToMe(joinSuccessResponseDto);
						
						reflashRoomList();
						break;
					
					case "create":
						 String roomName = requestDto.getBody();
						 System.out.println(roomName);
						 Room room = new Room(roomName, username);
						 roomList.add(room);
						 ResponseDto createSuccessResponseDto = new ResponseDto("createSuccess", "ok", room.getRoomName());
						 sendToMe(createSuccessResponseDto);
						 reflashRoomList();
						 break;
					 
					case "roomJoin":
						
						String selectRoomName = requestDto.getBody();
			
						for(Room r : roomList) {
							if (r.getRoomName().equals(selectRoomName)) {
								r.getUsers().add(this);
								break;
							}
						}
						
						
						ResponseDto roomJoinRespDto = new ResponseDto("roomJoinSuccess", "ok", selectRoomName);
						
						sendToMe(roomJoinRespDto);
						
						break;
					
//					case "sendMessage":
//						MessageReqDto messageReqDto = gson.fromJson(requestDto.getBody(), MessageReqDto.class);
											
				}
			
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	private void reflashRoomList() {
		List<String> roomNameList = new ArrayList<>();
		roomList.forEach(room -> {
			roomNameList.add(room.getRoomName());
		});
		ResponseDto responseDto = new ResponseDto("reflashRoom", "ok", gson.toJson(roomNameList));
		sendToAll(responseDto, socketList);
	}
	
	private void sendToMe(ResponseDto responseDto) {
		OutputStream outputStream;
		try {
			outputStream = socket.getOutputStream();
			PrintWriter out = new PrintWriter(outputStream, true);
			out.println(gson.toJson(responseDto));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendToAll(ResponseDto responseDto, List<ConnectedSocket> connectedSockets) {
		try {
			for (ConnectedSocket connectedSocket : connectedSockets) {
				OutputStream outputStream;
				outputStream = connectedSocket.getSocket().getOutputStream();
				PrintWriter out = new PrintWriter(outputStream, true);

				out.println(gson.toJson(responseDto));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
