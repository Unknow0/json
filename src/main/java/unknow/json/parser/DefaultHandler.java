package unknow.json.parser;

import java.math.*;
import java.util.*;

import unknow.json.*;

public class DefaultHandler implements JsonHandler<JsonValue>
	{
	private Deque<JsonValue> queue=new LinkedList<JsonValue>();
	private JsonValue root=null;
	private String key;

	public void init()
		{
		queue.clear();
		key=null;
		}

	private void append(JsonValue value) throws JsonException
		{
		if(!queue.isEmpty())
			{
			JsonValue head=queue.peekFirst();
			if(head instanceof JsonObject&&key!=null)
				{
				((JsonObject)head).putOnce(key, value);
				key=null;
				}
			else if(head instanceof JsonArray)
				((JsonArray)head).put(value);
			if(value instanceof JsonObject||value instanceof JsonArray)
				queue.offerFirst(value);
			}
		else
			{
			queue.offerFirst(value);
			root=value;
			}
		}

	public void newString(String str) throws JsonException
		{
		append(JsonUtils.wrap(str));
		}

	public void newNumber(BigDecimal nbr) throws JsonException
		{
		append(JsonUtils.wrap(nbr));
		}

	public void newBoolean(boolean b) throws JsonException
		{
		append(JsonUtils.wrap(b));
		}

	public void newNull() throws JsonException
		{
		append(JsonValue.NULL);
		}

	public void startArray() throws JsonException
		{
		append(new JsonArray());
		}

	public void endArray() throws JsonException
		{
		queue.pollFirst();
		}

	public void startObject() throws JsonException
		{
		append(new JsonObject());
		}

	public void newKey(String key) throws JsonException
		{
		this.key=key;
		}

	public void endObject() throws JsonException
		{
		queue.pollFirst();
		}

	public JsonValue getValue()
		{
		return root;
		}
	}
