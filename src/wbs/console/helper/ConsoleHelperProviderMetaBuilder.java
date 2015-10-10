package wbs.console.helper;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.split;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleMetaModuleBuilderHandler;
import wbs.console.context.ConsoleContextMetaBuilderContainer;
import wbs.console.module.ConsoleMetaModuleImpl;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectManager;

@PrototypeComponent ("consoleHelperProviderMetaBuilder")
@ConsoleMetaModuleBuilderHandler
public
class ConsoleHelperProviderMetaBuilder {

	@Inject
	ObjectManager objectManager;

	// prototype dependencies

	@Inject
	Provider<GenericConsoleHelperProvider> genericConsoleHelperProviderProvider;

	// builder

	@BuilderParent
	ConsoleContextMetaBuilderContainer contextMetaBuilderContainer;

	@BuilderSource
	ConsoleHelperProviderSpec consoleHelperProviderSpec;

	@BuilderTarget
	ConsoleMetaModuleImpl consoleMetaModule;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		ObjectHelper<?> objectHelper =
			objectManager.objectHelperForObjectName (
				consoleHelperProviderSpec.objectName ());

		List<String> packageNameParts =
			split (
				objectHelper.objectClass ().getPackage ().getName (),
				"\\.");

		String consoleHelperClassName =
			stringFormat (
				"%s.console.%sConsoleHelper",
				joinWithSeparator (
					".",
					packageNameParts.subList (
						0,
						packageNameParts.size () - 1)),
				capitalise (objectHelper.objectName ()));

		Class<?> consoleHelperClass;

		try {

			consoleHelperClass =
				Class.forName (
					consoleHelperClassName);

		} catch (ClassNotFoundException exception) {

			throw new RuntimeException (
				exception);

		}

		genericConsoleHelperProviderProvider.get ()

			.consoleHelperProviderSpec (
				consoleHelperProviderSpec)

			.objectHelper (
				objectHelper)

			.consoleHelperClass (
				consoleHelperClass)

			.init ();

	}

}
