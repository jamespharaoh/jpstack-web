package wbs.console.context;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class ConsoleContextHint {

	String linkName;

	Boolean singular;
	Boolean plural;

}
