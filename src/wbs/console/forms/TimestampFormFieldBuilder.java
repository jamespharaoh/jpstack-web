package wbs.console.forms;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

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

import wbs.utils.etc.PropertyUtils;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("timestampFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class TimestampFormFieldBuilder {

	// singleton dependencies

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

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
	Provider <SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <TextFormFieldRenderer>
	textFormFieldRendererProvider;

	@PrototypeDependency
	Provider <TimestampFormFieldInterfaceMapping>
	timestampFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	TimestampFormFieldSpec spec;

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

		TimestampFormFieldSpec.Format format =
			ifNull (
				spec.format (),
				TimestampFormFieldSpec.Format.timestamp);

		// accessor

		Class<?> propertyClass =
			PropertyUtils.propertyClassForClass (
				context.containerClass (),
				fieldName);

		FormFieldAccessor accessor;

		if (
			isNotNull (
				spec.fieldName ())
		) {

			accessor =
				dereferenceFormFieldAccessorProvider.get ()

				.path (
					fieldName);

		} else {

			accessor =
				simpleFormFieldAccessorProvider.get ()

				.name (
					fieldName)

				.nativeClass (
					propertyClass);

		}

		// native mapping

		FormFieldNativeMapping nativeMapping;

		if (propertyClass == Instant.class) {

			nativeMapping =
				identityFormFieldNativeMappingProvider.get ();

		} else if (propertyClass == Date.class) {

			nativeMapping =
				dateFormFieldNativeMappingProvider.get ();

		} else {

			throw new RuntimeException (
				stringFormat (
					"Don't know how to map %s as timestamp for %s.%s",
					classNameSimple (
						propertyClass),
					classNameSimple (
						context.containerClass ()),
					name));

		}

		// value validator

		List <FormFieldValueValidator> valueValidators =
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
			timestampFormFieldInterfaceMappingProvider.get ()

			.name (
				name)

			.format (
				format);

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
					accessor)

				.nativeMapping (
					nativeMapping)

				.interfaceMapping (
					interfaceMapping)

				.csvMapping (
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
					accessor)

				.nativeMapping (
					nativeMapping)

				.valueValidators (
					valueValidators)

				.constraintValidator (
					constraintValidator)

				.interfaceMapping (
					interfaceMapping)

				.csvMapping (
					interfaceMapping)

				.renderer (
					renderer)

				.updateHook (
					updateHook)

			);

		}

	}

}
