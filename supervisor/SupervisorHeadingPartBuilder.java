package wbs.platform.supervisor;

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
import wbs.platform.console.part.PagePart;
import wbs.platform.text.console.TextPart;

@PrototypeComponent ("supervisorHeadingPartBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorHeadingPartBuilder {

	@Inject
	Provider<TextPart> textPart;

	// builder

	@BuilderParent
	SupervisorPageSpec supervisorPageSpec;

	@BuilderSource
	SupervisorHeadingPartSpec supervisorHeadingPartSpec;

	@BuilderTarget
	SupervisorPageBuilder supervisorPageBuilder;

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

				return textPart.get ()
					.text (text);

			}

		};

		supervisorPageBuilder.pagePartFactories ().add (
			pagePartFactory);

	}

}
