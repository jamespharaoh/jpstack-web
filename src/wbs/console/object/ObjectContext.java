package wbs.console.object;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.lookup.ObjectLookup;
import wbs.console.lookup.StringLookup;
import wbs.console.request.Cryptor;
import wbs.console.tab.ConsoleContextTab;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.web.WebFile;

import com.google.common.base.Optional;

@Accessors (fluent = true)
@DataClass ("object-context")
@PrototypeComponent ("objectContext")
public
class ObjectContext
	extends AbstractObjectContext {

	@DataAttribute
	@Getter @Setter
	String name;

	@DataAttribute
	@Getter @Setter
	String typeName;

	@DataAttribute
	@Getter @Setter
	String pathPrefix;

	@DataAttribute
	@Getter @Setter
	Boolean global;

	@DataAttribute
	@Getter @Setter
	String parentContextName;

	@DataAttribute
	@Getter @Setter
	String parentContextTabName;

	@Getter @Setter
	Map<String,ConsoleContextTab> contextTabs =
		new LinkedHashMap<String,ConsoleContextTab> ();

	@Getter @Setter
	Map<String,WebFile> files =
		new LinkedHashMap<String,WebFile> ();

	@Getter @Setter
	String requestIdKey;

	@Getter @Setter
	String title;

	@Getter @Setter
	StringLookup titleLookup;

	@Getter @Setter
	ObjectLookup<?> objectLookup;

	@Getter @Setter
	String postProcessorName;

	@Getter @Setter
	Cryptor cryptor;

	@Getter @Setter
	Map<String,Object> stuff;

	@Getter @Setter
	Optional<String> defaultFileName;

}
