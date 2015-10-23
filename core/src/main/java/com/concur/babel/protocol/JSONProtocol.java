package com.concur.babel.protocol;

import java.io.Reader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * JSONProtocol is a JSON implementation of the babel protocol interface.
 */
public class JSONProtocol implements Protocol {
	
	private Gson gson = new GsonBuilder()
		.registerTypeAdapter(Date.class,  new DateAdapter())
		.registerTypeAdapter(byte[].class, new ByteArrayAdapter())
		.registerTypeAdapter(BigDecimal.class, new BigDecimalAdapter())
		.registerTypeAdapter(Long.class, new LongAdapter())
		.create();
	
	public <T> T read(String json, Class<T> classOfT) {
		return this.gson.fromJson(json, classOfT);
	}

	public <T> T read(String json, Type typeOfT) {
		return this.gson.fromJson(json, typeOfT);
	}
	
	public <T> T read(Reader reader, Class<T> classOfT) {
		return gson.fromJson(reader, classOfT);
	}
	
	public <T> T read(Reader reader, Type typeOfT) {
		return gson.fromJson(reader, typeOfT);
	}

	public String write(Object src) {
		return this.gson.toJson(src);
	}
	
	private class DateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
		
		private final DateTimeFormatter df = ISODateTimeFormat.dateTime();
		
		public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
		throws JsonParseException 
		{
			try {
				
				return df.parseDateTime(json.getAsString()).toDate();
				
			} catch (IllegalArgumentException e) {				
				throw new RuntimeException(
					"Unable to parse date..it must be in the format of " +
					"2013-09-01T00:00:00.000-05:00 or 2013-09-01T00:00:00.000Z",
					e);				
			}
			
		}

		public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {			
			return new JsonPrimitive(df.print(src.getTime()));			
		}
		
	}
	
	private class BigDecimalAdapter implements JsonSerializer<BigDecimal> {

		public JsonElement serialize(
			BigDecimal src, 
			Type typeOfSrc, 
			JsonSerializationContext context) 
		{
			return new JsonPrimitive(src.toString());
		}
		
	}
	
	private class LongAdapter implements JsonSerializer<Long> {
		
		public JsonElement serialize(
			Long src, 
			Type typeOfSrc, 
			JsonSerializationContext context) 
		{
			return new JsonPrimitive(src.toString());
		}		
		
	}
	
	private class ByteArrayAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

		public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
		throws JsonParseException 
		{
			return Base64.decodeBase64(json.getAsString());
		}

		public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(Base64.encodeBase64String(src));
		}
		
	}	

}
