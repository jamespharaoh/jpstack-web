package wbs.framework.component.manager;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.logging.TaskLogger;

public
interface ComponentManager
	extends Closeable {

	Map <String, Pair <Class <?>, Object>> allSingletonComponents (
			TaskLogger parentTaskLogger);

	<ComponentType>
	Optional <Provider <ComponentType>> getComponentProvider (
			TaskLogger taskLogger,
			String componentName,
			Class <ComponentType> componentClass);

	default <ComponentType>
	Optional <ComponentType> getComponent (
			@NonNull TaskLogger taskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass) {

		Optional <Provider <ComponentType>> componentProvider =
			getComponentProvider (
				taskLogger,
				componentName,
				componentClass);

		if (
			optionalIsNotPresent (
				componentProvider)
		) {
			return optionalAbsent ();
		}

		return optionalOf (
			componentProvider.get ().get ());

	}

	default <ComponentType>
	ComponentType getComponentRequired (
			@NonNull TaskLogger taskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass) {

		Optional <Provider <ComponentType>> componentProvider =
			getComponentProvider (
				taskLogger,
				componentName,
				componentClass);

		if (
			optionalIsNotPresent (
				componentProvider)
		) {

			throw new NoSuchElementException (
				stringFormat (
					"No such component %s of type %s",
					componentName,
					classNameSimple (
						componentClass)));

		}

		return componentProvider.get ().get ();

	}

	default <ComponentType>
	ComponentType getComponentOrElse (
			@NonNull TaskLogger taskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass,
			@NonNull Supplier <ComponentType> orElse) {

		Optional <Provider <ComponentType>> componentProvider =
			getComponentProvider (
				taskLogger,
				componentName,
				componentClass);

		if (
			optionalIsNotPresent (
				componentProvider)
		) {
			return orElse.get ();
		}

		return componentProvider.get ().get ();

	}

	default <ComponentType>
	Provider <ComponentType> getComponentProviderRequired (
			@NonNull TaskLogger taskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass) {

		Optional <Provider <ComponentType>> componentProvider =
			getComponentProvider (
				taskLogger,
				componentName,
				componentClass);

		if (
			optionalIsNotPresent (
				componentProvider)
		) {
			throw new NoSuchElementException ();
		}

		return componentProvider.get ();

	}

	List <String> requestComponentNames ();

	ComponentMetaData componentMetaData (
			Object component);

	void bootstrapComponent (
			Object component);

	void bootstrapComponent (
			Object component,
			String componentName);

	@Override
	void close ();

}
