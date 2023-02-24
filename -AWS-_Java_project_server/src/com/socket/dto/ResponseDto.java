package com.socket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ResponseDto {
	private String resource;
	private String status;
	private String body;
}
