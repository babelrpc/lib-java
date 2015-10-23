package com.concur.babel.test.model;

import java.util.Date;

public class TweetPostResult {

	private Result result;
	private Date postDate;
	
	public Result getResult() { return this.result; }
	public void setResult(Result result) {
		this.result = result;
	}
	
	public Date getPostDate() { return this.postDate; }
	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}
	
}
