package com.concur.babel.test.model;

import com.concur.babel.model.BabelEnum;

public enum TweetType implements BabelEnum {

	TWEET(0),
	RETWEET(1);
	
	private int value;
	
	private TweetType(int value) {
		this.value = value;
	}
	
	public int getValue() { return this.value; }
	
}
