package wbs.console.forms.object;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.forms.basic.IdentityFormFieldInterfaceMapping;
import wbs.console.forms.basic.IdentityFormFieldNativeMapping;
import wbs.console.forms.basic.RequiredFormFieldValueValidator;
import wbs.console.forms.basic.SimpleFormFieldAccessor;
import wbs.console.forms.core.FormFieldBuilderContext;
import wbs.console.forms.core.FormFieldSetImplementation;
import wbs.console.forms.core.ReadOnlyFormField;
import wbs.console.forms.core.UpdatableFormField;
import wbs.console.forms.logic.FormFieldPluginManagerImplementation;
import wbs.console.forms.types.FormFieldAccessor;
import wbs.console.forms.types.FormFieldConstraintValidator;
import wbs.console.forms.types.FormFieldInterfaceMapping;
import wbs.console.forms.types.FormFieldNativeMapping;
import wbs.console.forms.types.FormFieldRenderer;
import wbs.console.forms.types.FormFieldUpdateHook;
import wbs.console.forms.types.FormFieldValueValidator;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("parentFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class ParentFormFieldBuilder
	implements BuilderComponent {

	// dependencies

	@WeakSingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <ObjectFormFieldRenderer>
	objectFormFieldRendererProvider;

	@PrototypeDependency
	Provider <ParentFormFieldAccessor>
	parentFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <ParentFormFieldConstraintValidator>
	parentFormFieldValueConstraintValidatorProvider;

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
	ParentFormFieldSpec spec;

	@BuilderTarget
	FormFieldSetImplementation formFieldSet;

	// build

	@Override
	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

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

			ConsoleHelper <?> parentHelper =
				consoleHelper.parentTypeIsFixed ()
					? objectManager.findConsoleHelperRequired (
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
				ifThenElse (
					consoleHelper.canGetParent (),

				() -> simpleFormFieldAccessorProvider.get ()

					.name (
						consoleHelper.parentFieldName ())

					.nativeClass (
						consoleHelper.parentClass ()),

				() -> parentFormFieldAccessorProvider.get ()

			);

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

				formFieldSet.addFormItem (
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

				formFieldSet.addFormItem (
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

}
