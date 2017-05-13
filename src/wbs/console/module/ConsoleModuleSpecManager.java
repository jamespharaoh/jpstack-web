package wbs.console.module;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class ConsoleModuleSpecManager {

	List <ConsoleModuleSpec> specs;

	Map <String, ConsoleModuleSpec> specsByName;

}
