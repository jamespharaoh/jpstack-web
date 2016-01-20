package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.optionalRequired;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleHelperRegistry;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.object.ObjectManager;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("nameFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class NameFormFieldBuilder {

	// dependencies

	@Inject
	ConsoleHelperRegistry consoleHelperRegistry;

	@Inject
	FormFieldPluginManagerImplementation formFieldPluginManager;

	@Inject
	ObjectManager objectManager;

	// prototype dependencies

	@Inject
	Provider<DelegateFormFieldAccessor>
	delegateFormFieldAccessorProvider;

	@Inject
	Provider<IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@Inject
	Provider<NameFormFieldAccessor>
	nameFormFieldAccessorProvider;

	@Inject
	Provider<IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@Inject
	Provider<NameFormFieldConstraintValidator>
	nameFormFieldValueConstraintValidatorProvider;

	@Inject
	Provider<NameFormFieldValueValidator>
	nameFormFieldValueValidatorProvider;

	@Inject
	Provider<ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@Inject
	Provider<RequiredFormFieldValueValidator>
	requiredFormFieldValueValidatorProvider;

	@Inject
	Provider<TextFormFieldRenderer>
	textFormFieldRendererProvider;

	@Inject
	Provider<UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	NameFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		ConsoleHelper thisConsoleHelper =
			context.consoleHelper ();

		ConsoleHelper thatConsoleHelper;

		if (
			isNotNull (
				spec.delegate ())
		) {

			Class thatClass =
				optionalRequired (
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
				spec.delegate () == null
					? thatConsoleHelper.nameFieldName ()
					: null,
				"name");

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
				spec.delegate () == null
					? capitalise (
						thatConsoleHelper.nameLabel ())
					: null,
				"Name");

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
			nameFormFieldAccessorProvider.get ()

			.consoleHelper (
				thatConsoleHelper);

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

		// value validator

		List<FormFieldValueValidator> valueValidators =
			new ArrayList<> ();

		valueValidators.add (
			requiredFormFieldValueValidatorProvider.get ());

		valueValidators.add (
			nameFormFieldValueValidatorProvider.get ());

		// constraint validator

		FormFieldConstraintValidator constraintValidator =
			nameFormFieldValueConstraintValidatorProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			identityFormFieldInterfaceMappingProvider.get ();

		// renderer

		FormFieldRenderer renderer =
			textFormFieldRendererProvider.get ()

			.name (
				fullName)

			.label (
				label)

			.nullable (
				false);

		// update hook

		FormFieldUpdateHook updateHook =
			formFieldPluginManager.getUpdateHook (
				context,
				context.containerClass (),
				name);

		// field

		if (readOnly) {

			formFieldSet.addFormField (
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

			formFieldSet.addFormField (
				updatableFormFieldProvider.get ()

				.name (
					fullName)

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
