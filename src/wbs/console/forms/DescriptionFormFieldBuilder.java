package wbs.console.forms;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleHelperRegistry;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.object.ObjectManager;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("descriptionFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class DescriptionFormFieldBuilder {

	// singleton dependencies

	@SingletonDependency
	ConsoleHelperRegistry consoleHelperRegistry;

	@SingletonDependency
	FormFieldPluginManager formFieldPluginManager;

	@SingletonDependency
	ObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <DelegateFormFieldAccessor>
	delegateFormFieldAccessorProvider;

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
	Provider <TextFormFieldRenderer>
	textFormFieldRendererProvider;

	@PrototypeDependency
	Provider <UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	DescriptionFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		ConsoleHelper thisConsoleHelper =
			context.consoleHelper ();

		ConsoleHelper thatConsoleHelper;

		if (
			isNotNull (
				spec.delegate ())
		) {

			Class thatClass =
				optionalGetRequired (
					objectManager.dereferenceType (
						Optional.<Class<?>>of (
							thisConsoleHelper.objectClass ()),
						Optional.of (
							spec.delegate ())));

			thatConsoleHelper =
				consoleHelperRegistry.findByObjectClass (
					thatClass);

		} else {

			thatConsoleHelper =
				thisConsoleHelper;

		}

		String name =
			ifNull (
				spec.name (),
				thatConsoleHelper.descriptionFieldName ());

		String fullName =
			spec.delegate () != null
				? stringFormat (
					"%s.%s",
					spec.delegate (),
					name)
				: name;

		String label =
			ifNull (
				spec.label (),
				capitalise (
					thatConsoleHelper.descriptionLabel ()));

		if (
			spec.delegate () != null
			&& spec.readOnly () != null
			&& spec.readOnly () == false
		) {
			throw new RuntimeException ();
		}

		Boolean readOnly =
			ifNull (
				spec.readOnly (),
				spec.delegate () != null
					? true
					: null,
				false);

		// accessor

		FormFieldAccessor accessor =
			simpleFormFieldAccessorProvider.get ()

			.name (
				name)

			.nativeClass (
				String.class);

		if (spec.delegate () != null) {

			accessor =
				delegateFormFieldAccessorProvider.get ()

				.path (
					spec.delegate ())

				.delegateFormFieldAccessor (
					accessor);

		}

		// native mapping

		FormFieldNativeMapping nativeMapping =
			identityFormFieldNativeMappingProvider.get ();

		// value validators

		List<FormFieldValueValidator> valueValidators =
			new ArrayList<> ();

		valueValidators.add (
			requiredFormFieldValueValidatorProvider.get ());

		// constraint validators

		FormFieldConstraintValidator constraintValidator =
			nullFormFieldValueConstraintValidatorProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			identityFormFieldInterfaceMappingProvider.get ();

		// update hook

		FormFieldUpdateHook updateHook =
			formFieldPluginManager.getUpdateHook (
				context,
				context.containerClass (),
				name);

		// renderer

		FormFieldRenderer renderer =
			textFormFieldRendererProvider.get ()

			.name (
				fullName)

			.label (
				label)

			.nullable (
				false);

		// field

		if (readOnly) {

			formFieldSet.addFormItem (
				readOnlyFormFieldProvider.get ()

				.name (
					fullName)

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

				.renderer (
					renderer)

				.updateHook (
					updateHook)

			);

		}

	}

}
