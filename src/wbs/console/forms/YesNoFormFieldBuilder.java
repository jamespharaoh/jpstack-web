package wbs.console.forms;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("yesNoFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class YesNoFormFieldBuilder {

	// singleton dependencies

	@SingletonDependency
	FormFieldPluginManager formFieldPluginManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <HiddenFormField> hiddenFormFieldProvider;

	@PrototypeDependency
	Provider <UpdatableFormField>
	updatableFormFieldProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

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

	@PrototypeDependency
	Provider <YesNoCsvFormFieldInterfaceMapping>
	yesNoCsvFormFieldInterfaceMappingProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	YesNoFormFieldSpec spec;

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

		Boolean hidden =
			ifNull (
				spec.hidden (),
				false);

		String yesLabel =
			ifNull (
				spec.yesLabel (),
				"yes");

		String noLabel =
			ifNull (
				spec.noLabel (),
				"no");

		/*
		Boolean dynamic =
			ifNull (
				spec.dynamic (),
				false);
		*/

		// field components

		FormFieldAccessor accessor =
			simpleFormFieldAccessorProvider.get ()

			.name (
				name)

			.nativeClass (
				Boolean.class);

		// TODO dynamic

		// native mapping

		FormFieldNativeMapping nativeMapping =
			identityFormFieldNativeMappingProvider.get ();

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
			identityFormFieldInterfaceMappingProvider.get ();

		// renderer

		FormFieldRenderer renderer =
			yesNoFormFieldRendererProvider.get ()

			.name (
				name)

			.label (
				label)

			.nullable (
				nullable)

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

		// csv mapping

		FormFieldInterfaceMapping csvMapping =
			yesNoCsvFormFieldInterfaceMappingProvider.get ()

			.nullable (
				nullable);

		// field

		if (hidden) {

			formFieldSet.addFormField (
				hiddenFormFieldProvider.get ()

				.name (
					name)

				.accessor (
					accessor)

				.nativeMapping (
					nativeMapping)

				.csvMapping (
					csvMapping)

				.implicitValue (
					Optional.absent ())

			);

		} else if (! readOnly) {

			formFieldSet.addFormField (
				updatableFormFieldProvider.get ()

				.name (
					name)

				.label (
					label)

				.viewPriv (
					spec.viewPriv ())

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

			formFieldSet.addFormField (
				readOnlyFormFieldProvider.get ()

				.name (
					name)

				.label (
					label)

				.viewPriv (
					spec.viewPriv ())

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
