package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleHelperRegistry;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("objectFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectFormFieldBuilder {

	// dependencies

	@Inject
	ConsoleHelperRegistry consoleHelperRegistry;

	@Inject
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@Inject
	Provider<NullFormFieldValueValidator>
	nullFormFieldValueValidatorProvider;

	@Inject
	Provider<IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@Inject
	Provider<ObjectCsvFormFieldInterfaceMapping>
	objectCsvFormFieldInterfaceMappingProvider;

	@Inject
	Provider<IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@Inject
	Provider<ObjectIdFormFieldNativeMapping>
	objectIdFormFieldNativeMappingProvider;

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

		/*
		Boolean dynamic =
			ifNull (
				spec.dynamic(),
				false);
		*/

		ConsoleHelper<?> consoleHelper =
			consoleHelperRegistry.findByObjectName (
				spec.objectTypeName ());

		if (consoleHelper == null) {

			throw new RuntimeException (
				stringFormat (
					"Console helper does not exist: %s",
					spec.objectTypeName ()));

		}

		String rootFieldName =
			spec.rootFieldName ();

		// field type

		Class<?> propertyClass =
			BeanLogic.propertyClassForClass (
				context.containerClass (),
				name);

		FormFieldAccessor accessor;
		FormFieldNativeMapping nativeMapping;

		if (propertyClass == Integer.class) {

			// accessor

			accessor =
				simpleFormFieldAccessorProvider.get ()

				.name (
					name)

				.nativeClass (
					Integer.class);

			// native mapping

			nativeMapping =
				objectIdFormFieldNativeMappingProvider.get ()

				.consoleHelper (
					consoleHelper);

		} else {

			// accessor

			accessor =
				simpleFormFieldAccessorProvider.get ()

				.name (
					name)

				.nativeClass (
					Record.class);

			// native mapping

			nativeMapping =
				identityFormFieldNativeMappingProvider.get ();

		}

		// value validator

		FormFieldValueValidator valueValidator =
			nullFormFieldValueValidatorProvider.get ();

		// constraint validator

		FormFieldConstraintValidator constraintValidator =
			objectFormFieldConstraintValidatorProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			identityFormFieldInterfaceMappingProvider.get ();

		// csv mapping

		FormFieldInterfaceMapping csvMapping =
			objectCsvFormFieldInterfaceMappingProvider.get ();

		// renderer

		FormFieldRenderer renderer =
			objectFormFieldRendererProvider.get ()

			.name (
				name)

			.label (
				label)

			.nullable (
				nullable)

			.rootFieldName (
				rootFieldName)

			.entityFinder (
				consoleHelper);

		// update hook

		FormFieldUpdateHook updateHook =
			formFieldPluginManager.getUpdateHook (
				context,
				context.containerClass (),
				name);

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

				.csvMapping (
					csvMapping)

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

				.csvMapping (
					csvMapping)

				.renderer (
					renderer)

			);

		}

	}

}
