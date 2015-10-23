package com.concur.babel.test.service;

import com.concur.babel.BabelService;
import com.concur.babel.ResponseServiceMethod;
import com.concur.babel.ServiceMethod;
import com.concur.babel.VoidServiceMethod;
import com.concur.babel.processor.BaseInvoker;
import com.concur.babel.service.BabelServiceDefinition;
import com.concur.babel.test.model.Tweet;
import com.concur.babel.test.model.TweetPostResult;
import com.concur.babel.transport.BaseClient;
import com.concur.babel.transport.Transport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TweetService implements BabelServiceDefinition {

    public Class<Iface> getIfaceClass() {
        return Iface.class;
    }

    public Invoker createInvoker(BabelService iFaceImpl) {
        return new Invoker((Iface)iFaceImpl);
    }

	public interface Iface extends BabelService {
		TweetPostResult postTweet(int userId, Tweet tweet, boolean test);
		void deleteTweet(int userId, int tweetId);
		List<Tweet> recentTweets(int userId);
		List<Tweet> getAllTweets();
	}
	
	public static class Client extends BaseClient implements Iface {	    
		
		public Client(String url) { super(url); }
		public Client(String url, int timeoutInMillis) { super(url, timeoutInMillis); }
		public Client(Transport transport) { super(transport); }

		public TweetPostResult postTweet(int userId, Tweet tweet, boolean test) {
			
			postTweet serviceMethod = new postTweet(userId, tweet, test);
			return this.transport.invoke(serviceMethod);
			
		}
		
		public void deleteTweet(int userId, int tweetId) {
			
			deleteTweet serviceMethod = new deleteTweet(userId, tweetId);
			this.transport.invoke(serviceMethod);			
			
		}
		
		public List<Tweet> recentTweets(int userId) {
			
			recentTweets serviceMethod = new recentTweets(userId);
			return this.transport.invoke(serviceMethod);
			
		}
		
		public List<Tweet> getAllTweets() {
			
			getAllTweets serviceMethod = new getAllTweets();
			return this.transport.invoke(serviceMethod);
			
		}
		
	}
	
	public static class Invoker extends BaseInvoker<Iface> {
		
		public Invoker(Iface serviceImpl) {			
			super(serviceImpl);
		}
		
		protected Map<String, Class<? extends ServiceMethod>> initServiceMethods() {
			Map<String, Class<? extends ServiceMethod>> map = 
				new HashMap<String, Class<? extends ServiceMethod>>();
			map.put("postTweet", postTweet.class);
			map.put("deleteTweet", deleteTweet.class);
			map.put("getAllTweets", getAllTweets.class);
			return map;
		}
		
		public String getServiceName() { return "tweetservice"; }
		
		public Class<Iface> getInterface() { return Iface.class; }
		
	}
	
	public static class postTweet extends ResponseServiceMethod<TweetPostResult> {
		
		public int userId;
		public Tweet tweet;
		public boolean test;
		
		public postTweet(int userId, Tweet tweet, boolean test) {
			this.userId = userId;
			this.tweet = tweet;
			this.test = test;
		}	
		
		public String getServiceName() { return "TweetService"; }
		public String getMethodName() { return "postTweet"; }
		
		public Object[] getMethodParameters() {
			return new Object[] { this.userId, this.tweet, this.test };
		}
		
	}
	
	public static class deleteTweet extends VoidServiceMethod {
		
		public int userId;
		public int tweetId;
		
		public deleteTweet(int userId, int tweetId) {
			this.userId = userId;
			this.tweetId = tweetId;
		}
		
		public String getServiceName() { return "TweetService"; }
		public String getMethodName() { return "deleteTweet"; }
		
		public Object[] getMethodParameters() {
			return new Object[] { this.userId, this.tweetId };
		}
		
	}	
	
	public static class getAllTweets extends ResponseServiceMethod<List<Tweet>> {
		
		public String getServiceName() { return "TweetService"; }
		public String getMethodName() { return "getAllTweets"; }
		
		public Object[] getMethodParameters() {
			return new Object[] {};
		}
		
	}
	
	public static class recentTweets extends ResponseServiceMethod<List<Tweet>> {
		
		public int userId;
		
		public recentTweets(int userId) {
			this.userId = userId;
		}
		
		public String getServiceName() { return "TweetService"; }
		public String getMethodName() { return "recentTweets"; }
		
		public Object[] getMethodParameters() {
			return new Object[] { this.userId };
		}
		
	}
	
}
