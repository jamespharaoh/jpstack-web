package wbs.console.context;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class ConsoleContextMetaBuilderContainer {

	String structuralName;

	String extensionPointName;

}
