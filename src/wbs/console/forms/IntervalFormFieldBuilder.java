package wbs.console.forms;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.TypeUtils.classEqualSafe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import org.joda.time.Interval;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.utils.TextualInterval;
import wbs.framework.utils.etc.BeanLogic;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("intervalFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class IntervalFormFieldBuilder {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <DereferenceFormFieldAccessor>
	dereferenceFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <IntervalFormFieldNativeMapping>
	intervalFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <NullFormFieldConstraintValidator>
	nullFormFieldValueConstraintValidatorProvider;

	@PrototypeDependency
	Provider <IntervalFormFieldInterfaceMapping>
	intervalFormFieldInterfaceMappingProvider;

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
	Provider <TextFormFieldRenderer>
	textFormFieldRendererProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	IntervalFormFieldSpec spec;

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

		// accessor

		Optional<Class<?>> propertyClass;
		FormFieldAccessor accessor;

		if (readOnly) {

			propertyClass =
				objectManager.dereferenceType (
					Optional.of (
						context.containerClass ()),
					Optional.of (
						fieldName));

			accessor =
				dereferenceFormFieldAccessorProvider.get ()

				.path (
					fieldName);

		} else {

			propertyClass =
				Optional.of (
					BeanLogic.propertyClassForClass (
						context.containerClass (),
						fieldName));

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
			classEqualSafe (
				propertyClass.get (),
				Interval.class)
		) {

			nativeMapping =
				intervalFormFieldNativeMappingProvider.get ();

		} else if (
			classEqualSafe (
				propertyClass.get (),
				TextualInterval.class)
		) {

			nativeMapping =
				identityFormFieldNativeMappingProvider.get ();

		} else {

			throw new RuntimeException ();

		}

		// value validator

		List<FormFieldValueValidator> valueValidators =
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
			intervalFormFieldInterfaceMappingProvider.get ();

		// renderer

		FormFieldRenderer renderer =
			textFormFieldRendererProvider.get ()

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
				context,
				context.containerClass (),
				name);

		// form field

		if (readOnly) {

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

				.renderer (
					renderer)

				.updateHook (
					updateHook)

			);

		}

	}

}
