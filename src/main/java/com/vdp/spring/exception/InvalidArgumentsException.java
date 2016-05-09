package com.vdp.spring.exception;

@SuppressWarnings("serial")
public class InvalidArgumentsException extends RuntimeException {

	public InvalidArgumentsException(String msg) {
		super(msg);
	}
}
