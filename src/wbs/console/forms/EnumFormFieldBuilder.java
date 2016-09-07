package wbs.console.forms;

import static wbs.framework.utils.etc.LogicUtils.ifThenElse;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.toEnum;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.NullUtils.ifNullThenRequired;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.uncapitalise;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.EnumConsoleHelper;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.utils.etc.BeanLogic;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("enumFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class EnumFormFieldBuilder {

	// singleton dependencies

	@SingletonDependency
	ApplicationContext applicationContext;

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

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
	Provider <SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

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
			BeanLogic.propertyClassForClass (
				context.containerClass (),
				name);

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
			applicationContext.getComponentRequired (
				enumConsoleHelperName,
				EnumConsoleHelper.class);

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
			simpleFormFieldAccessorProvider.get ()

			.name (
				name)

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
