package wbs.platform.console.metamodule;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@Data
@PrototypeComponent ("resolvedConsoleContextLink")
public
class ResolvedConsoleContextLink {

	String name;
	String localName;

	String tabName;
	String tabLocation;
	String tabLabel;
	String tabPrivKey;
	String tabFile;
	List<String> tabContextTypeNames;

	List<String> parentContextNames;

	List<String> contextNamePrefixes;

}
