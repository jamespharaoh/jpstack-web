package wbs.console.forms;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.utils.string.CodeUtils;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("codeFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class CodeFormFieldBuilder {

	// singleton dependencies

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <CodeFormFieldConstraintValidator>
	codeFormFieldValueConstraintValidatorProvider;

	@PrototypeDependency
	Provider <CodeFormFieldValueValidator>
	codeFormFieldValueValidatorProvider;

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
	Provider <ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@PrototypeDependency
	Provider <RequiredFormFieldValueValidator>
	requiredFormFieldValueValidatorProvider;

	@PrototypeDependency
	Provider <SpecialFormFieldAccessor>
	specialFormFieldAccessorProvider;

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
				? namedPatterns.containsKey (
					spec.pattern ())
					? namedPatterns.get (
						spec.pattern ())
					: Pattern.compile (
						spec.pattern ())
				: CodeUtils.codePattern;

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

		List<FormFieldValueValidator> valueValidators =
			new ArrayList<> ();

		valueValidators.add (
			requiredFormFieldValueValidatorProvider.get ());

		valueValidators.add (
			codeFormFieldValueValidatorProvider.get ()

			.pattern (
				pattern)

		);

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

				.csvMapping (
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

				.csvMapping (
					interfaceMapping)

				.renderer (
					renderer)

				.updateHook (
					updateHook)

			);

		}

	}

	public final static
	Map<String,Pattern> namedPatterns =
		ImmutableMap.<String,Pattern>builder ()

		.put (
			"relaxed",
			CodeUtils.relaxedCodePattern)

		.build ();

}
