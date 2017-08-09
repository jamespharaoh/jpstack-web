package wbs.console.supervisor;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@Data
public
class SupervisorBuilderContext {

	TaskLogger taskLogger;

	SupervisorConfigSpec config;

}
