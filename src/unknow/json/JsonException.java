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

/**
 * The JSONException is thrown by the JSON.org classes when things are amiss.
 */
public class JsonException extends Exception {
	private static final long serialVersionUID = 0;
	/**
	 * @uml.property  name="cause"
	 */
	private Throwable cause;

    /**
     * Constructs a JSONException with an explanatory message.
     * @param message Detail about the reason for the exception.
     */
    public JsonException(String message) {
        super(message);
    }

    public JsonException(Throwable cause) {
        super(cause.getMessage());
        this.cause = cause;
    }

    /**
	 * @return
	 * @uml.property  name="cause"
	 */
    public Throwable getCause() {
        return this.cause;
    }
}
