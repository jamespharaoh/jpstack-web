package wbs.console.supervisor;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.part.PagePart;
import wbs.console.part.TextPart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("supervisorHeadingPartBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorHeadingPartBuilder {

	// prototype dependencies

	@PrototypeDependency
	Provider <TextPart> textPartProvider;

	// builder

	@BuilderParent
	SupervisorConfigSpec supervisorConfigSpec;

	@BuilderSource
	SupervisorHeadingPartSpec supervisorHeadingPartSpec;

	@BuilderTarget
	SupervisorConfigBuilder supervisorConfigBuilder;

	// state

	String label;
	String text;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		label =
			supervisorHeadingPartSpec.label ();

		text =
			stringFormat (
				"<h2>%h</h2>\n",
				label);

		Provider<PagePart> pagePartFactory =
			new Provider<PagePart> () {

			@Override
			public
			PagePart get () {

				return textPartProvider.get ()
					.text (text);

			}

		};

		supervisorConfigBuilder.pagePartFactories ().add (
			pagePartFactory);

	}

}
