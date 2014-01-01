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

import java.io.*;

public interface JsonValue
	{
	public String toString();

	public void toString(StringBuilder sb);

	public String toString(int indentFactor, int indent);

	public void toString(StringBuilder sb, int indentFactor, int indent);

	public void write(Writer writer) throws IOException;

	public void write(Writer writer, int indentFactor, int indent) throws IOException;

	public abstract class AbstractValue implements JsonValue
		{
		public void toString(StringBuilder sb)
			{
			sb.append(toString());
			}

		public String toString(int indentFactor, int indent)
			{
			return toString();
			}

		public void toString(StringBuilder sb, int indentFactor, int indent)
			{
			sb.append(toString());
			}

		public void write(Writer writer) throws IOException
			{
			writer.write(toString());
			}

		public void write(Writer writer, int indentFactor, int indent) throws IOException
			{
			writer.write(toString());
			}
		}

	public static final class Null extends AbstractValue
		{
		protected final Object clone()
			{
			return this;
			}

		public boolean equals(Object object)
			{
			return object==null||object==this;
			}

		public String toString()
			{
			return "null";
			}
		}

	public static class Native extends AbstractValue
		{
		private Object value;

		public Native(Object v)
			{
			value=v;
			}

		public String toString()
			{
			return value.toString();
			}

		public boolean equals(Object object)
			{
			if(object==this)
				return true;
			if(object!=null&&object instanceof JsonString)
				{
				Native json=(Native)object;
				return value.equals(json.value);
				}
			return false;
			}

		public Object value()
			{
			return value;
			}
		}

	public static class JsonString extends AbstractValue
		{
		private String value;

		public JsonString(String v)
			{
			value=v;
			}

		public String toString()
			{
			return JsonUtils.quote(value);
			}

		public boolean equals(Object object)
			{
			if(object==this)
				return true;
			if(object!=null&&object instanceof JsonString)
				{
				JsonString json=(JsonString)object;
				return value.equals(json.value);
				}
			return false;
			}

		public String value()
			{
			return value;
			}
		}
	}
