package wbs.framework.component.manager;

import java.io.Closeable;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Provider;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;

import wbs.framework.logging.Log4jLogTarget;
import wbs.framework.logging.TaskLogger;

public
interface ComponentManager
	extends Closeable {

	<ComponentType>
	Optional <ComponentType> getComponent (
			TaskLogger taskLogger,
			String componentName,
			Class <ComponentType> componentClass);

	default <ComponentType>
	Optional <ComponentType> getComponent (
			Logger logger,
			String componentName,
			Class <ComponentType> componentClass) {

		return Log4jLogTarget.wrap (
			logger,
			taskLogger ->
				getComponent (
					taskLogger,
					componentName,
					componentClass));

	}

	<ComponentType>
	ComponentType getComponentRequired (
			TaskLogger taskLogger,
			String componentName,
			Class <ComponentType> componentClass);

	default <ComponentType>
	ComponentType getComponentRequired (
			Logger logger,
			String componentName,
			Class <ComponentType> componentClass) {

		return Log4jLogTarget.wrap (
			logger,
			taskLogger ->
				getComponentRequired (
					taskLogger,
					componentName,
					componentClass));

	}

	<ComponentType>
	ComponentType getComponentOrElse (
			TaskLogger taskLogger,
			String componentName,
			Class <ComponentType> componentClass,
			Supplier <ComponentType> orElse);

	default <ComponentType>
	ComponentType getComponentOrElse (
			Logger logger,
			String componentName,
			Class <ComponentType> componentClass,
			Supplier <ComponentType> orElse) {

		return Log4jLogTarget.wrap (
			logger,
			taskLogger ->
				getComponentOrElse (
					taskLogger,
					componentName,
					componentClass,
					orElse));

	}

	<ComponentType>
	Provider <ComponentType> getComponentProviderRequired (
			TaskLogger taskLogger,
			String componentName,
			Class <ComponentType> componentClass);

	default <ComponentType>
	Provider <ComponentType> getComponentProviderRequired (
			Logger logger,
			String componentName,
			Class <ComponentType> componentClass) {

		return Log4jLogTarget.wrap (
			logger,
			taskLogger ->
				getComponentProviderRequired (
					taskLogger,
					componentName,
					componentClass));

	}

	List <String> requestComponentNames ();

	@Override
	void close ();

}
