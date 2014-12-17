package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.regex.Pattern;

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
import wbs.platform.console.helper.ConsoleObjectManager;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("codeFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class CodeFormFieldBuilder {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@Inject
	Provider<CodeFormFieldConstraintValidator>
	codeFormFieldValueConstraintValidatorProvider;

	@Inject
	Provider<CodeFormFieldValueValidator>
	codeFormFieldValueValidatorProvider;

	@Inject
	Provider<DelegateFormFieldAccessor>
	delegateFormFieldAccessorProvider;

	@Inject
	Provider<IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@Inject
	Provider<IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@Inject
	Provider<ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@Inject
	Provider<SimpleFormFieldUpdateHook>
	simpleEventFormFieldUpdateHookProvider;

	@Inject
	Provider<SpecialFormFieldAccessor>
	specialFormFieldAccessorProvider;

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
	CodeFormFieldSpec spec;

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
				spec.delegate () == null
					? consoleHelper.codeFieldName ()
					: null,
				"code");

		String fullName =
			spec.delegate () == null
				? name
				: stringFormat (
					"%s.%s",
					spec.delegate (),
					name);

		String label =
			ifNull (
				spec.label (),
				spec.delegate () == null
					? capitalise (
						consoleHelper.codeLabel ())
					: null,
				"Code");

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
				spec.delegate () != null ? true : null,
				! consoleHelper.nameIsCode (),
				false);

		Pattern pattern =
			spec.pattern () != null
				? Pattern.compile (spec.pattern ())
				: CodeFormFieldValueValidator.defaultPattern;

		// accessor

		FormFieldAccessor accessor =
			specialFormFieldAccessorProvider.get ()

			.specialName (
				"code")

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

		// value validator

		FormFieldValueValidator valueValidator =
			codeFormFieldValueValidatorProvider.get ()

			.pattern (
				pattern);

		// constraint validator

		FormFieldConstraintValidator constraintValidator =
			codeFormFieldValueConstraintValidatorProvider.get ();

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
				false)

			.size (
				FormField.defaultSize);

		// update hook

		FormFieldUpdateHook updateHook =
			simpleEventFormFieldUpdateHookProvider.get ()

			.name (
				name);

		// field

		if (readOnly) {

			formFieldSet.formFields ().add (

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

		}

	}

}
