package wbs.platform.console.object;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.context.ConsoleContextBuilderContainer;
import wbs.platform.console.context.ConsoleContextBuilderContainerImpl;
import wbs.platform.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.helper.ConsoleHelperRegistry;
import wbs.platform.console.metamodule.ConsoleMetaManager;
import wbs.platform.console.module.ConsoleModuleImpl;
import wbs.platform.console.spec.ConsoleSimpleBuilderContainer;

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
	ConsoleSimpleBuilderContainer container;

	@BuilderSource
	ExtendContextSpec spec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

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
			new ConsoleContextBuilderContainerImpl ()

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
			consoleModule);

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
