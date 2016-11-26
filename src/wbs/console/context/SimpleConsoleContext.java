package wbs.console.context;

import static wbs.utils.collection.CollectionUtils.collectionHasTwoElements;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listSecondElementRequired;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.utils.string.StringUtils.stringSplitColon;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.spec.PrivKeySpec;
import wbs.console.priv.UserPrivChecker;
import wbs.console.tab.ConsoleContextTab;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.web.file.WebFile;

@Accessors (fluent = true)
@DataClass ("simple-context")
@PrototypeComponent ("simpleConsoleContext")
public
class SimpleConsoleContext
	extends ConsoleContext {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	// properties

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

	@DataAttribute
	@Getter @Setter
	String title;

	@DataAttribute
	@Getter @Setter
	String postProcessorName;

	@DataChildren
	@Getter @Setter
	Map<String,Object> stuff =
		new LinkedHashMap<String,Object> ();

	// state

	@Getter @Setter
	Map<String,ConsoleContextTab> contextTabs =
		new LinkedHashMap<String,ConsoleContextTab> ();

	@Getter @Setter
	Map<String,WebFile> files =
		new LinkedHashMap<String,WebFile> ();

	@Getter @Setter
	List<PrivKeySpec> privKeySpecs =
		new ArrayList<PrivKeySpec> ();

	// implementation

	@Override
	public
	String titleForStuff (
			ConsoleContextStuff stuff) {

		return title ();

	}

	@Override
	public
	String localPathForStuff (
			ConsoleContextStuff stuff) {

		if (parentContext () != null) {

			return parentContext ().localPathForStuff (
				stuff);

		} else {

			return super.localPathForStuff (
				stuff);

		}

	}

	@Override
	public
	void initContext (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull PathSupply pathParts,
			@NonNull ConsoleContextStuff contextStuff) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"initContext");

		// set privs

		for (
			PrivKeySpec privKeySpec
				: privKeySpecs
		) {

			List <String> privCodeParts =
				stringSplitColon (
					privKeySpec.privCode ());

			if (
				collectionHasTwoElements (
					privCodeParts)
			) {

				if (
					stringNotEqualSafe (
						listFirstElementRequired (
							privCodeParts),
						"root")
				) {
					throw new RuntimeException ();
				}

				if (
					privChecker.canRecursive (
						GlobalId.root,
						listSecondElementRequired (
							privCodeParts))
				) {

					contextStuff.grant (
						privKeySpec.name ());

				}

			} else {

				throw new RuntimeException ();

			}

		}

		// set stuff

		if (stuff () != null) {

			for (Map.Entry<String,? extends Object> ent
					: stuff ().entrySet ()) {

				contextStuff.set (
					ent.getKey (),
					ent.getValue ());

			}

		}

		// run hook

		postInitHook (
			contextStuff);

		// initialise parent

		ConsoleContext parentContext =
			parentContext ();

		if (parentContext != null) {

			parentContext.initContext (
				taskLogger,
				pathParts,
				contextStuff);

		}

		// run post processors

		if (postProcessorName () != null) {

			consoleManager.runPostProcessors (
				taskLogger,
				postProcessorName (),
				contextStuff);

		}

	}

	protected
	void postInitHook (
			ConsoleContextStuff contextStuff) {

	}

}
