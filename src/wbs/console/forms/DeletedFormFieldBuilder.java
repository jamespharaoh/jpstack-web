package wbs.console.forms;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.ConsoleHelper;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("deletedFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class DeletedFormFieldBuilder {

	// singleton dependencies

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <UpdatableFormField>
	updatableFormFieldProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <YesNoCsvFormFieldInterfaceMapping>
	yesNoCsvFormFieldInterfaceMappingProvider;

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
	Provider <YesNoFormFieldRenderer>
	yesNoFormFieldRendererProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	DeletedFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		ConsoleHelper consoleHelper =
			context.consoleHelper ();

		String name =
			ifNull (
				spec.name (),
				consoleHelper.deletedFieldName ());

		String label =
			ifNull (
				spec.label (),
				capitalise (
					consoleHelper.deletedLabel ()));

		Boolean readOnly =
			ifNull (
				spec.readOnly (),
				false);

		String yesLabel =
			ifNull (
				spec.yesLabel (),
				"yes");

		String noLabel =
			ifNull (
				spec.noLabel (),
				"no");

		// accessor

		FormFieldAccessor accessor =
			simpleFormFieldAccessorProvider.get ()

			.name (
				name)

			.nativeClass (
				Boolean.class);

		// native mapping

		FormFieldNativeMapping nativeMapping =
			identityFormFieldNativeMappingProvider.get ();

		// value validators

		List<FormFieldValueValidator> valueValidators =
			new ArrayList<> ();

		valueValidators.add (
			requiredFormFieldValueValidatorProvider.get ());

		// constraint validator

		FormFieldConstraintValidator constraintValidator =
			nullFormFieldValueConstraintValidatorProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			identityFormFieldInterfaceMappingProvider.get ();

		// csv mapping

		FormFieldInterfaceMapping csvMapping =
			yesNoCsvFormFieldInterfaceMappingProvider.get ()

			.nullable (
				false);

		// render

		FormFieldRenderer renderer =
			yesNoFormFieldRendererProvider.get ()

			.name (
				name)

			.label (
				label)

			.nullable (
				false)

			.yesLabel (
				yesLabel)

			.noLabel (
				noLabel);

		// update hook

		FormFieldUpdateHook updateHook =
			formFieldPluginManager.getUpdateHook (
				context,
				context.containerClass (),
				name);

		// field

		if (! readOnly) {

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
					csvMapping)

				.renderer (
					renderer)

				.updateHook (
					updateHook)

			);

		} else {

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
					csvMapping)

				.renderer (
					renderer)

			);

		}

	}

}
