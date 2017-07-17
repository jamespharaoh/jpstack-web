package wbs.framework.component.manager;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

public
class ComponentProviderImplementation <Component>
	implements ComponentProvider <Component> {

	// state

	private final
	Function <TaskLogger, Component> instantiateComponent;

	private final
	BiConsumer <TaskLogger, Component> initialiseCompoment;

	// constructor

	public
	ComponentProviderImplementation (
			@NonNull Function <TaskLogger, Component> instantiateComponent,
			@NonNull BiConsumer <TaskLogger, Component> initialiseCompoment) {

		this.instantiateComponent =
			instantiateComponent;

		this.initialiseCompoment =
			initialiseCompoment;

	}

	// public implementation

	@Override
	public
	Component provideUninitialised (
			@NonNull TaskLogger parentTaskLogger) {

		return instantiateComponent.apply (
			parentTaskLogger);

	}

	@Override
	public
	Component provide (
			@NonNull TaskLogger parentTaskLogger) {

		Component component =
			instantiateComponent.apply (
				parentTaskLogger);

		initialiseCompoment.accept (
			parentTaskLogger,
			component);

		return component;

	}

	@Override
	public
	Component provide (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Consumer <? super Component> propertyIniitialiser) {

		Component component =
			instantiateComponent.apply (
				parentTaskLogger);

		propertyIniitialiser.accept (
			component);

		initialiseCompoment.accept (
			parentTaskLogger,
			component);

		return component;

	}

	@Override
	public
	void initialise (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Component component) {

		initialiseCompoment.accept (
			parentTaskLogger,
			component);

	}

}
