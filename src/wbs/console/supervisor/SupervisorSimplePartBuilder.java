package wbs.console.supervisor;

import javax.inject.Provider;

import lombok.experimental.Accessors;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.part.PagePart;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorSimplePartBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorSimplePartBuilder {

	// singleton dependencies

	@SingletonDependency
	ComponentManager applicationContext;

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
			applicationContext.getComponentProviderRequired (
				beanName,
				PagePart.class);

		supervisorConfigBuilder.pagePartFactories () .add  (
			pagePartFactory);

	}


}
