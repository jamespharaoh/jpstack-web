package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.joda.time.Interval;

import com.google.common.base.Optional;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.utils.TextualInterval;
import wbs.framework.utils.etc.BeanLogic;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("intervalFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class IntervalFormFieldBuilder {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@Inject
	Provider<DereferenceFormFieldAccessor>
	dereferenceFormFieldAccessorProvider;

	@Inject
	Provider<IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@Inject
	Provider<IntervalFormFieldNativeMapping>
	intervalFormFieldNativeMappingProvider;

	@Inject
	Provider<NullFormFieldConstraintValidator>
	nullFormFieldValueConstraintValidatorProvider;

	@Inject
	Provider<IntervalFormFieldInterfaceMapping>
	intervalFormFieldInterfaceMappingProvider;

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
	Provider<TextFormFieldRenderer>
	textFormFieldRendererProvider;

	@Inject
	Provider<IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@Inject
	Provider<UpdatableFormField>
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
			equal (
				propertyClass.get (),
				Interval.class)
		) {

			nativeMapping =
				intervalFormFieldNativeMappingProvider.get ();

		} else if (
			equal (
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
