package unknow.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A JsonArray is an ordered sequence of values. Its external text form is a
 * string wrapped in square brackets with commas separating the values. The
 * internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by index, and <code>put</code> methods for
 * adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>JsonArray</code>, <code>JsonObject</code>,
 * <code>Number</code>, <code>String</code>, or the
 * <code>JsonObject.NULL object</code>.
 * <p>
 * The constructor can convert a Json text into a Java object. The
 * <code>toString</code> method converts to Json text.
 * <p>
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * Json syntax rules. The constructors are more forgiving in the texts they will
 * accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there is <code>,</code>
 * &nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 * or single quote, and if they do not contain leading or trailing spaces, and
 * if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers and
 * if they are not the reserved words <code>true</code>, <code>false</code>, or
 * <code>null</code>.</li>
 * <li>Values can be separated by <code>;</code> <small>(semicolon)</small> as
 * well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0x-</code> <small>(hex)</small> prefix.</li>
 * </ul>
 * 
 * @author Json.org
 * @version 2011-05-04
 */
public class JsonArray implements JsonValue
	{

	/**
	 * The arrayList where the JsonArray's properties are kept.
	 */
	private ArrayList<JsonValue> myArrayList;

	/**
	 * Construct an empty JsonArray.
	 */
	public JsonArray()
		{
		this.myArrayList=new ArrayList<JsonValue>();
		}

	/**
	 * Construct a JsonArray from a JsonTokener.
	 * 
	 * @param x
	 *            A JsonTokener
	 * @throws JsonException
	 *             If there is a syntax error.
	 */
	public JsonArray(JsonTokener x) throws JsonException
		{
		this();
		if(x.nextClean()!='[')
			{
			throw x.syntaxError("A JsonArray text must start with '['");
			}
		if(x.nextClean()!=']')
			{
			x.back();
			for(;;)
				{
				if(x.nextClean()==',')
					{
					x.back();
					this.myArrayList.add(JsonUtils.NULL);
					}
				else
					{
					x.back();
					this.myArrayList.add(JsonUtils.wrap(x.nextValue()));
					}
				switch (x.nextClean())
					{
					case ';':
					case ',':
						if(x.nextClean()==']')
							{
							return;
							}
						x.back();
						break;
					case ']':
						return;
					default:
						throw x.syntaxError("Expected a ',' or ']'");
					}
				}
			}
		}

	/**
	 * Construct a JsonArray from a source Json text.
	 * 
	 * @param source
	 *            A string that begins with <code>[</code>&nbsp;<small>(left
	 *            bracket)</small> and ends with <code>]</code>
	 *            &nbsp;<small>(right bracket)</small>.
	 * @throws JsonException
	 *             If there is a syntax error.
	 */
	public JsonArray(String source) throws JsonException
		{
		this(new JsonTokener(source));
		}

	/**
	 * Construct a JsonArray from a Collection.
	 * 
	 * @param collection
	 *            A Collection.
	 */
	public JsonArray(Collection<?> collection)
		{
		this.myArrayList=new ArrayList<JsonValue>();
		if(collection!=null)
			{
			Iterator<?> iter=collection.iterator();
			while (iter.hasNext())
				{
				this.myArrayList.add(JsonUtils.wrap(iter.next()));
				}
			}
		}

	/**
	 * Construct a JsonArray from an array
	 * 
	 * @throws IllegalArgumentException
	 *             If not an array.
	 */
	public JsonArray(Object array)
		{
		this();
		if(array.getClass().isArray())
			{
			int length=Array.getLength(array);
			for(int i=0; i<length; i+=1)
				{
				this.put(JsonUtils.wrap(Array.get(array, i)));
				}
			}
		else
			{
			throw new IllegalArgumentException("JsonArray initial value should be a string or collection or array.");
			}
		}

	/**
	 * Get the object value associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return An object value.
	 * @throws JsonException
	 *             If there is no value for the index.
	 */
	public JsonValue get(int index) throws JsonException
		{
		JsonValue object=opt(index);
		if(object==null)
			{
			throw new JsonException("JsonArray["+index+"] not found.");
			}
		return object;
		}

	/**
	 * Get the boolean value associated with an index. The string values "true"
	 * and "false" are converted to boolean.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The truth.
	 * @throws JsonException
	 *             If there is no value for the index or if the value is not
	 *             convertible to boolean.
	 */
	public boolean getBoolean(int index) throws JsonException
		{
		JsonValue object=get(index);
		if(object instanceof JsonValue.Native)
			{
			JsonValue.Native o=(JsonValue.Native)object;
			if(o.value() instanceof Boolean)
				return (Boolean)o.value();
			}
		throw new JsonException("JsonArray["+index+"] is not a boolean.");
		}

	/**
	 * Get the double value associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws JsonException
	 *             If the key is not found or if the value cannot be converted
	 *             to a number.
	 */
	public double getDouble(int index) throws JsonException
		{
		JsonValue object=get(index);
		if(object instanceof JsonValue.Native)
			{
			JsonValue.Native o=(JsonValue.Native)object;
			if(o.value() instanceof Number)
				return ((Number)o.value()).doubleValue();
			}
			throw new JsonException("JsonArray["+index+"] is not a number.");
		}

	/**
	 * Get the int value associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws JsonException
	 *             If the key is not found or if the value is not a number.
	 */
	public int getInt(int index) throws JsonException
		{
		JsonValue object=get(index);
		if(object instanceof JsonValue.Native)
			{
			JsonValue.Native o=(JsonValue.Native)object;
			if(o.value() instanceof Number)
				return ((Number)o.value()).intValue();
			}
			throw new JsonException("JsonArray["+index+"] is not a number.");
		}

	/**
	 * Get the JsonArray associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return A JsonArray value.
	 * @throws JsonException
	 *             If there is no value for the index. or if the value is not a
	 *             JsonArray
	 */
	public JsonArray getJsonArray(int index) throws JsonException
		{
		JsonValue object=get(index);
		if(object instanceof JsonArray)
			{
			return (JsonArray)object;
			}
		throw new JsonException("JsonArray["+index+"] is not a JsonArray.");
		}

	/**
	 * Get the JsonObject associated with an index.
	 * 
	 * @param index
	 *            subscript
	 * @return A JsonObject value.
	 * @throws JsonException
	 *             If there is no value for the index or if the value is not a
	 *             JsonObject
	 */
	public JsonObject getJsonObject(int index) throws JsonException
		{
		JsonValue object=get(index);
		if(object instanceof JsonObject)
			{
			return (JsonObject)object;
			}
		throw new JsonException("JsonArray["+index+"] is not a JsonObject.");
		}

	/**
	 * Get the long value associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws JsonException
	 *             If the key is not found or if the value cannot be converted
	 *             to a number.
	 */
	public long getLong(int index) throws JsonException
		{
		JsonValue object=get(index);
		if(object instanceof JsonValue.Native)
			{
			JsonValue.Native o=(JsonValue.Native)object;
			if(o.value() instanceof Number)
				return ((Number)o.value()).longValue();
			}
			throw new JsonException("JsonArray["+index+"] is not a number.");
		}

	/**
	 * Get the string associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return A string value.
	 * @throws JsonException
	 *             If there is no string value for the index.
	 */
	public String getString(int index) throws JsonException
		{
		JsonValue object=get(index);
		if(object instanceof JsonValue.JsonString)
			{
			return ((JsonValue.JsonString)object).value();
			}
		throw new JsonException("JsonArray["+index+"] not a string.");
		}

	/**
	 * Determine if the value is null.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return true if the value at the index is null, or if there is no value.
	 */
	public boolean isNull(int index)
		{
		return JsonUtils.NULL.equals(opt(index));
		}

	/**
	 * Make a string from the contents of this JsonArray. The
	 * <code>separator</code> string is inserted between each element. Warning:
	 * This method assumes that the data structure is acyclical.
	 * 
	 * @param separator
	 *            A string that will be inserted between the elements.
	 * @return a string.
	 * @throws JsonException
	 *             If the array contains an invalid number.
	 */
	public String join(String separator) throws JsonException
		{
		int len=length();
		StringBuilder sb=new StringBuilder();

		for(int i=0; i<len; i+=1)
			{
			if(i>0)
				{
				sb.append(separator);
				}
			this.myArrayList.get(i).toString(sb);
			}
		return sb.toString();
		}

	/**
	 * Get the number of elements in the JsonArray, included nulls.
	 * 
	 * @return The length (or size).
	 */
	public int length()
		{
		return this.myArrayList.size();
		}

	/**
	 * Get the optional object value associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return An object value, or null if there is no object at that index.
	 */
	public JsonValue opt(int index)
		{
		return (index<0||index>=length())?null:this.myArrayList.get(index);
		}

	/**
	 * Get the optional boolean value associated with an index. It returns false
	 * if there is no value at that index, or if the value is not Boolean.TRUE
	 * or the String "true".
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The truth.
	 */
	public Boolean optBoolean(int index)
		{
		return optBoolean(index, null);
		}

	/**
	 * Get the optional boolean value associated with an index. It returns the
	 * defaultValue if there is no value at that index or if it is not a Boolean
	 * or the String "true" or "false" (case insensitive).
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            A boolean default.
	 * @return The truth.
	 */
	public Boolean optBoolean(int index, Boolean defaultValue)
		{
		JsonValue object=opt(index);
		if(object instanceof JsonValue.Native)
			{
			JsonValue.Native o=(JsonValue.Native)object;
			if(o.value() instanceof Boolean)
				return (Boolean)o.value();
			}
		return defaultValue;
		}

	/**
	 * Get the optional double value associated with an index. NaN is returned
	 * if there is no value for the index, or if the value is not a number and
	 * cannot be converted to a number.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 */
	public Double optDouble(int index)
		{
		return optDouble(index, null);
		}

	/**
	 * Get the optional double value associated with an index. The defaultValue
	 * is returned if there is no value for the index, or if the value is not a
	 * number and cannot be converted to a number.
	 * 
	 * @param index
	 *            subscript
	 * @param defaultValue
	 *            The default value.
	 * @return The value.
	 */
	public Double optDouble(int index, Double defaultValue)
		{
		JsonValue object=opt(index);
		if(object instanceof JsonValue.Native)
			{
			JsonValue.Native o=(JsonValue.Native)object;
			if(o.value() instanceof Number)
				return ((Number)o.value()).doubleValue();
			}
		return defaultValue;
		}

	/**
	 * Get the optional int value associated with an index. Zero is returned if
	 * there is no value for the index, or if the value is not a number and
	 * cannot be converted to a number.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 */
	public Integer optInt(int index)
		{
		return optInt(index, null);
		}

	/**
	 * Get the optional int value associated with an index. The defaultValue is
	 * returned if there is no value for the index, or if the value is not a
	 * number and cannot be converted to a number.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            The default value.
	 * @return The value.
	 */
	public Integer optInt(int index, Integer defaultValue)
		{
		JsonValue object=opt(index);
		if(object instanceof JsonValue.Native)
			{
			JsonValue.Native o=(JsonValue.Native)object;
			if(o.value() instanceof Number)
				return ((Number)o.value()).intValue();
			}
		return defaultValue;
		}

	/**
	 * Get the optional JsonArray associated with an index.
	 * 
	 * @param index
	 *            subscript
	 * @return A JsonArray value, or null if the index has no value, or if the
	 *         value is not a JsonArray.
	 */
	public JsonArray optJsonArray(int index)
		{
		JsonValue o=opt(index);
		return o instanceof JsonArray?(JsonArray)o:null;
		}

	/**
	 * Get the optional JsonObject associated with an index. Null is returned if
	 * the key is not found, or null if the index has no value, or if the value
	 * is not a JsonObject.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return A JsonObject value.
	 */
	public JsonObject optJsonObject(int index)
		{
		JsonValue o=opt(index);
		return o instanceof JsonObject?(JsonObject)o:null;
		}

	/**
	 * Get the optional long value associated with an index. Zero is returned if
	 * there is no value for the index, or if the value is not a number and
	 * cannot be converted to a number.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 */
	public Long optLong(int index)
		{
		return optLong(index, null);
		}

	/**
	 * Get the optional long value associated with an index. The defaultValue is
	 * returned if there is no value for the index, or if the value is not a
	 * number and cannot be converted to a number.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            The default value.
	 * @return The value.
	 */
	public Long optLong(int index, Long defaultValue)
		{
		JsonValue object=opt(index);
		if(object instanceof JsonValue.Native)
			{
			JsonValue.Native o=(JsonValue.Native)object;
			if(o.value() instanceof Number)
				return ((Number)o.value()).longValue();
			}
		return defaultValue;
		}

	/**
	 * Get the optional string value associated with an index. It returns an
	 * empty string if there is no value at that index. If the value is not a
	 * string and is not null, then it is coverted to a string.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return A String value.
	 */
	public String optString(int index)
		{
		return optString(index, null);
		}

	/**
	 * Get the optional string associated with an index. The defaultValue is
	 * returned if the key is not found.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            The default value.
	 * @return A String value.
	 */
	public String optString(int index, String defaultValue)
		{
		JsonValue object=opt(index);
		return object!=null&&object instanceof JsonValue.JsonString?((JsonValue.JsonString)object).value():defaultValue;
		}

	/**
	 * Append a boolean value. This increases the array's length by one.
	 * 
	 * @param value
	 *            A boolean value.
	 * @return this.
	 */
	public JsonArray put(boolean value)
		{
		put(value?Boolean.TRUE:Boolean.FALSE);
		return this;
		}

	/**
	 * Put a value in the JsonArray, where the value will be a JsonArray which
	 * is produced from a Collection.
	 * 
	 * @param value
	 *            A Collection value.
	 * @return this.
	 */
	public JsonArray put(Collection<?> value)
		{
		put(new JsonArray(value));
		return this;
		}

	/**
	 * Append a double value. This increases the array's length by one.
	 * 
	 * @param value
	 *            A double value.
	 * @throws JsonException
	 *             if the value is not finite.
	 * @return this.
	 */
	public JsonArray put(double value) throws JsonException
		{
		Double d=new Double(value);
		JsonObject.testValidity(d);
		put(d);
		return this;
		}

	/**
	 * Append an int value. This increases the array's length by one.
	 * 
	 * @param value
	 *            An int value.
	 * @return this.
	 */
	public JsonArray put(int value)
		{
		put(new Integer(value));
		return this;
		}

	/**
	 * Append an long value. This increases the array's length by one.
	 * 
	 * @param value
	 *            A long value.
	 * @return this.
	 */
	public JsonArray put(long value)
		{
		put(new Long(value));
		return this;
		}

	/**
	 * Append an object value. This increases the array's length by one.
	 * 
	 * @param value
	 *            An object value. The value should be a Boolean, Double,
	 *            Integer, JsonArray, JsonObject, Long, or String, or the
	 *            JsonObject.NULL object.
	 * @return this.
	 */
	public JsonArray put(Object value)
		{
		this.myArrayList.add(JsonUtils.wrap(value));
		return this;
		}

	/**
	 * Put or replace a boolean value in the JsonArray. If the index is greater
	 * than the length of the JsonArray, then null elements will be added as
	 * necessary to pad it out.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            A boolean value.
	 * @return this.
	 * @throws JsonException
	 *             If the index is negative.
	 */
	public JsonArray put(int index, boolean value) throws JsonException
		{
		put(index, value?Boolean.TRUE:Boolean.FALSE);
		return this;
		}

	/**
	 * Put a value in the JsonArray, where the value will be a JsonArray which
	 * is produced from a Collection.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            A Collection value.
	 * @return this.
	 * @throws JsonException
	 *             If the index is negative or if the value is not finite.
	 */
	public JsonArray put(int index, Collection<?> value) throws JsonException
		{
		put(index, new JsonArray(value));
		return this;
		}

	/**
	 * Put or replace a double value. If the index is greater than the length of
	 * the JsonArray, then null elements will be added as necessary to pad it
	 * out.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            A double value.
	 * @return this.
	 * @throws JsonException
	 *             If the index is negative or if the value is not finite.
	 */
	public JsonArray put(int index, double value) throws JsonException
		{
		put(index, new Double(value));
		return this;
		}

	/**
	 * Put or replace an int value. If the index is greater than the length of
	 * the JsonArray, then null elements will be added as necessary to pad it
	 * out.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            An int value.
	 * @return this.
	 * @throws JsonException
	 *             If the index is negative.
	 */
	public JsonArray put(int index, int value) throws JsonException
		{
		put(index, new Integer(value));
		return this;
		}

	/**
	 * Put or replace a long value. If the index is greater than the length of
	 * the JsonArray, then null elements will be added as necessary to pad it
	 * out.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            A long value.
	 * @return this.
	 * @throws JsonException
	 *             If the index is negative.
	 */
	public JsonArray put(int index, long value) throws JsonException
		{
		put(index, new Long(value));
		return this;
		}

	/**
	 * Put or replace an object value in the JsonArray. If the index is greater
	 * than the length of the JsonArray, then null elements will be added as
	 * necessary to pad it out.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            The value to put into the array. The value should be a
	 *            Boolean, Double, Integer, JsonArray, JsonObject, Long, or
	 *            String, or the JsonObject.NULL object.
	 * @return this.
	 * @throws JsonException
	 *             If the index is negative or if the the value is an invalid
	 *             number.
	 */
	public JsonArray put(int index, Object value) throws JsonException
		{
		JsonObject.testValidity(value);
		if(index<0)
			{
			throw new JsonException("JsonArray["+index+"] not found.");
			}
		if(index<length())
			{
			this.myArrayList.set(index, JsonUtils.wrap(value));
			}
		else
			{
			while (index!=length())
				{
				put(JsonUtils.NULL);
				}
			put(value);
			}
		return this;
		}

	/**
	 * Remove an index and close the hole.
	 * 
	 * @param index
	 *            The index of the element to be removed.
	 * @return The value that was associated with the index, or null if there
	 *         was no value.
	 */
	public Object remove(int index)
		{
		Object o=opt(index);
		this.myArrayList.remove(index);
		return o;
		}

	/**
	 * Produce a JsonObject by combining a JsonArray of names with the values of
	 * this JsonArray.
	 * 
	 * @param names
	 *            A JsonArray containing a list of key strings. These will be
	 *            paired with the values.
	 * @return A JsonObject, or null if there are no names or if this JsonArray
	 *         has no values.
	 * @throws JsonException
	 *             If any of the names are null.
	 */
	public JsonObject toJsonObject(JsonArray names) throws JsonException
		{
		if(names==null||names.length()==0||length()==0)
			{
			return null;
			}
		JsonObject jo=new JsonObject();
		for(int i=0; i<names.length(); i+=1)
			{
			jo.put(names.getString(i), this.opt(i));
			}
		return jo;
		}

	/**
	 * Make a Json text of this JsonArray. For compactness, no unnecessary
	 * whitespace is added. If it is not possible to produce a syntactically
	 * correct Json text then null will be returned instead. This could occur if
	 * the array contains an invalid number.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @return a printable, displayable, transmittable representation of the
	 *         array.
	 */
	public String toString()
		{
		try
			{
			return '['+join(",")+']';
			}
		catch (Exception e)
			{
			return null;
			}
		}

	/**
	 * Make a prettyprinted Json text of this JsonArray. Warning: This method
	 * assumes that the data structure is acyclical.
	 * 
	 * @param indentFactor
	 *            The number of spaces to add to each level of indentation.
	 * @return a printable, displayable, transmittable representation of the
	 *         object, beginning with <code>[</code>&nbsp;<small>(left
	 *         bracket)</small> and ending with <code>]</code>
	 *         &nbsp;<small>(right bracket)</small>.
	 * @throws JsonException
	 */
	public String toString(int indentFactor) throws JsonException
		{
		return toString(indentFactor, 0);
		}

	/**
	 * Make a prettyprinted Json text of this JsonArray. Warning: This method
	 * assumes that the data structure is acyclical.
	 * 
	 * @param indentFactor
	 *            The number of spaces to add to each level of indentation.
	 * @param indent
	 *            The indention of the top level.
	 * @return a printable, displayable, transmittable representation of the
	 *         array.
	 * @throws JsonException
	 */
	public String toString(int indentFactor, int indent)
		{
		StringBuilder sb=new StringBuilder();
		toString(sb, indentFactor, indent);
		return sb.toString();
		}

	public void toString(StringBuilder sb)
		{
		int len=length();
		sb.append('[');
		for(int i=0; i<len; i+=1)
			{
			if(i>0)
				sb.append(',');
			this.myArrayList.get(i).toString(sb);
			}
		sb.append(']');
		}

	public void toString(StringBuilder sb, int indentFactor, int indent)
		{
		int len=length();
		if(len==0)
			{
			sb.append("[]");
			return;
			}
		int i;
		if(len==1)
			myArrayList.get(0).toString(sb, indentFactor, indent);
		else
			{
			int newindent=indent+indentFactor;
			sb.append('\n');
			for(i=0; i<len; i+=1)
				{
				if(i>0)
					{
					sb.append(",\n");
					}
				for(int j=0; j<newindent; j+=1)
					{
					sb.append(' ');
					}
				this.myArrayList.get(i).toString(sb, indentFactor, newindent);
				}
			sb.append('\n');
			for(i=0; i<indent; i+=1)
				{
				sb.append(' ');
				}
			}
		sb.append(']');
		}

	/**
	 * Write the contents of the JsonArray as Json text to a writer. For
	 * compactness, no whitespace is added.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @return The writer.
	 */
	public void write(Writer writer) throws IOException
		{
		boolean b=false;
		int len=length();

		writer.write('[');

		for(int i=0; i<len; i+=1)
			{
			if(b)
				{
				writer.write(',');
				}
			JsonValue v=this.myArrayList.get(i);
			v.write(writer);
			b=true;
			}
		writer.write(']');
		}

	public void write(Writer writer, int indent) throws IOException
		{
		write(writer, indent, 0);
		}

	public void write(Writer writer, int indentFactor, int indent) throws IOException
		{
		boolean b=false;
		int len=length();
		for(int i=0; i<indent; i++)
			writer.write(' ');
		writer.write("[ ");
		indent+=indentFactor;

		for(int i=0; i<len; i+=1)
			{
			if(b)
				writer.write(", ");
			JsonValue v=this.myArrayList.get(i);
			v.write(writer, indentFactor, indent);
			b=true;
			}
		indent-=indentFactor;
		writer.write(" ]");
		}

	public boolean contains(Object o)
		{
		return myArrayList.contains(o==null?JsonUtils.NULL:o);
		}
	}
