package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;

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

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("codeFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class CodeFormFieldBuilder {

	// prototype dependencies

	@Inject
	Provider<CodeFormFieldValueValidator>
	codeFormFieldValueValidatorProvider;

	@Inject
	Provider<CodeFormFieldConstraintValidator>
	codeFormFieldValueConstraintValidatorProvider;

	@Inject
	Provider<ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@Inject
	Provider<SimpleFormFieldUpdateHook>
	simpleEventFormFieldUpdateHookProvider;

	@Inject
	Provider<UpdatableFormField>
	updatableFormFieldProvider;

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
	Provider<TextFormFieldRenderer>
	textFormFieldRendererProvider;

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
				consoleHelper.codeFieldName ());

		String label =
			ifNull (
				spec.label (),
				capitalise (consoleHelper.codeLabel ()));

		Boolean readOnly =
			ifNull (
				spec.readOnly (),
				! consoleHelper.nameIsCode ());

		Pattern pattern =
			spec.pattern () != null
				? Pattern.compile (spec.pattern ())
				: CodeFormFieldValueValidator.defaultPattern;

		// accessor

		FormFieldAccessor accessor =
			simpleFormFieldAccessorProvider.get ()

			.name (
				name)

			.nativeClass (
				String.class);

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
				name)

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
