package com.concur.babel.test.model;

public class Tweet {

	private TweetType tweetType;
	private String text;
	
	public TweetType getTweetType() { return this.tweetType; }
	public void setTweetType(TweetType tweetType) {
		this.tweetType = tweetType;
	}
	
	public String getText() { return this.text; }
	public void setText(String text) {
		this.text = text;
	}
	
}
