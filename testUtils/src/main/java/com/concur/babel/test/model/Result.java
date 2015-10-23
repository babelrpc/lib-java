package com.concur.babel.test.model;

import com.concur.babel.model.BabelEnum;

public enum Result implements BabelEnum {

	SUCCESS(1),
	FAILURE(0);
	
	private int value;
	
	private Result(int value) {
		this.value = value;
	}
	
	public int getValue() { return this.value; }
	
}
