package com.concur.babel.test.service;

import com.concur.babel.exception.BabelApplicationException;
import com.concur.babel.test.model.Result;
import com.concur.babel.test.model.Tweet;
import com.concur.babel.test.model.TweetPostResult;
import com.concur.babel.test.model.TweetType;

import java.util.*;

public class TweetServiceImpl implements TweetService.Iface {

	private Set<Integer> tweetIds = new HashSet<Integer>(Arrays.asList(1, 2, 3));
	private Map<Integer, List<Tweet>> tweetMap = new HashMap<Integer, List<Tweet>>();
	
	public TweetPostResult postTweet(int userId, Tweet tweet, boolean test) {
		
		if (!this.tweetMap.containsKey(userId)) {
			this.tweetMap.put(userId, new ArrayList<Tweet>());
		}
		this.tweetMap.get(userId).add(tweet);
		TweetPostResult result = new TweetPostResult();
		result.setResult(Result.SUCCESS);
		result.setPostDate(new Date());
		return result;
		
	}
	
	public void deleteTweet(int userId, int tweetId) {
		
		if (userId < 0) {
			throw new RuntimeException("User is is invalid");
		}
		
		if (!this.tweetIds.contains(tweetId)) {
			throw new BabelApplicationException("4545", "Tweet id does not exist");
		}
		
	}
	
	public List<Tweet> recentTweets(int userId) {
		
		Tweet tweet = new Tweet();
		tweet.setText("Tweet Posting");
		tweet.setTweetType(TweetType.TWEET);

		return Arrays.asList(tweet);
		
	}
	
	public List<Tweet> getAllTweets() {
		
		List<Tweet> tweetList = new ArrayList<Tweet>();
		for (List<Tweet> tweets : this.tweetMap.values()) {
			tweetList.addAll(tweets);
		}
		return tweetList;
		
	}
	
}
