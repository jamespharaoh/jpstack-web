package wbs.platform.supervisor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.part.PagePart;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorTablePartBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorTablePartBuilder {

	@Inject
	Provider<SupervisorTablePart> supervisorTablePart;

	// builder

	@BuilderParent
	SupervisorPageSpec supervisorPageSpec;

	@BuilderSource
	SupervisorTablePartSpec supervisorTablePartSpec;

	@BuilderTarget
	SupervisorPageBuilder supervisorPageBuilder;

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

				return supervisorTablePart.get ()
					.supervisorTablePartBuilder (
						SupervisorTablePartBuilder.this);

			}

		};

		supervisorPageBuilder.pagePartFactories ()
			.add (pagePartFactory);

		builder.descend (
			supervisorTablePartSpec,
			supervisorTablePartSpec.builders (),
			this);

	}

}
