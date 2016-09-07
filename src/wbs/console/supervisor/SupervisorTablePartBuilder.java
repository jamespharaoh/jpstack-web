package wbs.console.supervisor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.part.PagePart;
import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorTablePartBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorTablePartBuilder {

	// prototype dependencies

	@PrototypeDependency
	Provider <SupervisorTablePart> supervisorTablePartProvider;

	// builder

	@BuilderParent
	SupervisorConfigSpec container;

	@BuilderSource
	SupervisorTablePartSpec spec;

	@BuilderTarget
	SupervisorConfigBuilder supervisorConfigBuilder;

	// state

	@Getter @Setter
	List<Provider<PagePart>> pagePartFactories =
		new ArrayList<Provider<PagePart>> ();

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

				return supervisorTablePartProvider.get ()

					.supervisorTablePartBuilder (
						SupervisorTablePartBuilder.this);

			}

		};

		supervisorConfigBuilder.pagePartFactories ().add (
			pagePartFactory);

		builder.descend (
			spec,
			spec.builders (),
			this,
			MissingBuilderBehaviour.ignore);

	}

}
