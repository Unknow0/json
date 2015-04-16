package unknow.json.parser;

import java.math.*;
import java.util.*;

import unknow.json.*;

public class DefaultHandler implements JsonHandler<JsonValue>
	{
	private Queue<JsonValue> queue=new LinkedList<JsonValue>();
	private String key;

	public void init()
		{
		queue.clear();
		key=null;
		}

	public void newString(String str) throws JsonException
		{
		JsonValue value=JsonUtils.wrap(str);
		if(!queue.isEmpty())
			{
			JsonValue head=queue.peek();
			if(head instanceof JsonObject&&key!=null)
				{
				((JsonObject)head).putOnce(key, value);
				key=null;
				}
			else if(head instanceof JsonArray)
				((JsonArray)head).put(value);
			}
		else
			queue.offer(value);
		}

	public void newNumber(BigDecimal nbr) throws JsonException
		{
		JsonValue value=JsonUtils.wrap(nbr);
		if(!queue.isEmpty())
			{
			JsonValue head=queue.peek();
			if(head instanceof JsonObject&&key!=null)
				((JsonObject)head).putOnce(key, value);
			else if(head instanceof JsonArray)
				((JsonArray)head).put(value);
			}
		else
			queue.offer(value);
		}

	public void newBoolean(boolean b) throws JsonException
		{
		JsonValue value=JsonUtils.wrap(b);
		if(!queue.isEmpty())
			{
			JsonValue head=queue.peek();
			if(head instanceof JsonObject&&key!=null)
				((JsonObject)head).putOnce(key, value);
			else if(head instanceof JsonArray)
				((JsonArray)head).put(value);
			}
		else
			queue.offer(value);
		}

	public void newNull() throws JsonException
		{
		JsonValue value=JsonValue.NULL;
		if(!queue.isEmpty())
			{
			JsonValue head=queue.peek();
			if(head instanceof JsonObject&&key!=null)
				((JsonObject)head).putOnce(key, value);
			else if(head instanceof JsonArray)
				((JsonArray)head).put(value);
			}
		else
			queue.offer(value);
		}

	public void startArray() throws JsonException
		{
		queue.offer(new JsonArray());
		}

	public void endArray() throws JsonException
		{
		queue.poll();
		}

	public void startObject() throws JsonException
		{
		queue.offer(new JsonObject());
		}

	public void newKey(String key) throws JsonException
		{
		this.key=key;
		}

	public void endObject() throws JsonException
		{
		queue.poll();
		}

	public JsonValue getValue()
		{
		return queue.peek();
		}
	}
