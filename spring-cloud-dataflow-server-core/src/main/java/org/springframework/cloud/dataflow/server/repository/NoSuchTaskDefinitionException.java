/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.cloud.dataflow.server.repository;

/**
 * Thrown when a task definition of a given name was expected but did not exist.
 *
 * @author Eric Bottard
 */
public class NoSuchTaskDefinitionException extends RuntimeException {

	/**
	 * The name of the task definition that could not be found.
	 */
	private final String name;

	public NoSuchTaskDefinitionException(String name, String message) {
		super(message);
		this.name = name;
	}

	public NoSuchTaskDefinitionException(String name) {
		this(name, "Could not find task definition named " + name);
	}
}
