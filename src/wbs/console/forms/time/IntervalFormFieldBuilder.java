package wbs.console.forms.time;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.classEqualSafe;
import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Interval;

import wbs.console.forms.basic.IdentityFormFieldInterfaceMapping;
import wbs.console.forms.basic.IdentityFormFieldNativeMapping;
import wbs.console.forms.basic.NullFormFieldConstraintValidator;
import wbs.console.forms.basic.RequiredFormFieldValueValidator;
import wbs.console.forms.basic.SimpleFormFieldAccessor;
import wbs.console.forms.core.ConsoleFormBuilderComponent;
import wbs.console.forms.core.ConsoleFormBuilderContext;
import wbs.console.forms.core.ConsoleFormPluginManagerImplementation;
import wbs.console.forms.core.FormFieldSetImplementation;
import wbs.console.forms.core.ReadOnlyFormField;
import wbs.console.forms.core.UpdatableFormField;
import wbs.console.forms.object.DereferenceFormFieldAccessor;
import wbs.console.forms.text.TextFormFieldRenderer;
import wbs.console.forms.types.ConsoleFormNativeMapping;
import wbs.console.forms.types.FormFieldAccessor;
import wbs.console.forms.types.FormFieldConstraintValidator;
import wbs.console.forms.types.FormFieldInterfaceMapping;
import wbs.console.forms.types.FormFieldRenderer;
import wbs.console.forms.types.FormFieldUpdateHook;
import wbs.console.forms.types.FormFieldValueValidator;
import wbs.console.helper.manager.ConsoleObjectManager;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import

wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.PropertyUtils;
import wbs.utils.time.TextualInterval;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("intervalFormFieldBuilder")
public
class IntervalFormFieldBuilder
	implements ConsoleFormBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ConsoleFormPluginManagerImplementation formFieldPluginManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <DereferenceFormFieldAccessor>
		dereferenceFormFieldAccessorProvider;

	@PrototypeDependency
	ComponentProvider <IdentityFormFieldNativeMapping>
		identityFormFieldNativeMappingProvider;

	@PrototypeDependency
	ComponentProvider <IntervalFormFieldNativeMapping>
		intervalFormFieldNativeMappingProvider;

	@PrototypeDependency
	ComponentProvider <NullFormFieldConstraintValidator>
		nullFormFieldValueConstraintValidatorProvider;

	@PrototypeDependency
	ComponentProvider <IntervalFormFieldInterfaceMapping>
		intervalFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	ComponentProvider <ReadOnlyFormField> readOnlyFormFieldProvider;

	@PrototypeDependency
	ComponentProvider <RequiredFormFieldValueValidator>
		requiredFormFieldValueValidatorProvider;

	@PrototypeDependency
	ComponentProvider <SimpleFormFieldAccessor> simpleFormFieldAccessorProvider;

	@PrototypeDependency
	ComponentProvider <TextFormFieldRenderer> textFormFieldRendererProvider;

	@PrototypeDependency
	ComponentProvider <IdentityFormFieldInterfaceMapping>
		identityFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	ComponentProvider <UpdatableFormField> updatableFormFieldProvider;

	// builder

	@BuilderParent
	ConsoleFormBuilderContext context;

	@BuilderSource
	IntervalFormFieldSpec spec;

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

			// accessor

			Optional<Class<?>> propertyClass;
			FormFieldAccessor accessor;

			if (readOnly) {

				propertyClass =
					objectManager.dereferenceType (
						taskLogger,
						optionalOf (
							context.containerClass ()),
						optionalOf (
							fieldName));

				accessor =
					dereferenceFormFieldAccessorProvider.provide (
						taskLogger)

					.path (
						fieldName);

			} else {

				propertyClass =
					Optional.of (
						PropertyUtils.propertyClassForClass (
							context.containerClass (),
							fieldName));

				accessor =
					simpleFormFieldAccessorProvider.provide (
						taskLogger)

					.name (
						name)

					.nativeClass (
						propertyClass.get ());

			}

			// native mapping

			ConsoleFormNativeMapping nativeMapping;

			if (
				classEqualSafe (
					propertyClass.get (),
					Interval.class)
			) {

				nativeMapping =
					intervalFormFieldNativeMappingProvider.provide (
						taskLogger);

			} else if (
				classEqualSafe (
					propertyClass.get (),
					TextualInterval.class)
			) {

				nativeMapping =
					identityFormFieldNativeMappingProvider.provide (
						taskLogger);

			} else {

				throw new RuntimeException (
					stringFormat (
						"Don't know how to map %s",
						classNameFull (
							propertyClass.get ())));

			}

			// value validator

			List<FormFieldValueValidator> valueValidators =
				new ArrayList<> ();

			if (! nullable) {

				valueValidators.add (
					requiredFormFieldValueValidatorProvider.provide (
						taskLogger));

			}

			// constraint validator

			FormFieldConstraintValidator constraintValidator =
				nullFormFieldValueConstraintValidatorProvider.provide (
					taskLogger);

			// interface mapping

			FormFieldInterfaceMapping interfaceMapping =
				intervalFormFieldInterfaceMappingProvider.provide (
					taskLogger);

			// renderer

			FormFieldRenderer renderer =
				textFormFieldRendererProvider.provide (
					taskLogger)

				.name (
					name)

				.label (
					label)

				.nullable (
					nullable)

				.addPreset (
					"today")

				.addPreset (
					"yesterday")

				.addPreset (
					"this month")

				.addPreset (
					"last month");

			// update hook

			FormFieldUpdateHook updateHook =
				formFieldPluginManager.getUpdateHook (
					taskLogger,
					context,
					context.containerClass (),
					name);

			// form field

			if (readOnly) {

				formFieldSet.addFormItem (
					readOnlyFormFieldProvider.provide (
						taskLogger)

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

			} else {

				formFieldSet.addFormItem (
					updatableFormFieldProvider.provide (
						taskLogger)

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

			}

		}

	}

}
