package wbs.console.object;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ConsoleContextBuilderContainerImplementation;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleHelperRegistry;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@Log4j
@PrototypeComponent ("extendContextBuilder")
@ConsoleModuleBuilderHandler
public
class ExtendContextBuilder {

	// dependencies

	@Inject
	ConsoleHelperRegistry consoleHelperRegistry;

	@Inject
	ConsoleMetaManager consoleMetaManager;

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer container;

	@BuilderSource
	ExtendContextSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	String name;
	String baseName;
	String extensionPointName;

	ConsoleHelper<?> consoleHelper;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

		buildChildren (
			builder);

	}

	void buildChildren (
			Builder builder) {

		List<ResolvedConsoleContextExtensionPoint> resolvedExtensionPoints =
			consoleMetaManager.resolveExtensionPoint (
				extensionPointName);

		if (resolvedExtensionPoints == null) {

			log.warn (
				stringFormat (
					"Extend context %s in %s doesn't resolve",
					extensionPointName,
					spec.consoleSpec ().name ()));

			return;

		}

		ConsoleContextBuilderContainer nextBuilderContainer =
			new ConsoleContextBuilderContainerImplementation ()

			.consoleHelper (
				consoleHelper)

			.structuralName (
				baseName)

			.extensionPointName (
				extensionPointName)

			.pathPrefix (
				baseName)

			.newBeanNamePrefix (
				consoleHelper.objectName ())

			.existingBeanNamePrefix (
				consoleHelper.objectName ())

			.tabLocation (
				extensionPointName)

			.friendlyName (
				consoleHelper.friendlyName ());

		builder.descend (
			nextBuilderContainer,
			spec.children (),
			consoleModule,
			MissingBuilderBehaviour.error);

	}

	// defaults

	void setDefaults () {

		name =
			spec.name ();

		baseName =
			spec.baseName ();

		extensionPointName =
			spec.extensionPointName ();

		consoleHelper =
			spec.objectName () != null
				? consoleHelperRegistry.findByObjectName (
					spec.objectName ())
				: null;

	}

}
