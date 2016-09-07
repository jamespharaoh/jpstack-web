package wbs.console.forms;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Provider;

import org.joda.time.Instant;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.utils.etc.BeanLogic;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("timestampToFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class TimestampToFormFieldBuilder {

	// singleton dependencies

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototsype dependencies

	@PrototypeDependency
	Provider <DateFormFieldNativeMapping>
	dateFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <NullFormFieldConstraintValidator>
	nullFormFieldValueConstraintValidatorProvider;

	@PrototypeDependency
	Provider <ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@PrototypeDependency
	Provider <RequiredFormFieldValueValidator>
	requiredFormFieldValueValidatorProvider;

	@PrototypeDependency
	Provider <SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <TextFormFieldRenderer>
	textFormFieldRendererProvider;

	@PrototypeDependency
	Provider <TimestampToFormFieldInterfaceMapping>
	timestampToFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	TimestampToFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			spec.name ();

		String label =
			ifNull (
				spec.label (),
				capitalise (
					camelToSpaces (
						name)));

		Boolean readOnly =
			ifNull (
				spec.readOnly (),
				false);

		Boolean nullable =
			ifNull (
				spec.nullable (),
				false);

		// accessor and native mapping

		Class<?> propertyClass =
			BeanLogic.propertyClassForClass (
				context.containerClass (),
				name);

		FormFieldAccessor formFieldAccessor;
		FormFieldNativeMapping formFieldNativeMapping;

		if (propertyClass == Instant.class) {

			formFieldAccessor =
				simpleFormFieldAccessorProvider.get ()
					.name (name)
					.nativeClass (Instant.class);

			formFieldNativeMapping =
				identityFormFieldNativeMappingProvider.get ();

		} else if (propertyClass == Date.class) {

			formFieldAccessor =
				simpleFormFieldAccessorProvider.get ()
					.name (name)
					.nativeClass (Date.class);

			formFieldNativeMapping =
				dateFormFieldNativeMappingProvider.get ();

		} else {

			throw new RuntimeException (
				stringFormat (
					"Don't know how to map %s as timestamp for %s.%s",
					propertyClass,
					context.containerClass (),
					name));

		}

		// value validators

		List<FormFieldValueValidator> valueValidators =
			new ArrayList<> ();

		if (! nullable) {

			valueValidators.add (
				requiredFormFieldValueValidatorProvider.get ());

		}

		// constraint validator

		FormFieldConstraintValidator constraintValidator =
			nullFormFieldValueConstraintValidatorProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			timestampToFormFieldInterfaceMappingProvider.get ()
				.name (name);

		// renderer

		FormFieldRenderer renderer =
			textFormFieldRendererProvider.get ()

			.name (
				name)

			.label (
				label)

			.nullable (
				nullable);

		// update hook

		FormFieldUpdateHook updateHook =
			formFieldPluginManager.getUpdateHook (
				context,
				context.containerClass (),
				name);

		// form field

		if (readOnly) {

			formFieldSet.addFormField (

				readOnlyFormFieldProvider.get ()

				.name (
					name)

				.label (
					label)

				.accessor (
					formFieldAccessor)

				.nativeMapping (
					formFieldNativeMapping)

				.interfaceMapping (
					interfaceMapping)

				.renderer (
					renderer)

			);

		} else {

			formFieldSet.addFormField (

				updatableFormFieldProvider.get ()

				.name (
					name)

				.label (
					label)

				.accessor (
					formFieldAccessor)

				.nativeMapping (
					formFieldNativeMapping)

				.valueValidators (
					valueValidators)

				.constraintValidator (
					constraintValidator)

				.interfaceMapping (
					interfaceMapping)

				.renderer (
					renderer)

				.updateHook (
					updateHook)

			);

		}

	}

}
