package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
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
import wbs.framework.record.Record;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.helper.ConsoleHelperRegistry;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("objectFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectFormFieldBuilder {

	// dependencies

	@Inject
	ConsoleHelperRegistry consoleHelperRegistry;

	// prototype dependencies

	@Inject
	Provider<NullFormFieldValueValidator>
	nullFormFieldValueValidatorProvider;

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
	Provider<ObjectFormFieldConstraintValidator>
	objectFormFieldConstraintValidatorProvider;

	@Inject
	Provider<ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@Inject
	Provider<UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	ObjectFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// state

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
				capitalise (camelToSpaces (name)));

		Boolean nullable =
			ifNull (
				spec.nullable (),
				true);

		Boolean readOnly =
			ifNull (
				spec.readOnly (),
				false);

		EntityFinder<?> entityFinder =
			consoleHelperRegistry.findByObjectName (
				spec.finderName ());

		if (entityFinder == null) {

			throw new RuntimeException (
				stringFormat (
					"Entity finder does not exist: %s",
					spec.finderName ()));

		}

		String rootFieldName =
			spec.rootFieldName ();

		// field components

		FormFieldAccessor accessor =
			simpleFormFieldAccessorProvider.get ()
				.name (name)
				.nativeClass (Record.class);

		FormFieldNativeMapping nativeMapping =
			identityFormFieldNativeMappingProvider.get ();

		FormFieldValueValidator valueValidator =
			nullFormFieldValueValidatorProvider.get ();

		FormFieldConstraintValidator constraintValidator =
			objectFormFieldConstraintValidatorProvider.get ();

		FormFieldInterfaceMapping interfaceMapping =
			identityFormFieldInterfaceMappingProvider.get ();

		FormFieldRenderer renderer =
			objectFormFieldRendererProvider.get ()
				.name (name)
				.label (label)
				.nullable (nullable)
				.rootFieldName (rootFieldName)
				.entityFinder (entityFinder);

		FormFieldUpdateHook updateHook =
			simpleFormFieldUpdateHookProvider.get ()
				.name (name);

		// field

		if (! readOnly) {

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
