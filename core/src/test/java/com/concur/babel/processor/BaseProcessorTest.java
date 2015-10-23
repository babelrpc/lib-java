package com.concur.babel.processor;

import com.concur.babel.test.model.Result;
import com.concur.babel.test.model.Tweet;
import com.concur.babel.test.model.TweetPostResult;
import com.concur.babel.test.model.TweetType;
import com.concur.babel.test.service.TweetService;
import com.concur.babel.transport.server.BabelServerTransport;
import com.concur.babel.transport.server.ServerTransport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Date;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class BaseProcessorTest {

	@Test
	public void testProcessThrowsAnExceptionIfNoMethodNameCanBeMapped() throws Throwable {
		
		TweetService.Iface mockTweetService = createMock(TweetService.Iface.class);
		ServerTransport mockTransport = createMock(ServerTransport.class);
		expect(mockTransport.getMethodName()).andReturn("badMethodName");
		replay(mockTransport, mockTweetService);		
		
		TweetService.Invoker invoker = new TweetService.Invoker(mockTweetService);
		
		try {
			invoker.invoke(mockTransport);
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().startsWith("Unable to find babel processor"));
		}
		
		verify(mockTransport, mockTweetService);		
		
	}
	
	@Test
	public void testProcessCallsUnderlyingServiceMethod() throws Throwable {

		Tweet tweet = new Tweet();
		tweet.setTweetType(TweetType.TWEET);
		tweet.setText("Posting Something Cool");
		
		TweetPostResult result = new TweetPostResult();
		result.setResult(Result.SUCCESS);
		result.setPostDate(new Date());		
		
		TweetService.postTweet tweetPost = new TweetService.postTweet(1234, tweet, true);
		
		TweetService.Iface mockTweetService = createMock(TweetService.Iface.class);
		ServerTransport mockTransport = createMock(ServerTransport.class);
		expect(mockTransport.getMethodName()).andReturn("postTweet");
		expect(mockTransport.getMethodName()).andReturn("postTweet");
		expect(mockTransport.read(TweetService.postTweet.class)).andReturn(tweetPost);
		expect(mockTweetService.postTweet(1234, tweet, true)).andReturn(result);
		mockTransport.write(BabelServerTransport.Code.SUCCESS, result);
		replay(mockTransport, mockTweetService);
		
		
		TweetService.Invoker invoker = new TweetService.Invoker(mockTweetService);
		
		invoker.invoke(mockTransport);
		
		verify(mockTransport, mockTweetService);
		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testServiceExceptionBubbleBackAsExpected() throws Throwable {
		
		Tweet tweet = new Tweet();
		tweet.setTweetType(TweetType.TWEET);
		tweet.setText("Posting Something Cool");
		
		TweetPostResult result = new TweetPostResult();
		result.setResult(Result.SUCCESS);
		result.setPostDate(new Date());		
		
		TweetService.Iface mockTweetService = createMock(TweetService.Iface.class);
		ServerTransport mockTransport = createMock(ServerTransport.class);
		expect(mockTransport.getMethodName()).andReturn("postTweet");
		expect(mockTransport.getMethodName()).andReturn("postTweet");
		expect(mockTransport.read(TweetService.postTweet.class))
			.andThrow(new IllegalArgumentException("testing"));
		expect(mockTweetService.postTweet(1234, tweet, true)).andReturn(result);
		mockTransport.write(BabelServerTransport.Code.SUCCESS, result);
		replay(mockTransport, mockTweetService);
		
		
		TweetService.Invoker invoker = new TweetService.Invoker(mockTweetService);
		
		invoker.invoke(mockTransport);
		
		verify(mockTransport, mockTweetService);		
		
	}
	
	@Test
	public void testVoidServiceMethodsAreHandledProperly() throws Throwable {
				
		TweetService.deleteTweet deleteTweet = new TweetService.deleteTweet(1234, 4444);
		
		TweetService.Iface mockTweetService = createMock(TweetService.Iface.class);
		ServerTransport mockTransport = createMock(ServerTransport.class);
		expect(mockTransport.getMethodName()).andReturn("deleteTweet");
		expect(mockTransport.getMethodName()).andReturn("deleteTweet");
		expect(mockTransport.read(TweetService.deleteTweet.class)).andReturn(deleteTweet);
		mockTweetService.deleteTweet(1234, 4444);
		mockTransport.write(BabelServerTransport.Code.SUCCESS, null);
		replay(mockTransport, mockTweetService);
		
		
		TweetService.Invoker invoker = new TweetService.Invoker(mockTweetService);
		
		invoker.invoke(mockTransport);
		
		verify(mockTransport, mockTweetService);
		
	}
	
	@Test
	public void testNoArgServiceMethodsAreHandledPropertly() throws Throwable {
			
		TweetService.getAllTweets getAllTweets = new TweetService.getAllTweets();
		
		TweetService.Iface mockTweetService = createMock(TweetService.Iface.class);
		ServerTransport mockTransport = createMock(ServerTransport.class);
		expect(mockTransport.getMethodName()).andReturn("getAllTweets");
		expect(mockTransport.getMethodName()).andReturn("getAllTweets");
		expect(mockTransport.read(TweetService.getAllTweets.class)).andReturn(getAllTweets);
		expect(mockTweetService.getAllTweets()).andReturn(new ArrayList<Tweet>());
		mockTransport.write(BabelServerTransport.Code.SUCCESS, new ArrayList<Tweet>());
		replay(mockTransport, mockTweetService);
		
		
		TweetService.Invoker invoker = new TweetService.Invoker(mockTweetService);
			
		invoker.invoke(mockTransport);
		
		verify(mockTransport, mockTweetService);		
		
	}
	
}
