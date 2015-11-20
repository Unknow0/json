package unknow.json.parser;

import java.math.*;

import unknow.json.*;

public interface JsonHandler<T>
	{
	public void init();
	
	public void newString(String str) throws JsonException;

	public void newNumber(BigDecimal nbr) throws JsonException;

	public void newBoolean(boolean b) throws JsonException;

	public void newNull() throws JsonException;

	public void startArray() throws JsonException;

	public void endArray() throws JsonException;

	public void startObject() throws JsonException;

	public void newKey(String key) throws JsonException;

	public void endObject() throws JsonException;
	
	public T getValue();
	}
