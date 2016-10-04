package wbs.console.forms;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.toEnum;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.ifNullThenRequired;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.uncapitalise;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.helper.EnumConsoleHelper;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("enumFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class EnumFormFieldBuilder {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <DereferenceFormFieldAccessor>
	dereferenceFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <EnumCsvFormFieldInterfaceMapping>
	enumCsvFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <EnumFormFieldRenderer>
	enumFormFieldRendererProvider;

	@PrototypeDependency
	Provider <HiddenFormField>
	hiddenFormFieldProvider;

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
	Provider <UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	EnumFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			spec.name ();

		String fieldName =
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

		Boolean hidden =
			ifNull (
				spec.hidden (),
				false);

		Class <?> propertyClass =
			optionalGetRequired (
				objectManager.dereferenceType (
					optionalOf (
						context.containerClass ()),
					optionalOf (
						fieldName)));

		String enumConsoleHelperName =
			ifNullThenRequired (

			() -> spec.helperBeanName (),

			() -> ifThenElse (
				isNotNull (
					propertyClass.getEnclosingClass ()),

				() -> stringFormat (
					"%s%sConsoleHelper",
					uncapitalise (
						propertyClass.getEnclosingClass ().getSimpleName ()),
					propertyClass.getSimpleName ()),

				() -> stringFormat (
					"%sConsoleHelper",
					uncapitalise (
						propertyClass.getSimpleName ()))

			)

		);

		EnumConsoleHelper enumConsoleHelper =
			componentManager.getComponentOrElse (
				enumConsoleHelperName,
				EnumConsoleHelper.class,
				() -> new EnumConsoleHelper ()
					.enumClass (propertyClass)
					.auto ());

		Optional <Optional <Object>> implicitValue =
			spec.implicitValue () != null
				? Optional.of (
					Optional.of (
						toEnum (
							enumConsoleHelper.enumClass (),
							spec.implicitValue ())))
				: Optional.absent ();

		// accessor

		FormFieldAccessor accessor =
			dereferenceFormFieldAccessorProvider.get ()

			.path (
				fieldName)

			.nativeClass (
				enumConsoleHelper.enumClass ());

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
			nullFormFieldValueConstraintValidatorProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			identityFormFieldInterfaceMappingProvider.get ();

		// csv mapping

		FormFieldInterfaceMapping csvMapping =
			enumCsvFormFieldInterfaceMappingProvider.get ();

		// renderer

		FormFieldRenderer renderer =
			enumFormFieldRendererProvider.get ()

			.name (
				name)

			.label (
				label)

			.nullable (
				nullable)

			.enumConsoleHelper (
				enumConsoleHelper);

		// update hook

		FormFieldUpdateHook updateHook =
			formFieldPluginManager.getUpdateHook (
				context,
				context.containerClass (),
				name);

		// form field

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
					implicitValue)

			);

		} else if (readOnly) {

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

				.csvMapping (
					csvMapping)

				.renderer (
					renderer)

			);

		} else {

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

				.csvMapping (
					csvMapping)

				.renderer (
					renderer)

				.updateHook (
					updateHook)

			);

		}

	}

}
