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

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("objectIdFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectIdFormFieldBuilder {

	// dependencies

	@Inject
	ConsoleHelperRegistry consoleHelperRegistry;

	@Inject
	FormFieldPluginManager formFieldPluginManager;

	// prototype dependencies

	@Inject
	Provider<NullFormFieldValueValidator>
	nullFormFieldValueValidatorProvider;

	@Inject
	Provider<IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

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
	ObjectIdFormFieldSpec spec;

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
				capitalise (
					camelToSpaces (
						name.endsWith ("Id")
							? name.substring (
								0,
								name.length () - 2)
							: name)));

		Boolean nullable =
			ifNull (
				spec.nullable (),
				true);

		Boolean readOnly =
			ifNull (
				spec.readOnly (),
				false);

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

		// field components

		FormFieldAccessor accessor =
			simpleFormFieldAccessorProvider.get ()

			.name (
				name)

			.nativeClass (
				Integer.class);

		FormFieldNativeMapping nativeMapping =
			objectIdFormFieldNativeMappingProvider.get ()

			.consoleHelper (
				consoleHelper);

		FormFieldValueValidator valueValidator =
			nullFormFieldValueValidatorProvider.get ();

		FormFieldConstraintValidator constraintValidator =
			objectFormFieldConstraintValidatorProvider.get ();

		FormFieldInterfaceMapping interfaceMapping =
			identityFormFieldInterfaceMappingProvider.get ();

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
