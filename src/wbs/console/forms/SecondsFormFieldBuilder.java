package wbs.console.forms;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.capitalise;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

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
@PrototypeComponent ("secondsFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class SecondsFormFieldBuilder {

	// singleton dependencies

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <NullFormFieldConstraintValidator>
	nullFormFieldConstraintValidatorProvider;

	@PrototypeDependency
	Provider <ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@PrototypeDependency
	Provider <RequiredFormFieldValueValidator>
	requiredFormFieldValueValidatorProvider;

	@PrototypeDependency
	Provider <SecondsFormFieldInterfaceMapping>
	secondsFormFieldInterfaceMapping;

	@PrototypeDependency
	Provider <SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <TextFormFieldRenderer>
	textFormFieldRendererProvider;

	@PrototypeDependency
	Provider <UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	SecondsFormFieldSpec spec;

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

		Boolean nullable =
			ifNull (
				spec.nullable (),
				false);

		Boolean readOnly =
			ifNull (
				spec.readOnly (),
				false);

		SecondsFormFieldSpec.Format format =
			ifNull (
				spec.format (),
				SecondsFormFieldSpec.Format.textual);

		Class<?> propertyClass =
			BeanLogic.propertyClassForClass (
				context.containerClass (),
				name);

		// accessor

		FormFieldAccessor accessor =
			simpleFormFieldAccessorProvider.get ()

			.name (
				name)

			.nativeClass (
				propertyClass);

		// native mapping

		FormFieldNativeMapping nativeMapping =
			identityFormFieldNativeMappingProvider.get ();

		// value validators

		List <FormFieldValueValidator> valueValidators =
			new ArrayList<> ();

		if (! nullable) {

			valueValidators.add (
				requiredFormFieldValueValidatorProvider.get ());

		}

		// constraint validator

		FormFieldConstraintValidator constraintValidator =
			nullFormFieldConstraintValidatorProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			secondsFormFieldInterfaceMapping.get ()

			.label (
				label)

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
				nullable)

			.listAlign (
				FormField.Align.right);

		// update hook

		FormFieldUpdateHook updateHook =
			formFieldPluginManager.getUpdateHook (
				context,
				context.containerClass (),
				name);

		// field

		if (! readOnly) {

			formFieldSet.addFormField (
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

				.renderer (
					renderer)

				.updateHook (
					updateHook)

			);

		} else {

			formFieldSet.addFormField (
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

				.renderer (
					renderer)

			);

		}

	}

}
