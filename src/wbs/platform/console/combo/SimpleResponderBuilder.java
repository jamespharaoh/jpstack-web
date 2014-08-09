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
import wbs.platform.console.module.ConsoleModuleImpl;
import wbs.platform.console.responder.ConsoleFile;
import wbs.platform.console.spec.ConsoleSimpleBuilderContainer;

@PrototypeComponent ("simpleResponderBuilder")
@ConsoleModuleBuilderHandler
public
class SimpleResponderBuilder {

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFile;

	// builder

	@BuilderParent
	ConsoleSimpleBuilderContainer simpleContainerSpec;

	@BuilderSource
	SimpleResponderSpec simpleResponderSpec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// state

	String name;
	String responderName;
	String responderBeanName;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

		buildResponder ();

	}

	void buildResponder () {

		consoleModule.addResponder (
			responderName,
			consoleModule.beanResponder (
				responderBeanName));

	}

	// defaults

	void setDefaults () {

		name =
			simpleResponderSpec.name ();

		responderName =
			ifNull (
				simpleResponderSpec.responderName (),
				stringFormat (
					"%s%sResponder",
					simpleContainerSpec.newBeanNamePrefix (),
					capitalise (name)));

		responderBeanName =
			ifNull (
				simpleResponderSpec.responderBeanName (),
				stringFormat (
					"%s%sResponder",
					simpleContainerSpec.newBeanNamePrefix (),
					capitalise (name)));

	}

}
