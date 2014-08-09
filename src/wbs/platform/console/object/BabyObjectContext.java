package wbs.platform.console.object;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.record.Record;
import wbs.framework.web.WebFile;
import wbs.platform.console.lookup.ObjectLookup;
import wbs.platform.console.lookup.StringLookup;
import wbs.platform.console.request.Cryptor;
import wbs.platform.console.tab.ConsoleContextTab;

@Accessors (fluent = true)
@DataClass ("baby-object-context")
@PrototypeComponent ("babyObjectContext")
public
class BabyObjectContext
	extends AbstractBabyObjectContext {

	// properties

	@DataAttribute
	@Getter @Setter
	String name;

	@DataAttribute ("type")
	@Getter @Setter
	String typeName;

	@DataAttribute
	@Getter @Setter
	String pathPrefix;

	@DataAttribute
	@Getter @Setter
	Boolean global;

	@DataAttribute ("parent")
	@Getter @Setter
	String parentContextName;

	@DataAttribute ("parent-tab")
	@Getter @Setter
	String parentContextTabName;

	@Getter @Setter
	Map<String,ConsoleContextTab> contextTabs =
		new LinkedHashMap<String,ConsoleContextTab> ();

	@Getter @Setter
	Map<String,WebFile> files =
		new LinkedHashMap<String,WebFile> ();

	@Getter @Setter
	Cryptor cryptor;

	@DataAttribute
	@Getter @Setter
	String requestIdKey;

	@Getter @Setter
	StringLookup titleLookup;

	@Getter @Setter
	ObjectLookup<? extends Record<?>> objectLookup;

	@DataAttribute
	@Getter @Setter
	String postProcessorName;

	@DataAttribute
	@Getter @Setter
	String title;

	@Getter @Setter
	Map<String,Object> stuff;

}
