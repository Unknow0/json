package unknow.json;

import java.lang.reflect.*;

public class JsonUtils
	{
	/**
	 * It is sometimes more convenient and less ambiguous to have a
	 * <code>NULL</code> object than to use Java's <code>null</code> value.
	 * <code>JsonObject.NULL.equals(null)</code> returns <code>true</code>.
	 * <code>JsonObject.NULL.toString()</code> returns <code>"null"</code>.
	 */
	public static final JsonValue NULL=new JsonValue.Null();

	/**
	 * Wrap an object, if necessary. If the object is null, return the NULL
	 * object. If it is an array wrap it in a JsonArray. . If it is a standard
	 * property (Double, String, et al) then it is already wrapped. If the
	 * wrapping fails, then null is returned.
	 * 
	 * @param object
	 *            The object to wrap
	 * @return The wrapped value
	 */
	public static JsonValue wrap(Object object)
		{
		if(object==null)
			return NULL;
		if(object instanceof String)
			return new JsonValue.JsonString((String)object);
		if(object instanceof JsonValue)
			return (JsonValue)object;
		if(object instanceof Byte||object instanceof Character||object instanceof Short||object instanceof Integer||object instanceof Long||object instanceof Boolean||object instanceof Float||object instanceof Double)
			return new JsonValue.Native(object);
		if(object.getClass().isArray())
			return new JsonArray(object);
		return null;
		}

	/**
	 * Produce a string in double quotes with backslash sequences in all the
	 * right places. A backslash will be inserted within </, producing <\/,
	 * allowing Json text to be delivered in HTML. In Json text, a string cannot
	 * contain a control character or an unescaped quote or backslash.
	 * 
	 * @param string
	 *            A String
	 * @return A String correctly formatted for insertion in a Json text.
	 */
	public static String quote(String string)
		{
		if(string==null||string.length()==0)
			{
			return "\"\"";
			}

		char b;
		char c=0;
		String hhhh;
		int i;
		int len=string.length();
		StringBuffer sb=new StringBuffer(len+4);

		sb.append('"');
		for(i=0; i<len; i+=1)
			{
			b=c;
			c=string.charAt(i);
			switch (c)
				{
				case '\\':
				case '"':
					sb.append('\\');
					sb.append(c);
					break;
				case '/':
					if(b=='<')
						{
						sb.append('\\');
						}
					sb.append(c);
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\r':
					sb.append("\\r");
					break;
				default:
					if(c<' '||(c>='\u0080'&&c<'\u00a0')||(c>='\u2000'&&c<'\u2100'))
						{
						hhhh="000"+Integer.toHexString(c);
						sb.append("\\u"+hhhh.substring(hhhh.length()-4));
						}
					else
						{
						sb.append(c);
						}
				}
			}
		sb.append('"');
		return sb.toString();
		}

	/**
	 * Try to convert a string into a number, boolean, or null. If the string
	 * can't be converted, return the string.
	 * 
	 * @param string
	 *            A String.
	 * @return A simple Json value.
	 */
	public static Object stringToValue(String string)
		{
		if(string.equals(""))
			{
			return string;
			}
		if(string.equalsIgnoreCase("true"))
			{
			return Boolean.TRUE;
			}
		if(string.equalsIgnoreCase("false"))
			{
			return Boolean.FALSE;
			}
		if(string.equalsIgnoreCase("null"))
			{
			return JsonUtils.NULL;
			}

		/*
		 * If it might be a number, try converting it. We support the
		 * non-standard 0x- convention. If a number cannot be produced, then the
		 * value will just be a string. Note that the 0x-, plus, and implied
		 * string conventions are non-standard. A Json parser may accept
		 * non-Json forms as long as it accepts all correct Json forms.
		 */

		char b=string.charAt(0);
		if((b>='0'&&b<='9')||b=='.'||b=='-'||b=='+')
			{
			if(b=='0'&&string.length()>2&&(string.charAt(1)=='x'||string.charAt(1)=='X'))
				{
				try
					{
					return new Integer(Integer.parseInt(string.substring(2), 16));
					}
				catch (Exception ignore)
					{
					}
				}
			try
				{
				if(string.indexOf('.')>-1||string.indexOf('e')>-1||string.indexOf('E')>-1)
					{
					return Double.valueOf(string);
					}
				else
					{
					Long myLong=new Long(string);
					if(myLong.longValue()==myLong.intValue())
						{
						return new Integer(myLong.intValue());
						}
					else
						{
						return myLong;
						}
					}
				}
			catch (Exception ignore)
				{
				}
			}
		return string;
		}

	public static JsonValue toJsonValue(Object o)
		{
		if(o==null)
			return NULL;
		if(o instanceof String)
			return new JsonValue.JsonString((String)o);
		if(o instanceof JsonValue)
			return (JsonValue)o;
		if(o instanceof Byte||o instanceof Character||o instanceof Short||o instanceof Integer||o instanceof Long||o instanceof Boolean||o instanceof Float||o instanceof Double)
			return new JsonValue.Native(o);
		if(o.getClass().isArray())
			{
			JsonArray a=new JsonArray();
			Object[] t=(Object[])o;
			for(int i=0; i<t.length; i++)
				a.put(toJsonValue(t[i]));
			return a;
			}
		JsonObject obj=new JsonObject();
		Field[] fields=o.getClass().getDeclaredFields();
		for(int i=0; i<fields.length; i++)
			{
			Field f=fields[i];
			try
				{
				obj.put(f.getName(), f.get(o));
				}
			catch (IllegalAccessException e)
				{
				}
			}
		return obj;
		}
	}
