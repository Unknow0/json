/*******************************************************************************
 * Copyright (c) 2014 Unknow.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * Contributors:
 *     Unknow - initial API and implementation
 ******************************************************************************/
package unknow.json;

import java.util.*;
import java.io.*;

/**
 * 
 */
public class JsonObject implements Iterable<String>, JsonValue
	{
	/**
	 * The map where the JsonObject's properties are kept.
	 */
	private Map<String,JsonValue> map;

	/**
	 * Construct an empty JsonObject.
	 */
	public JsonObject()
		{
		map=new TreeMap<String,JsonValue>();
		}

	/**
	 * Construct a JsonObject from a JsonTokener.
	 * 
	 * @param x
	 *            A JsonTokener object containing the source string.
	 * @throws JsonException
	 *             If there is a syntax error in the source string or a
	 *             duplicated key.
	 */
	public JsonObject(JsonTokener x) throws JsonException
		{
		this();
		char c;
		String key;

		if(x.nextClean()!='{')
			{
			throw x.syntaxError("A JsonObject text must begin with '{'");
			}
		for(;;)
			{
			c=x.nextClean();
			switch (c)
				{
				case 0:
					throw x.syntaxError("A JsonObject text must end with '}'");
				case '}':
					return;
				default:
					x.back();
					key=x.nextValue().toString();
				}

			// The key is followed by ':'. We will also tolerate '=' or '=>'.
			c=x.nextClean();
			if(c=='=')
				{
				if(x.next()!='>')
					x.back();
				}
			else if(c!=':')
				throw x.syntaxError("Expected a ':' after a key");
			putOnce(key, x.nextValue());

			// Pairs are separated by ','. We will also tolerate ';'.
			switch (x.nextClean())
				{
				case ';':
				case ',':
					if(x.nextClean()=='}')
						return;
					x.back();
					break;
				case '}':
					return;
				default:
					throw x.syntaxError("Expected a ',' or '}'");
				}
			}
		}

	/**
	 * Construct a JsonObject from a source Json text string. This is the most
	 * commonly used JsonObject constructor.
	 * 
	 * @param source
	 *            A string beginning with <code>{</code>&nbsp;<small>(left
	 *            brace)</small> and ending with <code>}</code>
	 *            &nbsp;<small>(right brace)</small>.
	 * @exception JsonException
	 *                If there is a syntax error in the source string or a
	 *                duplicated key.
	 */
	public JsonObject(String source) throws JsonException
		{
		this(new JsonTokener(source));
		}

	/**
	 * Append values to the array under a key. If the key does not exist in the
	 * JsonObject, then the key is put in the JsonObject with its value being a
	 * JsonArray containing the value parameter. If the key was already
	 * associated with a JsonArray, then the value parameter is appended to it.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            An object to be accumulated under the key.
	 * @return this.
	 * @throws JsonException
	 *             If the key is null or if the current value associated with
	 *             the key is not a JsonArray.
	 */
	public JsonObject append(String key, Object value) throws JsonException
		{
		testValidity(value);
		Object object=opt(key);
		if(object==null)
			put(key, new JsonArray().put(value));
		else if(object instanceof JsonArray)
			put(key, ((JsonArray)object).put(value));
		else
			throw new JsonException("JsonObject["+key+"] is not a JsonArray.");
		return this;
		}

	/**
	 * Get the value object associated with a key.
	 * 
	 * @param key
	 *            A key string.
	 * @return The object associated with the key.
	 * @throws JsonException
	 *             if the key is not found.
	 */
	public JsonValue get(String key) throws JsonException
		{
		if(key==null)
			throw new JsonException("Null key.");
		JsonValue object=opt(key);
		if(object==null)
			throw new JsonException("JsonObject["+JsonUtils.quote(key)+"] not found.");
		return object;
		}

	/**
	 * Get the boolean value associated with a key.
	 * 
	 * @param key
	 *            A key string.
	 * @return The truth.
	 * @throws JsonException
	 *             if the value is not a Boolean or the String "true" or
	 *             "false".
	 */
	public boolean getBoolean(String key) throws JsonException
		{
		JsonValue object=get(key);
		if(object instanceof JsonValue.Native)
			{
			JsonValue.Native o=(JsonValue.Native)object;
			if(o.value() instanceof Boolean)
				return (Boolean)o.value();
			}
		throw new JsonException("JsonObject["+JsonUtils.quote(key)+"] is not a Boolean.");
		}

	/**
	 * Get the double value associated with a key.
	 * 
	 * @param key
	 *            A key string.
	 * @return The numeric value.
	 * @throws JsonException
	 *             if the key is not found or if the value is not a Number
	 *             object and cannot be converted to a number.
	 */
	public double getDouble(String key) throws JsonException
		{
		JsonValue object=get(key);
		if(object instanceof JsonValue.Native)
			{
			JsonValue.Native o=(JsonValue.Native)object;
			if(o.value() instanceof Number)
				return ((Number)o.value()).doubleValue();
			}
		throw new JsonException("JsonObject["+JsonUtils.quote(key)+"] is not a number.");
		}

	/**
	 * Get the int value associated with a key.
	 * 
	 * @param key
	 *            A key string.
	 * @return The integer value.
	 * @throws JsonException
	 *             if the key is not found or if the value cannot be converted
	 *             to an integer.
	 */
	public int getInt(String key) throws JsonException
		{
		JsonValue object=get(key);
		if(object instanceof JsonValue.Native)
			{
			JsonValue.Native o=(JsonValue.Native)object;
			if(o.value() instanceof Number)
				return ((Number)o.value()).intValue();
			}
		throw new JsonException("JsonObject["+JsonUtils.quote(key)+"] is not an int.");
		}

	/**
	 * Get the JsonArray value associated with a key.
	 * 
	 * @param key
	 *            A key string.
	 * @return A JsonArray which is the value.
	 * @throws JsonException
	 *             if the key is not found or if the value is not a JsonArray.
	 */
	public JsonArray getJsonArray(String key) throws JsonException
		{
		JsonValue object=get(key);
		if(object instanceof JsonArray)
			{
			return (JsonArray)object;
			}
		throw new JsonException("JsonObject["+JsonUtils.quote(key)+"] is not a JsonArray.");
		}

	/**
	 * Get the JsonObject value associated with a key.
	 * 
	 * @param key
	 *            A key string.
	 * @return A JsonObject which is the value.
	 * @throws JsonException
	 *             if the key is not found or if the value is not a JsonObject.
	 */
	public JsonObject getJsonObject(String key) throws JsonException
		{
		JsonValue object=get(key);
		if(object instanceof JsonObject)
			{
			return (JsonObject)object;
			}
		throw new JsonException("JsonObject["+JsonUtils.quote(key)+"] is not a JsonObject.");
		}

	/**
	 * Get the long value associated with a key.
	 * 
	 * @param key
	 *            A key string.
	 * @return The long value.
	 * @throws JsonException
	 *             if the key is not found or if the value cannot be converted
	 *             to a long.
	 */
	public long getLong(String key) throws JsonException
		{
		JsonValue object=get(key);
		if(object instanceof JsonValue.Native)
			{
			JsonValue.Native o=(JsonValue.Native)object;
			if(o.value() instanceof Number)
				return ((Number)o.value()).longValue();
			}
		throw new JsonException("JsonObject["+JsonUtils.quote(key)+"] is not a long.");
		}

	/**
	 * Get the string associated with a key.
	 * 
	 * @param key
	 *            A key string.
	 * @return A string which is the value.
	 * @throws JsonException
	 *             if there is no string value for the key.
	 */
	public String getString(String key) throws JsonException
		{
		JsonValue object=get(key);
		if(object instanceof JsonValue.JsonString)
			{
			return ((JsonValue.JsonString)object).value();
			}
		throw new JsonException("JsonObject["+JsonUtils.quote(key)+"] not a string.");
		}

	/**
	 * Determine if the JsonObject contains a specific key.
	 * 
	 * @param key
	 *            A key string.
	 * @return true if the key exists in the JsonObject.
	 */
	public boolean has(String key)
		{
		return map.containsKey(key);
		}

	/**
	 * Determine if the value associated with the key is null or if there is no
	 * value.
	 * 
	 * @param key
	 *            A key string.
	 * @return true if there is no value associated with the key or if the value
	 *         is the JsonObject.NULL object.
	 */
	public boolean isNull(String key)
		{
		return JsonValue.NULL.equals(opt(key));
		}

	/**
	 * Get an enumeration of the keys of the JsonObject.
	 * 
	 * @return An iterator of the keys.
	 */
	public Iterator<String> iterator()
		{
		return map.keySet().iterator();
		}

	/**
	 * Get the number of keys stored in the JsonObject.
	 * 
	 * @return The number of keys in the JsonObject.
	 */
	public int length()
		{
		return map.size();
		}

	/**
	 * Produce a string from a Number.
	 * 
	 * @param number
	 *            A Number
	 * @return A String.
	 * @throws IllegalArgumentException
	 *             If n is a non-finite number.
	 */
	public static String numberToString(Number number)
		{
		if(number==null)
			throw new NullPointerException("Null pointer");
		testValidity(number);

		// Shave off trailing zeros and decimal point, if possible.
		String string=number.toString();
		if(string.indexOf('.')>0&&string.indexOf('e')<0&&string.indexOf('E')<0)
			{
			while (string.endsWith("0"))
				string=string.substring(0, string.length()-1);
			if(string.endsWith("."))
				string=string.substring(0, string.length()-1);
			}
		return string;
		}

	/**
	 * Get an optional value associated with a key.
	 * 
	 * @param key
	 *            A key string.
	 * @return An object which is the value, or null if there is no value.
	 */
	public JsonValue opt(String key)
		{
		return key==null?null:map.get(key);
		}

	/**
	 * Get an optional boolean associated with a key. It returns null if there
	 * is no such key, or if the value is not Boolean.TRUE or the String "true".
	 * 
	 * @param key
	 *            A key string.
	 * @return The truth.
	 */
	public Boolean optBoolean(String key)
		{
		return optBoolean(key, null);
		}

	/**
	 * Get an optional boolean associated with a key. It returns the
	 * defaultValue if there is no such key, or if it is not a Boolean or the
	 * String "true" or "false" (case insensitive).
	 * 
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return The truth.
	 */
	public Boolean optBoolean(String key, Boolean defaultValue)
		{
		JsonValue object=opt(key);
		if(object instanceof JsonValue.Native)
			{
			JsonValue.Native o=(JsonValue.Native)object;
			if(o.value() instanceof Boolean)
				return (Boolean)o.value();
			}
		return defaultValue;
		}

	/**
	 * Get an optional double associated with a key, or NaN if there is no such
	 * key or if its value is not a number. If the value is a string, an attempt
	 * will be made to evaluate it as a number.
	 * 
	 * @param key
	 *            A string which is the key.
	 * @return An object which is the value.
	 */
	public Double optDouble(String key)
		{
		return optDouble(key, null);
		}

	/**
	 * Get an optional double associated with a key, or the defaultValue if
	 * there is no such key or if its value is not a number. If the value is a
	 * string, an attempt will be made to evaluate it as a number.
	 * 
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return An object which is the value.
	 */
	public Double optDouble(String key, Double defaultValue)
		{
		JsonValue object=opt(key);
		if(object instanceof JsonValue.Native)
			{
			JsonValue.Native o=(JsonValue.Native)object;
			if(o.value() instanceof Number)
				return ((Number)o.value()).doubleValue();
			}
		return defaultValue;
		}

	/**
	 * Get an optional int value associated with a key, or zero if there is no
	 * such key or if the value is not a number. If the value is a string, an
	 * attempt will be made to evaluate it as a number.
	 * 
	 * @param key
	 *            A key string.
	 * @return An object which is the value.
	 */
	public Integer optInt(String key)
		{
		return optInt(key, null);
		}

	/**
	 * Get an optional int value associated with a key, or the default if there
	 * is no such key or if the value is not a number. If the value is a string,
	 * an attempt will be made to evaluate it as a number.
	 * 
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return An object which is the value.
	 */
	public Integer optInt(String key, Integer defaultValue)
		{
		JsonValue object=opt(key);
		if(object instanceof JsonValue.Native)
			{
			JsonValue.Native o=(JsonValue.Native)object;
			if(o.value() instanceof Number)
				return ((Number)o.value()).intValue();
			}
		return defaultValue;
		}

	/**
	 * Get an optional JsonArray associated with a key. It returns null if there
	 * is no such key, or if its value is not a JsonArray.
	 * 
	 * @param key
	 *            A key string.
	 * @return A JsonArray which is the value.
	 */
	public JsonArray optJsonArray(String key)
		{
		JsonValue o=opt(key);
		return o instanceof JsonArray?(JsonArray)o:null;
		}

	/**
	 * Get an optional JsonObject associated with a key. It returns null if
	 * there is no such key, or if its value is not a JsonObject.
	 * 
	 * @param key
	 *            A key string.
	 * @return A JsonObject which is the value.
	 */
	public JsonObject optJsonObject(String key)
		{
		JsonValue object=opt(key);
		return object instanceof JsonObject?(JsonObject)object:null;
		}

	/**
	 * Get an optional long value associated with a key, or zero if there is no
	 * such key or if the value is not a number. If the value is a string, an
	 * attempt will be made to evaluate it as a number.
	 * 
	 * @param key
	 *            A key string.
	 * @return An object which is the value.
	 */
	public Long optLong(String key)
		{
		return optLong(key, null);
		}

	/**
	 * Get an optional long value associated with a key, or the default if there
	 * is no such key or if the value is not a number. If the value is a string,
	 * an attempt will be made to evaluate it as a number.
	 * 
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return An object which is the value.
	 */
	public Long optLong(String key, Long defaultValue)
		{
		JsonValue object=opt(key);
		if(object instanceof JsonValue.Native)
			{
			JsonValue.Native o=(JsonValue.Native)object;
			if(o.value() instanceof Number)
				return ((Number)o.value()).longValue();
			}
		return defaultValue;
		}

	/**
	 * Get an optional string associated with a key. It returns an empty string
	 * if there is no such key. If the value is not a string and is not null,
	 * then it is converted to a string.
	 * 
	 * @param key
	 *            A key string.
	 * @return A string which is the value.
	 */
	public String optString(String key)
		{
		return optString(key, null);
		}

	/**
	 * Get an optional string associated with a key. It returns the defaultValue
	 * if there is no such key.
	 * 
	 * @param key
	 *            A key string.
	 * @param defaultValue
	 *            The default.
	 * @return A string which is the value.
	 */
	public String optString(String key, String defaultValue)
		{
		JsonValue object=opt(key);
		return JsonValue.NULL.equals(object)?defaultValue:object.toString();
		}

	/**
	 * Put a key/boolean pair in the JsonObject.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            A boolean which is the value.
	 * @return this.
	 * @throws JsonException
	 *             If the key is null.
	 */
	public JsonObject put(String key, boolean value)
		{
		put(key, value?Boolean.TRUE:Boolean.FALSE);
		return this;
		}

	/**
	 * Put a key/value pair in the JsonObject, where the value will be a
	 * JsonArray which is produced from a Collection.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            A Collection value.
	 * @return this.
	 * @throws JsonException
	 */
	public JsonObject put(String key, Collection<?> value)
		{
		put(key, new JsonArray(value));
		return this;
		}

	/**
	 * Put a key/double pair in the JsonObject.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            A double which is the value.
	 * @return this.
	 * @throws JsonException
	 *             If the key is null or if the number is invalid.
	 */
	public JsonObject put(String key, double value)
		{
		put(key, new Double(value));
		return this;
		}

	/**
	 * Put a key/int pair in the JsonObject.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            An int which is the value.
	 * @return this.
	 * @throws JsonException
	 *             If the key is null.
	 */
	public JsonObject put(String key, int value)
		{
		put(key, new Integer(value));
		return this;
		}

	/**
	 * Put a key/long pair in the JsonObject.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            A long which is the value.
	 * @return this.
	 * @throws JsonException
	 *             If the key is null.
	 */
	public JsonObject put(String key, long value)
		{
		put(key, new Long(value));
		return this;
		}

	/**
	 * Put a key/value pair in the JsonObject. If the value is null, then the
	 * key will be removed from the JsonObject if it is present.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            An object which is the value. It should be of one of these
	 *            types: Boolean, Double, Integer, JsonArray, JsonObject, Long,
	 *            String, or the JsonObject.NULL object.
	 * @return this.
	 * @throws IllegalArgumentException
	 *             If the value is non-finite number or if the key is null.
	 */
	public JsonObject put(String key, Object value)
		{
		if(key==null)
			{
			throw new IllegalArgumentException("Null key.");
			}
		if(value!=null)
			{
			testValidity(value);
			this.map.put(key, JsonUtils.wrap(value));
			}
		else
			{
			remove(key);
			}
		return this;
		}

	/**
	 * Put a key/value pair in the JsonObject, but only if the key and the value
	 * are both non-null, and only if there is not already a member with that
	 * name.
	 * 
	 * @param key
	 * @param value
	 * @return his.
	 * @throws JsonException
	 *             if the key is a duplicate
	 */
	public JsonObject putOnce(String key, Object value) throws JsonException
		{
		if(key!=null&&value!=null)
			{
			if(opt(key)!=null)
				{
				throw new JsonException("Duplicate key \""+key+"\"");
				}
			put(key, value);
			}
		return this;
		}

	/**
	 * Put a key/value pair in the JsonObject, but only if the key and the value
	 * are both non-null.
	 * 
	 * @param key
	 *            A key string.
	 * @param value
	 *            An object which is the value. It should be of one of these
	 *            types: Boolean, Double, Integer, JsonArray, JsonObject, Long,
	 *            String, or the JsonObject.NULL object.
	 * @return this.
	 * @throws JsonException
	 *             If the value is a non-finite number.
	 */
	public JsonObject putOpt(String key, Object value) throws JsonException
		{
		if(key!=null&&value!=null)
			{
			put(key, value);
			}
		return this;
		}

	/**
	 * Remove a name and its value, if present.
	 * 
	 * @param key
	 *            The name to be removed.
	 * @return The value that was associated with the name, or null if there was
	 *         no value.
	 */
	public Object remove(String key)
		{
		return map.remove(key);
		}

	/**
	 * Throw an exception if the object is a NaN or infinite number.
	 * 
	 * @param o
	 *            The object to test.
	 * @throws IllegalArgumentException
	 *             If o is a non-finite number.
	 */
	public static void testValidity(Object o)
		{
		if(o!=null)
			{
			if(o instanceof Double)
				{
				if(((Double)o).isInfinite()||((Double)o).isNaN())
					{
					throw new IllegalArgumentException("Json does not allow non-finite numbers.");
					}
				}
			else if(o instanceof Float)
				{
				if(((Float)o).isInfinite()||((Float)o).isNaN())
					{
					throw new IllegalArgumentException("Json does not allow non-finite numbers.");
					}
				}
			}
		}

	/**
	 * Make a Json text of this JsonObject. For compactness, no whitespace is
	 * added. If this would not result in a syntactically correct Json text,
	 * then null will be returned instead.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @return a printable, displayable, portable, transmittable representation
	 *         of the object, beginning with <code>{</code>&nbsp;<small>(left
	 *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
	 *         brace)</small>.
	 */
	public String toString()
		{
		StringBuilder sb=new StringBuilder();
		toString(sb);
		return sb.toString();
		}

	/**
	 * Make a prettyprinted Json text of this JsonObject.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @param indentFactor
	 *            The number of spaces to add to each level of indentation.
	 * @return a printable, displayable, portable, transmittable representation
	 *         of the object, beginning with <code>{</code>&nbsp;<small>(left
	 *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
	 *         brace)</small>.
	 * @throws JsonException
	 *             If the object contains an invalid number.
	 */
	public String toString(int indentFactor) throws JsonException
		{
		return toString(indentFactor, 0);
		}

	/**
	 * Make a prettyprinted Json text of this JsonObject.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @param indentFactor
	 *            The number of spaces to add to each level of indentation.
	 * @param indent
	 *            The indentation of the top level.
	 * @return a printable, displayable, transmittable representation of the
	 *         object, beginning with <code>{</code>&nbsp;<small>(left
	 *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
	 *         brace)</small>.
	 * @throws JsonException
	 *             If the object contains an invalid number.
	 */
	public String toString(int indentFactor, int indent)
		{
		StringBuilder sb=new StringBuilder();
		toString(sb, indentFactor, indent);
		return sb.toString();
		}

	public void toString(StringBuilder sb)
		{
		sb.append("{");
		for(String o:map.keySet())
			{
			if(sb.length()>1)
				sb.append(',');
			sb.append(JsonUtils.quote(o));
			sb.append(':');
			map.get(o).toString(sb);
			}
		sb.append('}');
		}

	public void toString(StringBuilder sb, int indentFactor, int indent)
		{
		int i;
		int length=this.length();
		if(length==0)
			{
			sb.append("{}");
			return;
			}
		Iterator<String> keys=iterator();
		int newindent=indent+indentFactor;
		String object;
		sb.append("{");
		if(length==1)
			{
			object=keys.next();
			sb.append(JsonUtils.quote(object.toString()));
			sb.append(": ");
			map.get(object).toString(sb, indentFactor, indent);
			}
		else
			{
			while (keys.hasNext())
				{
				object=keys.next();
				if(sb.length()>1)
					{
					sb.append(",\n");
					}
				else
					{
					sb.append('\n');
					}
				for(i=0; i<newindent; i+=1)
					{
					sb.append(' ');
					}
				sb.append(JsonUtils.quote(object.toString()));
				sb.append(": ");
				map.get(object).toString(sb, indentFactor, newindent);
				}
			if(sb.length()>1)
				{
				sb.append('\n');
				for(i=0; i<indent; i+=1)
					{
					sb.append(' ');
					}
				}
			}
		sb.append('}');
		}

	/**
	 * Write the contents of the JsonObject as Json text to a writer. For
	 * compactness, no whitespace is added.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @return The writer.
	 */
	public void write(Writer writer) throws IOException
		{
		boolean commanate=false;
		writer.write('{');

		for(String key:map.keySet())
			{
			if(commanate)
				writer.write(',');
			writer.write(JsonUtils.quote(key));
			writer.write(':');
			JsonValue value=this.map.get(key);
			value.write(writer);
			commanate=true;
			}
		writer.write('}');
		}

	/**
	 * Write the contents of the JsonObject as Json text to a writer.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @return The writer.
	 * @throws IOException 
	 */
	public void write(Writer writer, int indent) throws IOException
		{
		write(writer, indent, 0);
		}

	public void write(Writer writer, int indentFactor, int indent) throws IOException
		{
		boolean commanate=false;
		writer.write("{\n");
		indent+=indentFactor;

		for(String key:map.keySet())
			{
			if(commanate)
				writer.write(",\n");
			for(int i=0; i<indent; i++)
				writer.write(' ');
			writer.write(JsonUtils.quote(key));
			writer.write(": ");
			JsonValue value=this.map.get(key);
			value.write(writer, indentFactor, indent);
			commanate=true;
			}
		indent-=indentFactor;
		writer.write('\n');
		for(int i=0; i<indent; i++)
			writer.write(' ');
		writer.write('}');
		}

	public void clear()
		{
		map.clear();
		}
	}
