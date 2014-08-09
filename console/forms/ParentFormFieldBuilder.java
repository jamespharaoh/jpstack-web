package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.helper.ConsoleHelperRegistry;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("parentFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class ParentFormFieldBuilder {

	// dependencies

	@Inject
	ConsoleHelperRegistry consoleHelperRegistry;

	// prototype dependencies

	@Inject
	Provider<NullFormFieldValueValidator>
	nullFormFieldValueValidatorProvider;

	@Inject
	Provider<ParentFormFieldConstraintValidator>
	parentFormFieldValueConstraintValidatorProvider;

	@Inject
	Provider<SimpleFormFieldUpdateHook>
	simpleFormFieldUpdateHookProvider;

	@Inject
	Provider<IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@Inject
	Provider<IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@Inject
	Provider<SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

	@Inject
	Provider<ObjectFormFieldRenderer>
	objectFormFieldRendererProvider;

	@Inject
	Provider<ParentFormFieldAccessor>
	parentFormFieldAccessorProvider;

	@Inject
	Provider<ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@Inject
	Provider<UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext formFieldBuilderContext;

	@BuilderSource
	ParentFormFieldSpec parentFormFieldSpec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// state

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		ConsoleHelper consoleHelper =
			formFieldBuilderContext.consoleHelper ();

		String name =
			ifNull (
				parentFormFieldSpec.name (),
				consoleHelper.parentExists ()
					? consoleHelper.parentFieldName ()
					: "parent");

		String label =
			ifNull (
				parentFormFieldSpec.label (),
				consoleHelper.parentExists ()
					? capitalise (consoleHelper.parentLabel ())
					: "Parent");

		Boolean readOnly =
			ifNull (
				parentFormFieldSpec.readOnly (),
				! consoleHelper.parentTypeIsFixed ());

		ConsoleHelper<?> parentHelper =
			consoleHelper.parentTypeIsFixed ()
				? consoleHelperRegistry.findByObjectClass (
					consoleHelper.parentClass ())
				: null;

		String createPrivDelegate =
			parentHelper != null
				? parentFormFieldSpec.createPrivDelegate ()
				: null;

		String createPrivCode =
			parentHelper != null
				? ifNull (
					parentFormFieldSpec.createPrivCode (),
					readOnly
						? null
						: stringFormat (
							"%s_create",
							consoleHelper.objectTypeCode ()))
				: null;

		// accessor

		FormFieldAccessor accessor =
			consoleHelper.canGetParent ()
				? simpleFormFieldAccessorProvider.get ()
					.name (consoleHelper.parentFieldName ())
					.nativeClass (consoleHelper.parentClass ())
				: parentFormFieldAccessorProvider.get ();

		// native mapping

		FormFieldNativeMapping nativeMapping =
			identityFormFieldNativeMappingProvider.get ();

		// value validator

		FormFieldValueValidator valueValidator =
			nullFormFieldValueValidatorProvider.get ();

		// constraint validator

		FormFieldConstraintValidator constraintValidator =
			parentHelper != null

				? parentFormFieldValueConstraintValidatorProvider.get ()

					.createPrivDelegate (
						createPrivDelegate)

					.createPrivCode (
						createPrivCode)

				: null;

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			identityFormFieldInterfaceMappingProvider.get ();

		// renderer

		FormFieldRenderer renderer =
			objectFormFieldRendererProvider.get ()
				.name (name)
				.label (label)
				.entityFinder (parentHelper);

		// update hook

		FormFieldUpdateHook updateHook =
			simpleFormFieldUpdateHookProvider.get ()
				.name (name);

		// field

		if (! readOnly) {

			if (! consoleHelper.parentTypeIsFixed ())
				throw new RuntimeException ();

			// read only field

			formFieldSet.formFields ().add (

				updatableFormFieldProvider.get ()

				.name (
					name)

				.label (
					label)

				.accessor (
					accessor)

				.nativeMapping (
					nativeMapping)

				.valueValidator (
					valueValidator)

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

			if (parentFormFieldSpec.readOnly () != null
					&& ! parentFormFieldSpec.readOnly ())
				throw new RuntimeException ();

			formFieldSet.formFields ().add (

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
