package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.optionalEquals;
import static wbs.framework.utils.etc.Misc.stringFormat;

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
@PrototypeComponent ("objectFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectFormFieldBuilder {

	// dependencies

	@Inject
	ConsoleHelperRegistry consoleHelperRegistry;

	@Inject
	FormFieldPluginManagerImplementation formFieldPluginManager;

	@Inject
	ObjectManager objectManager;

	// prototype dependencies

	@Inject
	Provider<DereferenceFormFieldAccessor>
	dereferenceFormFieldAccessorProvider;

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

		Optional<ConsoleHelper<?>> consoleHelper;

		if (
			isNotNull (
				spec.objectTypeName ())
		) {

			consoleHelper =
				Optional.<ConsoleHelper<?>>fromNullable (
					consoleHelperRegistry.findByObjectName (
						spec.objectTypeName ()));

			if (
				isNotPresent (
					consoleHelper)
			) {

				throw new RuntimeException (
					stringFormat (
						"Console helper does not exist: %s",
						spec.objectTypeName ()));

			}

		} else {

			consoleHelper =
				Optional.<ConsoleHelper<?>>absent ();

		}

		String rootFieldName =
			spec.rootFieldName ();

		// field type

		Optional<Class<?>> propertyClass =
			objectManager.dereferenceType (
				Optional.<Class<?>>of (
					context.containerClass ()),
				Optional.of (
					fieldName));

		// accessor

		FormFieldAccessor accessor;

		if (
			isNotNull (
				spec.fieldName ())
		) {

			accessor =
				dereferenceFormFieldAccessorProvider.get ()

				.path (
					spec.fieldName ());

		} else {

			accessor =
				simpleFormFieldAccessorProvider.get ()

				.name (
					name)

				.nativeClass (
					propertyClass.get ());

		}

		// native mapping

		FormFieldNativeMapping nativeMapping;

		if (

			optionalEquals (
				propertyClass,
				Integer.class)

		) {

			nativeMapping =
				objectIdFormFieldNativeMappingProvider.get ()

				.consoleHelper (
					consoleHelper.orNull ());

		} else {

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
			objectCsvFormFieldInterfaceMappingProvider.get ()

			.rootFieldName (
				rootFieldName);

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
				consoleHelper.orNull ())

			.mini (
				isNotNull (
					spec.objectTypeName ()));

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
