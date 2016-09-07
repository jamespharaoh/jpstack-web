package wbs.console.supervisor;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.experimental.Accessors;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.part.PagePart;
import wbs.console.part.TextPart;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorTableSeparatorBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorTableSeparatorBuilder {

	// prototype dependencies

	@PrototypeDependency
	Provider <TextPart> textPartProvider;

	// builder

	@BuilderParent
	SupervisorConfigSpec container;

	@BuilderSource
	SupervisorTableSeparatorSpec spec;

	@BuilderTarget
	SupervisorTablePartBuilder supervisorTablePartBuilder;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		Provider<PagePart> pagePartFactory =
			new Provider<PagePart> () {

			@Override
			public
			PagePart get () {

				return textPartProvider.get ()

					.text (
						stringFormat (
							"<tr class=\"sep\"></tr>\n"));


			}

		};

		supervisorTablePartBuilder.pagePartFactories ().add (
			pagePartFactory);

	}

}
