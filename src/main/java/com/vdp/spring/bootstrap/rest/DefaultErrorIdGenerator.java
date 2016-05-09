package com.vdp.spring.bootstrap.rest;

public class DefaultErrorIdGenerator implements ErrorIdGenerator {
	
	private static int id = 1;
	
	@Override
	public String generateId() {
		return "serverErrorId:" + id++;
	}

}
