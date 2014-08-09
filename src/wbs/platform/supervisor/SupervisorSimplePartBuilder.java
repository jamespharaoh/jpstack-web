package wbs.platform.supervisor;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.part.PagePart;

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
	SupervisorPageSpec supervisorPageSpec;

	@BuilderSource
	SupervisorSimplePartSpec supervisorSimplePartSpec;

	@BuilderTarget
	SupervisorPageBuilder supervisorPageBuilder;

	// implementation

	@BuildMethod
	public
	void build (
			Builder builder) {

		String beanName =
			supervisorSimplePartSpec.beanName ();

		Provider<PagePart> pagePartFactory =
			applicationContext.getBeanProvider (
				beanName,
				PagePart.class);

		supervisorPageBuilder.pagePartFactories () .add  (
			pagePartFactory);

	}


}
