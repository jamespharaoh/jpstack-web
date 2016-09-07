package wbs.console.supervisor;

import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.part.PagePart;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;

@PrototypeComponent ("supervisorTableHeadingBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorTableHeadingBuilder {

	// prototype dependencies

	@PrototypeDependency
	Provider <SupervisorTableHeadingPart> supervisorTableHeadingPartProvider;

	// builder

	@BuilderParent
	SupervisorTablePartSpec supervisorTablePartSpec;

	@BuilderSource
	SupervisorTableHeadingSpec supervisorTableHeadingSpec;

	@BuilderTarget
	SupervisorTablePartBuilder supervisorTablePartBuilder;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		Provider <PagePart> pagePartFactory =
			new Provider <PagePart> () {

			@Override
			public
			PagePart get () {

				return supervisorTableHeadingPartProvider.get ()

					.supervisorTableHeadingSpec (
						supervisorTableHeadingSpec);

			}

		};

		supervisorTablePartBuilder.pagePartFactories ()
			.add (pagePartFactory);

	}

}
