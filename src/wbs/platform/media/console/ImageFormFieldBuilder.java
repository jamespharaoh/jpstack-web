package wbs.platform.media.console;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.forms.DereferenceFormFieldAccessor;
import wbs.console.forms.FormFieldAccessor;
import wbs.console.forms.FormFieldBuilderContext;
import wbs.console.forms.FormFieldConstraintValidator;
import wbs.console.forms.FormFieldInterfaceMapping;
import wbs.console.forms.FormFieldNativeMapping;
import wbs.console.forms.FormFieldPluginManagerImplementation;
import wbs.console.forms.FormFieldRenderer;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormFieldUpdateHook;
import wbs.console.forms.FormFieldValueValidator;
import wbs.console.forms.IdentityFormFieldInterfaceMapping;
import wbs.console.forms.IdentityFormFieldNativeMapping;
import wbs.console.forms.NullFormFieldConstraintValidator;
import wbs.console.forms.ReadOnlyFormField;
import wbs.console.forms.RequiredFormFieldValueValidator;
import wbs.console.forms.SimpleFormFieldAccessor;
import wbs.console.forms.UpdatableFormField;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.media.model.MediaRec;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("imageFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class ImageFormFieldBuilder {

	// dependencies

	@Inject
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@Inject
	Provider<DereferenceFormFieldAccessor>
	dereferenceFormFieldAccessorProvider;

	@Inject
	Provider<IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@Inject
	Provider<IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@Inject
	Provider<ImageCsvFormFieldInterfaceMapping>
	imageCsvFormFieldInterfaceMappingProvider;

	@Inject
	Provider<NullFormFieldConstraintValidator>
	nullFormFieldValueConstraintValidatorProvider;

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
	Provider<ImageFormFieldRenderer>
	imageFormFieldRendererProvider;

	@Inject
	Provider<UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	ImageFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// state

	String name;
	String label;
	Boolean nullable;
	Boolean showFilename;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();
		buildField ();

	}

	void setDefaults () {

		name =
			spec.name ();

		label =
			ifNull (
				spec.label (),
				capitalise (
					camelToSpaces (
						name)));

		nullable =
			ifNull (
				spec.nullable (),
				false);

		showFilename =
			ifNull (
				spec.showFilename (),
				true);

	}

	void buildField () {

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
					MediaRec.class);

		}

		// native mapping

		FormFieldNativeMapping nativeMapping =
			identityFormFieldNativeMappingProvider.get ();

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
			identityFormFieldInterfaceMappingProvider.get ();

		// renderer

		FormFieldRenderer renderer =
			imageFormFieldRendererProvider.get ()

			.name (
				name)

			.label (
				label)

			.nullable (
				nullable)

			.showFilename (
				showFilename);

		// update hook

		FormFieldUpdateHook updateHook =
			formFieldPluginManager.getUpdateHook (
				context,
				context.containerClass (),
				name);

		// csv mapping

		FormFieldInterfaceMapping csvMapping =
			imageCsvFormFieldInterfaceMappingProvider.get ();

		// form field

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

			.csvMapping (
				csvMapping)

			.renderer (
				renderer)

			.updateHook (
				updateHook)

		);

	}

}
