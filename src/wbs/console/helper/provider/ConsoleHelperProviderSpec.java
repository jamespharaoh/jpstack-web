package wbs.console.helper.provider;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContextStuffSpec;
import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataSetupMethod;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@Data
@DataClass ("console-helper-provider")
@PrototypeComponent ("consoleHelperProviderSpec")
public
class ConsoleHelperProviderSpec
	implements ConsoleSpec {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

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

	@DataAttribute
	String createPriv;

	@DataAttribute (
		name = "cryptor")
	String cryptorBeanName;

	// children

	@DataChildren (
		direct = true,
		childElement = "priv-key")
	List <PrivKeySpec> privKeys =
		new ArrayList<> ();

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

	@DataSetupMethod
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			if (idKey () == null) {

				idKey (
					stringFormat (
						"%sId",
						objectName ()));

			}

		}

	}

}
