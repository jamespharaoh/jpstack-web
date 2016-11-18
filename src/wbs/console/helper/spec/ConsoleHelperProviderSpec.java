package wbs.console.helper.spec;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContextStuffSpec;
import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataInitMethod;

@Accessors (fluent = true)
@Data
@DataClass ("console-helper-provider")
@PrototypeComponent ("consoleHelperProviderSpec")
@ConsoleModuleData
public
class ConsoleHelperProviderSpec {

	// attributes

	@DataAttribute (
		required = true)
	String objectName;

	@DataAttribute
	String idKey;

	@DataAttribute (
		name = "default-list-context")
	String defaultListContextName;

	@DataAttribute (
		name = "default-object-context")
	String defaultObjectContextName;

	@DataAttribute
	String viewPriv;

	@DataAttribute (
		name = "cryptor")
	String cryptorBeanName;

	// children

	@DataChildren (
		direct = true,
		childElement = "priv-key")
	List<PrivKeySpec> privKeys =
		new ArrayList<PrivKeySpec> ();

	@DataChildren (
		direct = true,
		childElement = "context-stuff")
	List <ConsoleContextStuffSpec> contextStuffs =
		new ArrayList<> ();

	@DataChildren (
		direct = true,
		childElement = "run-post-processor")
	List <RunPostProcessorSpec> runPostProcessors =
		new ArrayList<> ();

	// defaults

	@DataInitMethod
	public
	void init () {

		if (idKey () == null) {

			idKey (
				stringFormat (
					"%sId",
					objectName ()));

		}

	}

}
