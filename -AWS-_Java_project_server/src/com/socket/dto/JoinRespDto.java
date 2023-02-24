package com.socket.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JoinRespDto {
	private String welcomeMessage;
	private List<String> connectedUser;
}
