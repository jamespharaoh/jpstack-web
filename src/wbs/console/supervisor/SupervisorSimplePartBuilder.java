package wbs.console.supervisor;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.experimental.Accessors;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.part.PagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorSimplePartBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorSimplePartBuilder {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	// builder

	@BuilderParent
	SupervisorConfigSpec container;

	@BuilderSource
	SupervisorSimplePartSpec spec;

	@BuilderTarget
	SupervisorConfigBuilder supervisorConfigBuilder;

	// implementation

	@BuildMethod
	public
	void build (
			Builder builder) {

		String beanName =
			spec.beanName ();

		Provider<PagePart> pagePartFactory =
			applicationContext.getBeanProvider (
				beanName,
				PagePart.class);

		supervisorConfigBuilder.pagePartFactories () .add  (
			pagePartFactory);

	}


}
