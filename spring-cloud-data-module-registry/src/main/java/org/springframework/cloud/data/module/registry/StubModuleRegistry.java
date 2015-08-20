/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.data.module.registry;

import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.data.core.ModuleCoordinates;

/**
 * @author Mark Fisher
 */
public class StubModuleRegistry implements ModuleRegistry {

	private static final String GROUP_ID = "org.springframework.cloud.stream.module";

	private static final String VERSION = "1.0.0.BUILD-SNAPSHOT";

	private final Map<String, ModuleCoordinates> sources = new HashMap<>();

	private final Map<String, ModuleCoordinates> processors = new HashMap<>();

	private final Map<String, ModuleCoordinates> sinks = new HashMap<>();

	public StubModuleRegistry() {
		sources.put("time", defaultCoordinatesFor("time-source"));
		processors.put("filter", defaultCoordinatesFor("filter-processor"));		
		processors.put("groovy-filter", defaultCoordinatesFor("groovy-filter-processor"));
		processors.put("groovy-transform", defaultCoordinatesFor("groovy-transform-processor"));
		processors.put("transform", defaultCoordinatesFor("transform-processor"));
		sinks.put("counter", defaultCoordinatesFor("counter-sink"));
		sinks.put("log", defaultCoordinatesFor("log-sink"));
	}

	@Override
	public ModuleCoordinates findByNameAndType(String name, String type) {
		if ("source".equals(type)) {
			return sources.get(name);
		}
		if ("processor".equals(type)) {
			return processors.get(name);
		}
		if ("sink".equals(type)) {
			return sinks.get(name);
		}
		throw new UnsupportedOperationException("only 'source', 'processor', and 'sink' types are supported");
	}

	private ModuleCoordinates defaultCoordinatesFor(String moduleName) {
		return ModuleCoordinates.parse(String.format("%s:%s:%s", GROUP_ID, moduleName, VERSION));
	}
}
