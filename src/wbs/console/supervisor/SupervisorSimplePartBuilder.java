package wbs.console.supervisor;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.part.PagePart;
import wbs.console.part.PagePartFactory;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorSimplePartBuilder")
public
class SupervisorSimplePartBuilder
	implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@ClassSingletonDependency
	LogContext logContext;

	// builder

	@BuilderParent
	SupervisorBuilderContext context;

	@BuilderSource
	SupervisorSimplePartSpec spec;

	@BuilderTarget
	SupervisorConfigBuilder supervisorConfigBuilder;

	// implementation

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			String beanName =
				spec.beanName ();

			PagePartFactory pagePartFactory =
				nextTaskLogger ->
					componentManager.getComponentRequired (
						nextTaskLogger,
						beanName,
						PagePart.class);

			supervisorConfigBuilder.pagePartFactories ().add  (
				pagePartFactory);

		}

	}

}
