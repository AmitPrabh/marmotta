/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kiwi.core.exception;

/**
 * Thrown to indicate that a user already exists when trying to create a new one.
 * 
 * @author Sebastian Schaffert
 *
 */
public class UserExistsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8670379009777836957L;

	/**
	 * 
	 */
	public UserExistsException() {
	}

	/**
	 * @param message
	 */
	public UserExistsException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public UserExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UserExistsException(String message, Throwable cause) {
		super(message, cause);
	}

}
