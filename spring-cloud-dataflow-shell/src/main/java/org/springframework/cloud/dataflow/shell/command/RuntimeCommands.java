/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.cloud.dataflow.shell.command;

import static org.springframework.shell.table.BorderSpecification.TOP;
import static org.springframework.shell.table.BorderStyle.fancy_light;
import static org.springframework.shell.table.BorderStyle.fancy_light_quadruple_dash;
import static org.springframework.shell.table.CellMatchers.column;
import static org.springframework.shell.table.CellMatchers.ofType;
import static org.springframework.shell.table.SimpleHorizontalAligner.center;
import static org.springframework.shell.table.SimpleVerticalAligner.middle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.dataflow.rest.client.RuntimeOperations;
import org.springframework.cloud.dataflow.rest.resource.AppInstanceStatusResource;
import org.springframework.cloud.dataflow.rest.resource.AppStatusResource;
import org.springframework.cloud.dataflow.shell.config.DataFlowShell;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.shell.table.Table;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.shell.table.TableModelBuilder;
import org.springframework.shell.table.Tables;
import org.springframework.stereotype.Component;

/**
 * Commands for displaying the runtime state of deployed apps.
 *
 * @author Eric Bottard
 * @author Mark Fisher
 */
@Component
public class RuntimeCommands implements CommandMarker {

	private static final String LIST_MODULES = "runtime modules";

	@Autowired
	private DataFlowShell dataFlowShell;

	@CliAvailabilityIndicator({LIST_MODULES})
	public boolean available() {
		return dataFlowShell.getDataFlowOperations() != null;
	}

	@CliCommand(value = LIST_MODULES, help = "List runtime modules")
	public Table list(
			@CliOption(key = "summary", help = "whether to hide module instances details",
					unspecifiedDefaultValue = "false", specifiedDefaultValue = "true") boolean summary,
			@CliOption(key = {"moduleId", "moduleIds"}, help = "module id(s) to display, also supports '<group>.*' pattern") String[] moduleIds) {

		Set<String> filter = null;
		if (moduleIds != null) {
			filter = new HashSet<>(Arrays.asList(moduleIds));
		}

		TableModelBuilder<Object> modelBuilder = new TableModelBuilder<>();
		if (!summary) {
			modelBuilder.addRow()
					.addValue("Module Id / Instance Id")
					.addValue("Unit Status")
					.addValue("No. of Instances / Attributes");
		}
		else {
			modelBuilder.addRow()
					.addValue("Module Id")
					.addValue("Unit Status")
					.addValue("No. of Instances");
		}

		// In detailed mode, keep track of module vs instance lines, to use
		// a different border style later.
		List<Integer> splits = new ArrayList<>();
		int line = 1;
		// Optimise for the 1 module case, which is likely less resource intensive on the server
		// than client side filtering
		Iterable<AppStatusResource> statuses;
		if (filter != null && filter.size() == 1 && !filter.iterator().next().endsWith(".*")) {
			statuses = Collections.singleton(runtimeOperations().status(filter.iterator().next()));
		}
		else {
			statuses = runtimeOperations().status();
		}
		for (AppStatusResource appStatusResource : statuses) {
			if (filter != null && !shouldRetain(filter, appStatusResource)) {
				continue;
			}
			modelBuilder.addRow()
					.addValue(appStatusResource.getDeploymentId())
					.addValue(appStatusResource.getState())
					.addValue(appStatusResource.getInstances().getContent().size());
			splits.add(line);
			line++;
			if (!summary) {
				for (AppInstanceStatusResource appInstanceStatusResource : appStatusResource.getInstances()) {
					modelBuilder.addRow()
							.addValue(appInstanceStatusResource.getInstanceId())
							.addValue(appInstanceStatusResource.getState())
							.addValue(appInstanceStatusResource.getAttributes());
					line++;
				}
			}
		}

		TableModel model = modelBuilder.build();
		final TableBuilder builder = new TableBuilder(model);
		DataFlowTables.applyStyle(builder);
		builder.on(column(0)).addAligner(middle)
				.on(column(1)).addAligner(middle)
				.on(column(1)).addAligner(center)
				// This will match the "number of instances" cells only
				.on(ofType(Integer.class)).addAligner(center);


		Tables.configureKeyValueRendering(builder, " = ");
		for (int i = 2; i < model.getRowCount(); i++) {
			if (splits.contains(i)) {
				builder.paintBorder(fancy_light, TOP).fromRowColumn(i, 0).toRowColumn(i + 1, model.getColumnCount());
			}
			else {
				builder.paintBorder(fancy_light_quadruple_dash, TOP).fromRowColumn(i, 0).toRowColumn(i + 1, model.getColumnCount());
			}
		}

		return builder.build();
	}

	private boolean shouldRetain(Set<String> filter, AppStatusResource appStatusResource) {
		String deploymentId = appStatusResource.getDeploymentId();
		boolean directMatch = filter.contains(deploymentId);
		if (directMatch) {
			return true;
		}
		for (String candidate : filter) {
			if (candidate.endsWith(".*")) {
				String pattern = candidate.substring(0, candidate.length() - "*".length());
				if (deploymentId.startsWith(pattern)) {
					return true;
				}
			}
		}
		return false;
	}

	private RuntimeOperations runtimeOperations() {
		return dataFlowShell.getDataFlowOperations().runtimeOperations();
	}
}
