package wbs.platform.console.combo;

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

@PrototypeComponent ("simpleFileBuilder")
@ConsoleModuleBuilderHandler
public
class SimpleFileBuilder {

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFileProvider;

	// builder

	@BuilderParent
	ConsoleSimpleBuilderContainer simpleContainerSpec;

	@BuilderSource
	SimpleFileSpec simpleFileSpec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// state

	String path;
	String getResponderName;
	String getActionName;
	String postResponderName;
	String postActionName;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

		buildFile ();

	}

	void buildFile () {

		consoleModule.addFile (
			path,
			consoleFileProvider.get ()
				.getResponderName (getResponderName)
				.getActionName (getActionName)
				.postActionName (postActionName));

	}

	// defaults

	void setDefaults () {

		path =
			simpleFileSpec.path ();

		getResponderName =
			simpleFileSpec.getResponderName ();

		getActionName =
			simpleFileSpec.getActionName ();

		postResponderName =
			simpleFileSpec.postResponderName ();

		postActionName =
			simpleFileSpec.postActionName ();

	}

}
