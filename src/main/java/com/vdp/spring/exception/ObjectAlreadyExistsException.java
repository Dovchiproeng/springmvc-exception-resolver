package com.vdp.spring.exception;


@SuppressWarnings("serial")
public class ObjectAlreadyExistsException extends RuntimeException {

    public ObjectAlreadyExistsException() {
        super();
    }

	public ObjectAlreadyExistsException(String msg) {
		super(msg);
	}
}
