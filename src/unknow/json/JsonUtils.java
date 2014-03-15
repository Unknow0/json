/*******************************************************************************
 * Copyright (c) 2014 Unknow.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * Contributors:
 * Unknow - initial API and implementation
 ******************************************************************************/
package unknow.json;


public class JsonUtils
	{
	/**
	 * Wrap an object, if necessary. If the object is null, return the NULL
	 * object. If it is an array wrap it in a JsonArray. . If the
	 * wrapping fails, then null is returned.
	 * 
	 * @param object
	 *            The object to wrap
	 * @return The wrapped value
	 */
	public static final JsonValue wrap(Object object)
		{
		if(object==null)
			return JsonValue.NULL;
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
	public static JsonValue stringToValue(String string)
		{
		if(string.equals(""))
			return new JsonValue.JsonString("");
		if(string.equalsIgnoreCase("true"))
			return new JsonValue.Native(Boolean.TRUE);
		if(string.equalsIgnoreCase("false"))
			return new JsonValue.Native(Boolean.FALSE);
		if(string.equalsIgnoreCase("null"))
			return JsonValue.NULL;

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
					return new JsonValue.Native(Integer.parseInt(string.substring(2), 16));
					}
				catch (Exception ignore)
					{
					}
				}
			try
				{
				if(string.indexOf('.')>-1||string.indexOf('e')>-1||string.indexOf('E')>-1)
					{
					return new JsonValue.Native(Double.valueOf(string));
					}
				else
					{
					Long myLong=new Long(string);
					if(myLong.longValue()==myLong.intValue())
						{
						return new JsonValue.Native(myLong.intValue());
						}
					else
						{
						return new JsonValue.Native(myLong);
						}
					}
				}
			catch (Exception ignore)
				{
				}
			}
		return new JsonValue.JsonString(string);
		}
	}
