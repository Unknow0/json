package unknow.json.parser;

import java.io.*;
import java.math.*;

import unknow.json.*;

public class JsonParser<T>
	{
	private JsonHandler<T> handler;

	private int character;
	private boolean eof;
	private int index;
	private int line;
	private char previous;
	private Reader reader;
	private boolean usePrevious;

	/**
	 * Construct a JsonParser from a Reader.
	 *
	 * @param handler     A handler.
	 */
	public JsonParser(JsonHandler<T> handler)
		{
		this.handler=handler;
		}

	/**
	* reset the parser and  from an InputStream.
	*/
	public T parse(InputStream inputStream) throws JsonException
		{
		return parse(new BufferedReader(new InputStreamReader(inputStream)));
		}

	/**
	 * Construct a JsonParser from a string.
	 *
	 * @param s     A source string.
	 * @throws JsonException 
	 */
	public T parse(String s) throws JsonException
		{
		return parse(new StringReader(s));
		}

	public T parse(Reader reader) throws JsonException
		{
		this.reader=reader;
		this.eof=false;
		this.usePrevious=false;
		this.previous=0;
		this.index=0;
		this.character=1;
		this.line=1;
		parseNext();
		try
			{
			reader.close();
			}
		catch (IOException e)
			{
			}
		return handler.getValue();
		}

	/**
	 * Back up one character. This provides a sort of lookahead capability,
	 * so that you can test for a digit or letter before attempting to parse
	 * the next number or identifier.
	 */
	private void back() throws JsonException
		{
		if(usePrevious||index<=0)
			{
			throw new JsonException("Stepping back two steps is not supported");
			}
		this.index-=1;
		this.character-=1;
		this.usePrevious=true;
		this.eof=false;
		}

	private boolean end()
		{
		return eof&&!usePrevious;
		}

	/**
	 * Get the next character in the source string.
	 *
	 * @return The next character, or 0 if past the end of the source string.
	 */
	private char next() throws JsonException
		{
		int c;
		if(this.usePrevious)
			{
			this.usePrevious=false;
			c=this.previous;
			}
		else
			{
			try
				{
				c=this.reader.read();
				}
			catch (IOException exception)
				{
				throw new JsonException(exception);
				}

			if(c<=0)
				{ // End of stream
				this.eof=true;
				c=0;
				}
			}
		this.index+=1;
		if(this.previous=='\r')
			{
			this.line+=1;
			this.character=c=='\n'?0:1;
			}
		else if(c=='\n')
			{
			this.line+=1;
			this.character=0;
			}
		else
			{
			this.character+=1;
			}
		this.previous=(char)c;
		return this.previous;
		}

	/**
	 * Get the next n characters.
	 *
	 * @param n     The number of characters to take.
	 * @return      A string of n characters.
	 * @throws JsonException
	 *   Substring bounds error if there are not
	 *   n characters remaining in the source string.
	 */
	private String next(int n) throws JsonException
		{
		if(n==0)
			{
			return "";
			}

		char[] chars=new char[n];
		int pos=0;

		while (pos<n)
			{
			chars[pos]=next();
			if(end())
				{
				throw syntaxError("Substring bounds error");
				}
			pos+=1;
			}
		return new String(chars);
		}

	/**
	 * Get the next char in the string, skipping whitespace.
	 * @throws JsonException
	 * @return  A character, or 0 if there are no more characters.
	 */
	private char nextClean() throws JsonException
		{
		for(;;)
			{
			char c=next();
			if(c==0||c>' ')
				{
				return c;
				}
			}
		}

	/**
	 * Return the characters up to the next close quote character.
	 * Backslash processing is done. The formal Json format does not
	 * allow strings in single quotes, but an implementation is allowed to
	 * accept them.
	 * @param quote The quoting character, either
	 *      <code>"</code>&nbsp;<small>(double quote)</small> or
	 *      <code>'</code>&nbsp;<small>(single quote)</small>.
	 * @return      A String.
	 * @throws JsonException Unterminated string.
	 */
	private String nextString(char quote) throws JsonException
		{
		char c;
		StringBuffer sb=new StringBuffer();
		for(;;)
			{
			c=next();
			switch (c)
				{
				case 0:
				case '\n':
				case '\r':
					throw syntaxError("Unterminated string");
				case '\\':
					c=next();
					switch (c)
						{
						case 'b':
							sb.append('\b');
							break;
						case 't':
							sb.append('\t');
							break;
						case 'n':
							sb.append('\n');
							break;
						case 'f':
							sb.append('\f');
							break;
						case 'r':
							sb.append('\r');
							break;
						case 'u':
							sb.append((char)Integer.parseInt(next(4), 16));
							break;
						case '"':
						case '\'':
						case '\\':
						case '/':
							sb.append(c);
							break;
						default:
							throw syntaxError("Illegal escape.");
						}
					break;
				default:
					if(c==quote)
						{
						return sb.toString();
						}
					sb.append(c);
				}
			}
		}

	/**
	 * Get the next value. The value can be a Boolean, Double, Integer,
	 * JsonArray, JsonObject, Long, or String, or the JsonObject.NULL object.
	 * @throws JsonException If syntax error.
	 *
	 * @return An object.
	 */
	private void parseNext() throws JsonException
		{
		char c=nextClean();
		String string;

		switch (c)
			{
			case '"':
			case '\'':
				handler.newString(nextString(c));
			case '{':
				handler.startObject();
				parseObject();
				handler.endObject();
			case '[':
				handler.startArray();
				parseArray();
				handler.endArray();
			}

		/*
		 * Handle unquoted text. This could be the values true, false, or
		 * null, or it can be a number. An implementation (such as this one)
		 * is allowed to also accept non-standard forms.
		 *
		 * Accumulate characters until we reach the end of the text or a
		 * formatting character.
		 */

		StringBuffer sb=new StringBuffer();
		while (c>=' '&&",:]}/\\\"[{;=#".indexOf(c)<0)
			{
			sb.append(c);
			c=next();
			}
		back();

		string=sb.toString().trim();
		if(string.equals(""))
			{
			throw syntaxError("Missing value");
			}
		stringToValue(string);
		}

	public void parseObject() throws JsonException
		{
		char c;

		for(;;)
			{
			c=nextClean();
			switch (c)
				{
				case 0:
					throw syntaxError("A JsonObject text must end with '}'");
				case '}':
					return;
				case '"':
				case '\'':
					handler.newKey(nextString(c));
					break;
				default:
					throw syntaxError("A JsonObject key must start with '\"'");
				}

			// The key is followed by ':'. We will also tolerate '=' or '=>'.
			c=nextClean();
			if(c=='=')
				{
				if(next()!='>')
					back();
				}
			else if(c!=':')
				throw syntaxError("Expected a ':' after a key");
			parseNext();

			// Pairs are separated by ','. We will also tolerate ';'.
			switch (nextClean())
				{
				case ';':
				case ',':
					if(nextClean()=='}')
						return;
					back();
					break;
				case '}':
					return;
				default:
					throw syntaxError("Expected a ',' or '}'");
				}
			}
		}

	public void parseArray() throws JsonException
		{
		if(nextClean()!=']')
			{
			back();
			for(;;)
				{
				if(nextClean()==',')
					{
					back();
					handler.newNull();
					}
				else
					{
					back();
					parseNext();
					}
				switch (nextClean())
					{
					case ';':
					case ',':
						if(nextClean()==']')
							{
							return;
							}
						back();
						break;
					case ']':
						return;
					default:
						throw syntaxError("Expected a ',' or ']'");
					}
				}
			}
		}

	public void stringToValue(String string) throws JsonException
		{
		if(string.equalsIgnoreCase("true"))
			handler.newBoolean(Boolean.TRUE);
		if(string.equalsIgnoreCase("false"))
			handler.newBoolean(Boolean.FALSE);
		if(string.equalsIgnoreCase("null"))
			handler.newNull();

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
					handler.newNumber(BigDecimal.valueOf(Long.parseLong(string.substring(2), 16)));
					}
				catch (Exception ignore)
					{
					}
				}
			try
				{
				handler.newNumber(new BigDecimal(string));
				}
			catch (Exception ignore)
				{
				}
			}
		handler.newString(string);
		}

	/**
	 * Make a JsonException to signal a syntax error.
	 *
	 * @param message The error message.
	 * @return  A JsonException object, suitable for throwing
	 */
	private JsonException syntaxError(String message)
		{
		return new JsonException(message+toString());
		}

	/**
	 * Make a printable string of this JsonTokener.
	 *
	 * @return " at {index} [character {character} line {line}]"
	 */
	public String toString()
		{
		return " at "+index+" [character "+this.character+" line "+this.line+"]";
		}
	}
