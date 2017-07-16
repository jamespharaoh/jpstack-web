package shn.shopify.logic;

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

import shn.shopify.apiclient.ShopifyApiResponse;

@SingletonComponent ("shnShopifySynchronisationComponentWrapper")
public
class ShnShopifySynchronisationComponentWrapper <
	Local extends Record <Local>,
	Remote extends ShopifyApiResponse
>
	implements ComponentWrapper <
		ShnShopifySynchronisationHelper <Local, Remote>
	> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// details

	@Override
	public
	Class <ShnShopifySynchronisationHelper <Local, Remote>> componentClass () {

		return genericCastUnchecked (
			ShnShopifySynchronisationHelper.class);

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
					"SHN shopify synchronisation helper component %s ",
					componentDefinition.name (),
					"has non-prototype scope: %s",
					componentDefinition.scope ());

				return;

			}

			Optional <String> baseNameOptional =
				stringExtract (
					"",
					"SynchronisationHelper",
					componentDefinition.name ());

			if (
				optionalIsNotPresent (
					baseNameOptional)
			) {

				taskLogger.errorFormat (
					"SHN shopify synchronisation helper component has invalid ",
					"name: %s",
					componentDefinition.name ());

				return;

			}

			String baseName =
				optionalGetRequired (
					baseNameOptional);

			componentRegistry.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.nameFormat (
					"%sSynchronisation",
					baseName)

				.scope (
					"prototype")

				.componentClass (
					ShnShopifySynchronisationWrapper.class)

				.addReferenceProperty (
					"helperProvider",
					"prototype",
					componentDefinition.name ())

			);

		}

	}

}
