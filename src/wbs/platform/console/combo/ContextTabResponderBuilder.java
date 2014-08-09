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
import wbs.platform.console.context.ConsoleContextBuilderContainer;
import wbs.platform.console.module.ConsoleModuleImpl;
import wbs.platform.console.tab.TabContextResponder;

@PrototypeComponent ("contextTabResponderBuilder")
@ConsoleModuleBuilderHandler
public
class ContextTabResponderBuilder {

	// prototype dependencies

	@Inject
	Provider<TabContextResponder> tabContextResponder;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderSource
	ContextTabResponderSpec spec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// state

	String name;
	String tabName;
	String title;
	String responderName;
	String pagePartName;

	// build

	@BuildMethod
	public
	void buildConsoleModule (
			Builder builder) {

		setDefaults ();

		buildResponder ();

	}

	void buildResponder () {

		consoleModule.addResponder (
			responderName,
			tabContextResponder.get ()
				.tab (tabName)
				.title (title)
				.pagePartName (pagePartName));

	}

	// set defaults

	void setDefaults () {

		name =
			spec.name ();

		tabName =
			spec.tabName ();

		title =
			ifNull (
				spec.title (),
				capitalise (name));

		responderName =
			ifNull (
				spec.responderName (),
				stringFormat (
					"%s%sResponder",
					container.newBeanNamePrefix (),
					capitalise (name)));


		pagePartName =
			ifNull (
				spec.pagePartName (),
				stringFormat (
					"%s%sPart",
					container.existingBeanNamePrefix (),
					capitalise (name)));

	}

}
