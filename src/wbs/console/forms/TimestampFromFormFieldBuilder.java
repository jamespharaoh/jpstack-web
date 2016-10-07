package wbs.console.forms;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Provider;

import org.joda.time.Instant;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("timestampFromFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class TimestampFromFormFieldBuilder {

	// singleton dependencies

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <DateFormFieldNativeMapping>
	dateFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <DereferenceFormFieldAccessor>
	dereferenceFormFieldAccessorProvider;

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
	Provider <TextFormFieldRenderer>
	textFormFieldRendererProvider;

	@PrototypeDependency
	Provider <TimestampFromFormFieldInterfaceMapping>
	timestampFromFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	TimestampFromFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			spec.name ();

		String fieldName =
			ifNull (
				spec.fieldName (),
				name);

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

		Class <?> propertyClass =
			optionalGetRequired (
				objectManager.dereferenceType (
					optionalOf (
						context.containerClass ()),
					optionalOf (
						fieldName)));

		FormFieldAccessor formFieldAccessor;
		FormFieldNativeMapping formFieldNativeMapping;

		if (propertyClass == Instant.class) {

			formFieldAccessor =
				dereferenceFormFieldAccessorProvider.get ()

				.path (
					fieldName)

				.nativeClass (
					Instant.class);

			formFieldNativeMapping =
				identityFormFieldNativeMappingProvider.get ();

		} else if (propertyClass == Date.class) {

			formFieldAccessor =
				dereferenceFormFieldAccessorProvider.get ()

				.path (
					fieldName)

				.nativeClass (
					Date.class);

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

		// value validator

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
			timestampFromFormFieldInterfaceMappingProvider.get ()

			.name (
				name);

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

			formFieldSet.addFormItem (
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

			formFieldSet.addFormItem (
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
