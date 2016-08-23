package wbs.console.forms;

import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

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
@PrototypeComponent ("parentFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class ParentFormFieldBuilder {

	// dependencies

	@Inject
	ConsoleHelperRegistry consoleHelperRegistry;

	@Inject
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@Inject
	Provider<IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@Inject
	Provider<IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@Inject
	Provider<ObjectFormFieldRenderer>
	objectFormFieldRendererProvider;

	@Inject
	Provider<ParentFormFieldAccessor>
	parentFormFieldAccessorProvider;

	@Inject
	Provider<ParentFormFieldConstraintValidator>
	parentFormFieldValueConstraintValidatorProvider;

	@Inject
	Provider<ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@Inject
	Provider<RequiredFormFieldValueValidator>
	requiredFormFieldValueValidatorProvider;

	@Inject
	Provider<SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

	@Inject
	Provider<UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	ParentFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// state

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
				consoleHelper.parentExists ()
					? consoleHelper.parentFieldName ()
					: "parent");

		String label =
			ifNull (
				spec.label (),
				consoleHelper.parentExists ()
					? capitalise (consoleHelper.parentLabel ())
					: "Parent");

		Boolean readOnly =
			ifNull (
				spec.readOnly (),
				! consoleHelper.parentTypeIsFixed ());

		ConsoleHelper<?> parentHelper =
			consoleHelper.parentTypeIsFixed ()
				? consoleHelperRegistry.findByObjectClass (
					consoleHelper.parentClass ())
				: null;

		String createPrivDelegate =
			parentHelper != null
				? spec.createPrivDelegate ()
				: null;

		String createPrivCode =
			parentHelper != null
				? ifNull (
					spec.createPrivCode (),
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

		List<FormFieldValueValidator> valueValidators =
			new ArrayList<> ();

		valueValidators.add (
			requiredFormFieldValueValidatorProvider.get ());

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

			.name (
				name)

			.label (
				label)

			.entityFinder (
				parentHelper)

			.mini (
				consoleHelper.parentTypeIsFixed ())

			.nullable (
				false);

		// update hook

		FormFieldUpdateHook updateHook =
			formFieldPluginManager.getUpdateHook (
				context,
				context.containerClass (),
				name);

		// field

		if (! readOnly) {

			if (! consoleHelper.parentTypeIsFixed ())
				throw new RuntimeException ();

			// read only field

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

				.renderer (
					renderer)

				.updateHook (
					updateHook)

			);

		} else {

			if (
				spec.readOnly () != null
				&& ! spec.readOnly ()
			) {
				throw new RuntimeException ();
			}

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

				.renderer (
					renderer)

			);

		}

	}

}
