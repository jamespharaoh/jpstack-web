package wbs.console.combo;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.console.responder.ConsoleFile;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;

@PrototypeComponent ("simpleActionPageBuilder")
@ConsoleModuleBuilderHandler
public
class SimpleActionPageBuilder {

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFileProvider;

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer simpleContainerSpec;

	@BuilderSource
	SimpleActionPageSpec simpleActionPageSpec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	String name;
	String path;
	String actionName;
	String responderName;
	String responderBeanName;

	// build

	@BuildMethod
	public
	void buildConsoleModule (
			Builder builder) {

		setDefaults ();

		buildFile ();
		buildResponder ();

	}

	void buildFile () {

		consoleModule.addFile (
			path,
			consoleFileProvider.get ()
				.getResponderName (responderName)
				.postActionName (actionName));

	}

	void buildResponder () {

		consoleModule.addResponder (
			responderName,
			consoleModule.beanResponder (
				responderBeanName));

	}

	void setDefaults () {

		name =
			simpleActionPageSpec.name ();

		path =
			simpleActionPageSpec.path ();

		actionName =
			ifNull (
				simpleActionPageSpec.actionName (),
				stringFormat (
					"%s%sAction",
					simpleContainerSpec.existingBeanNamePrefix (),
					capitalise (name)));

		responderName =
			ifNull (
				simpleActionPageSpec.responderName (),
				stringFormat (
					"%s%sResponder",
					simpleContainerSpec.newBeanNamePrefix (),
					capitalise (name)));

		responderBeanName =
			ifNull (
				simpleActionPageSpec.responderBeanName (),
				stringFormat (
					"%s%sResponder",
					simpleContainerSpec.existingBeanNamePrefix (),
					capitalise (name)));

	}

}
