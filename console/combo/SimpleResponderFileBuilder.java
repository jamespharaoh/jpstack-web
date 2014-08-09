package wbs.platform.console.combo;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.module.ConsoleModuleImpl;
import wbs.platform.console.responder.ConsoleFile;
import wbs.platform.console.spec.ConsoleSimpleBuilderContainer;

@PrototypeComponent ("simpleResponderFileBuilder")
@ConsoleModuleBuilderHandler
public
class SimpleResponderFileBuilder {

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFileProvider;

	// builder

	@BuilderParent
	ConsoleSimpleBuilderContainer simpleContainerSpec;

	@BuilderSource
	SimpleResponderFileSpec simpleResponderFileSpec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// state

	ConsoleHelper<?> consoleHelper;
	String path;
	String name;
	String responderName;
	String responderBeanName;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

		buildFile ();
		buildResponder ();

	}

	void buildFile () {

		consoleModule.addFile (
			path,
			consoleFileProvider.get ()
				.getResponderName (responderName));

	}

	void buildResponder () {

		consoleModule.addResponder (
			responderName,
			consoleModule.beanResponder (
				responderBeanName));

	}

	// defaults

	void setDefaults () {

		path =
			simpleResponderFileSpec.path ();

		name =
			simpleResponderFileSpec.name ();

		responderName =
			ifNull (
				simpleResponderFileSpec.responderName (),
				stringFormat (
					"%s%sResponder",
					simpleContainerSpec.newBeanNamePrefix (),
					capitalise (name)));

		responderBeanName =
			ifNull (
				simpleResponderFileSpec.responderBeanName (),
				stringFormat (
					"%s%sResponder",
					simpleContainerSpec.newBeanNamePrefix (),
					capitalise (name)));

	}

}
