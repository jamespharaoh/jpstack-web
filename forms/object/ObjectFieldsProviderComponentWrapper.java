package wbs.console.forms.object;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringExtract;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistryBuilder;
import wbs.framework.component.tools.ComponentWrapper;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("objectFieldsProviderComponentWrapper")
public
class ObjectFieldsProviderComponentWrapper <
	Container extends Record <Container>,
	Parent extends Record <Parent>
>
	implements ComponentWrapper <ObjectFieldsProvider <Container, Parent>> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// details

	@Override
	public
	Class <ObjectFieldsProvider <Container, Parent>> componentClass () {

		return genericCastUnchecked (
			ObjectFieldsProvider.class);

	}

	// public implementation

	@Override
	public
	void wrapComponent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull ComponentDefinition componentDefinition) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"wrapComponent");

		) {

			// sanity check

			if (
				stringNotEqualSafe (
					componentDefinition.scope (),
					"prototype")
			) {

				taskLogger.errorFormat (
					"Api logging action component %s ",
					componentDefinition.name (),
					"has non-prototype scope: %s",
					componentDefinition.scope ());

				return;

			}

			// extract base name

			Optional <String> baseNameOptional =
				stringExtract (
					"",
					"ObjectFieldsProvider",
					componentDefinition.name ());

			if (
				optionalIsNotPresent (
					baseNameOptional)
			) {

				taskLogger.errorFormat (
					"Object fields provider component has invalid name: %s",
					componentDefinition.name ());

				return;

			}

			String baseName =
				optionalGetRequired (
					baseNameOptional);

			// register components

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.nameFormat (
					"%sListFieldsProvider",
					baseName)

				.scope (
					"prototype")

				.componentClass (
					ObjectListFieldsProviderWrapper.class)

				.addReferenceProperty (
					"objectFieldsProviderProvider",
					"prototype",
					componentDefinition.name ())

			);

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.nameFormat (
					"%sCreateFieldsProvider",
					baseName)

				.scope (
					"prototype")

				.componentClass (
					ObjectCreateFieldsProviderWrapper.class)

				.addReferenceProperty (
					"objectFieldsProviderProvider",
					"prototype",
					componentDefinition.name ())

			);

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.nameFormat (
					"%sSummaryFieldsProvider",
					baseName)

				.scope (
					"prototype")

				.componentClass (
					ObjectSummaryFieldsProviderWrapper.class)

				.addReferenceProperty (
					"objectFieldsProviderProvider",
					"prototype",
					componentDefinition.name ())

			);

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.nameFormat (
					"%sSettingsFieldsProvider",
					baseName)

				.scope (
					"prototype")

				.componentClass (
					ObjectSettingsFieldsProviderWrapper.class)

				.addReferenceProperty (
					"objectFieldsProviderProvider",
					"prototype",
					componentDefinition.name ())

			);

		}

	}

}
