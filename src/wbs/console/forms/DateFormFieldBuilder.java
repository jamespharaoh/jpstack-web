package wbs.console.forms;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.capitalise;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import org.joda.time.LocalDate;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("dateFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class DateFormFieldBuilder {

	// singleton dependencies

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

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
	Provider <DateFormFieldInterfaceMapping>
	dateFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	DateFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet target;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			spec.name ();

		String nativeFieldName =
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

		// accessor

		FormFieldAccessor accessor;

		if (readOnly) {

			accessor =
				dereferenceFormFieldAccessorProvider.get ()

				.path (
					nativeFieldName);

		} else {

			accessor =
				simpleFormFieldAccessorProvider.get ()

				.name (
					nativeFieldName)

				.nativeClass (
					LocalDate.class);

		}

		// native mapping

		FormFieldNativeMapping nativeMapping =
			identityFormFieldNativeMappingProvider.get ();

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
			dateFormFieldInterfaceMappingProvider.get ();

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

			target.addFormField (

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

			target.addFormField (

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
