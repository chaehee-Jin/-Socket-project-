package com.socket.entity;

import java.util.ArrayList;
import java.util.List;

import com.socket.server.ConnectedSocket;

import lombok.Data;



@Data
public class Room {
	private String roomName;
	private String roomKing;
	private List<ConnectedSocket> users;
	
	public Room(String roomName, String roomKing) {
		this.roomName = roomName;
		this.roomKing = roomKing;
		this.users = new ArrayList<>();
	}
}
