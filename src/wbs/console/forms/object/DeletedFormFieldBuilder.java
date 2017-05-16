package wbs.console.forms.object;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.forms.basic.IdentityFormFieldInterfaceMapping;
import wbs.console.forms.basic.IdentityFormFieldNativeMapping;
import wbs.console.forms.basic.NullFormFieldConstraintValidator;
import wbs.console.forms.basic.RequiredFormFieldValueValidator;
import wbs.console.forms.basic.SimpleFormFieldAccessor;
import wbs.console.forms.basic.YesNoCsvFormFieldInterfaceMapping;
import wbs.console.forms.basic.YesNoFormFieldRenderer;
import wbs.console.forms.core.ConsoleFormBuilderComponent;
import wbs.console.forms.core.ConsoleFormBuilderContext;
import wbs.console.forms.core.ConsoleFormPluginManagerImplementation;
import wbs.console.forms.core.FormFieldSetImplementation;
import wbs.console.forms.core.ReadOnlyFormField;
import wbs.console.forms.core.UpdatableFormField;
import wbs.console.forms.types.ConsoleFormNativeMapping;
import wbs.console.forms.types.FormFieldAccessor;
import wbs.console.forms.types.FormFieldConstraintValidator;
import wbs.console.forms.types.FormFieldInterfaceMapping;
import wbs.console.forms.types.FormFieldRenderer;
import wbs.console.forms.types.FormFieldUpdateHook;
import wbs.console.forms.types.FormFieldValueValidator;
import wbs.console.helper.core.ConsoleHelper;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("deletedFormFieldBuilder")
public
class DeletedFormFieldBuilder
	implements ConsoleFormBuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleFormPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <UpdatableFormField>
	updatableFormFieldProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <YesNoCsvFormFieldInterfaceMapping>
	yesNoCsvFormFieldInterfaceMappingProvider;

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
	Provider <YesNoFormFieldRenderer>
	yesNoFormFieldRendererProvider;

	// builder

	@BuilderParent
	ConsoleFormBuilderContext context;

	@BuilderSource
	DeletedFormFieldSpec spec;

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
					consoleHelper.deletedFieldName ());

			String label =
				ifNull (
					spec.label (),
					capitalise (
						consoleHelper.deletedLabel ()));

			Boolean readOnly =
				ifNull (
					spec.readOnly (),
					false);

			String yesLabel =
				ifNull (
					spec.yesLabel (),
					"yes");

			String noLabel =
				ifNull (
					spec.noLabel (),
					"no");

			// accessor

			FormFieldAccessor accessor =
				simpleFormFieldAccessorProvider.get ()

				.name (
					name)

				.nativeClass (
					Boolean.class);

			// native mapping

			ConsoleFormNativeMapping nativeMapping =
				identityFormFieldNativeMappingProvider.get ();

			// value validators

			List<FormFieldValueValidator> valueValidators =
				new ArrayList<> ();

			valueValidators.add (
				requiredFormFieldValueValidatorProvider.get ());

			// constraint validator

			FormFieldConstraintValidator constraintValidator =
				nullFormFieldValueConstraintValidatorProvider.get ();

			// interface mapping

			FormFieldInterfaceMapping interfaceMapping =
				identityFormFieldInterfaceMappingProvider.get ();

			// csv mapping

			FormFieldInterfaceMapping csvMapping =
				yesNoCsvFormFieldInterfaceMappingProvider.get ()

				.nullable (
					false);

			// render

			FormFieldRenderer renderer =
				yesNoFormFieldRendererProvider.get ()

				.name (
					name)

				.label (
					label)

				.nullable (
					false)

				.yesLabel (
					yesLabel)

				.noLabel (
					noLabel);

			// update hook

			FormFieldUpdateHook updateHook =
				formFieldPluginManager.getUpdateHook (
					context,
					context.containerClass (),
					name);

			// field

			if (! readOnly) {

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

					.csvMapping (
						csvMapping)

					.renderer (
						renderer)

					.updateHook (
						updateHook)

				);

			} else {

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

					.csvMapping (
						csvMapping)

					.renderer (
						renderer)

				);

			}

		}

	}

}
