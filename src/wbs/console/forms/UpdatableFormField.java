package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.console.html.ScriptRef;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.record.PermanentRecord;
import wbs.framework.utils.etc.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("updatableFormField")
@DataClass ("updatable-form-field")
public
class UpdatableFormField<Container,Generic,Native,Interface>
	implements FormField<Container,Generic,Native,Interface> {

	// properties

	@Getter
	Boolean virtual = false;

	@DataAttribute
	@Getter @Setter
	Boolean large = false;

	@DataAttribute
	@Getter @Setter
	String name;

	@DataAttribute
	@Getter @Setter
	String label;

	@Getter @Setter
	Set<ScriptRef> scriptRefs =
		new LinkedHashSet<ScriptRef> ();

	@Getter @Setter
	FormFieldAccessor<Container,Native> accessor;

	@Getter @Setter
	FormFieldNativeMapping<Generic,Native> nativeMapping;

	@Getter @Setter
	FormFieldValueValidator<Generic> valueValidator;

	@Getter @Setter
	FormFieldConstraintValidator<Container,Native> constraintValidator;

	@Getter @Setter
	FormFieldInterfaceMapping<Container,Generic,Interface> interfaceMapping;

	@Getter @Setter
	FormFieldRenderer<Container,Interface> renderer;

	@Getter @Setter
	FormFieldUpdateHook<Container,Generic,Native> updateHook;

	// implementation

	@Override
	public
	Boolean fileUpload () {
		return renderer.fileUpload ();
	}

	@Override
	public
	void init (
			String fieldSetName) {

		if (valueValidator == null) {

			throw new NullPointerException (
				stringFormat (
					"No value validator for %s.%s",
					fieldSetName,
					name));

		}

		if (interfaceMapping == null) {

			throw new NullPointerException (
				stringFormat (
					"No interface mapping for %s.%s",
					fieldSetName,
					name));

		}

	}

	@Override
	public
	void renderTableCellList (
			@NonNull FormatWriter out,
			@NonNull Container container,
			boolean link,
			int colspan) {

		Optional<Native> nativeValue =
			accessor.read (
				container);

		Optional<Generic> genericValue =
			nativeMapping.nativeToGeneric (
				nativeValue);

		Optional<Interface> interfaceValue =
			interfaceMapping.genericToInterface (
				container,
				genericValue);

		renderer.renderTableCellList (
			out,
			container,
			interfaceValue,
			link,
			colspan);

	}

	@Override
	public
	void renderTableCellProperties (
			@NonNull FormatWriter out,
			@NonNull Container container) {

		Optional<Native> nativeValue =
			accessor.read (
				container);

		Optional<Generic> genericValue =
			nativeMapping.nativeToGeneric (
				nativeValue);

		Optional<Interface> interfaceValue =
			interfaceMapping.genericToInterface (
				container,
				genericValue);

		renderer.renderTableCellProperties (
			out,
			container,
			interfaceValue);

	}

	@Override
	public
	void renderFormRow (
			@NonNull FormatWriter out,
			@NonNull Container container) {

		Optional<Native> nativeValue =
			accessor.read (
				container);

		Optional<Generic> genericValue =
			nativeMapping.nativeToGeneric (
				nativeValue);

		Optional<Interface> interfaceValue =
			interfaceMapping.genericToInterface (
				container,
				genericValue);

		renderer.renderFormRow (
			out,
			container,
			interfaceValue);

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
			@NonNull Container container) {

		Optional<Native> nativeValue =
			accessor.read (
				container);

		Optional<Generic> genericValue =
			nativeMapping.nativeToGeneric (
				nativeValue);

		Optional<Interface> interfaceValue =
			interfaceMapping.genericToInterface (
				container,
				genericValue);

		renderer.renderFormReset (
			javascriptWriter,
			indent,
			container,
			interfaceValue);

	}

	@Override
	public
	void renderCsvRow (
			@NonNull FormatWriter out,
			@NonNull Container container) {

		Optional<Native> nativeValue =
			accessor.read (
				container);

		Optional<Generic> genericValue =
			nativeMapping.nativeToGeneric (
				nativeValue);

		Optional<Interface> interfaceValue =
			interfaceMapping.genericToInterface (
				container,
				genericValue);

		String stringValue =
			interfaceValue.isPresent ()
				? interfaceValue.get ().toString ()
				: "";

		out.writeFormat (
			"\"");

		out.writeFormat (
			"%s",
			stringValue.replace ("\"", "\"\""));

		out.writeFormat (
			"\"");

	}

	@Override
	public
	void update (
			@NonNull Container container,
			@NonNull UpdateResult<Generic,Native> updateResult) {

		List<String> errors =
			new ArrayList<String> ();

		// do nothing if no value present in form

		if (! renderer.formValuePresent ()) {

			updateResult
				.updated (false);

			return;

		}

		// get interface value from form

		Optional<Interface> newInterfaceValue =
			renderer.formToInterface (
				errors);

		if (! errors.isEmpty ()) {

			updateResult

				.updated (
					false)

				.errors (
					errors);

			return;

		}

		// convert to generic

		Optional<Generic> newGenericValue =
			interfaceMapping.interfaceToGeneric (
				container,
				newInterfaceValue,
				errors);

		if (! errors.isEmpty ()) {

			updateResult

				.updated (
					false)

				.errors (
					errors);

			return;

		}

		// perform value validation

		valueValidator.validate (
			newGenericValue,
			errors);

		if (! errors.isEmpty ()) {

			updateResult
				.updated (false)
				.errors (errors);

		}

		// convert to native

		Optional<Native> newNativeValue =
			nativeMapping.genericToNative (
				newGenericValue);

		// check new value

		constraintValidator.validate (
			container,
			newNativeValue,
			errors);

		if (! errors.isEmpty ()) {

			updateResult
				.updated (false)
				.errors (errors);

			return;

		}

		// get the current value, if it is the same, do nothing

		Optional<Native> oldNativeValue =
			accessor.read (
				container);

		Optional<Generic> oldGenericValue =
			nativeMapping.nativeToGeneric (
				oldNativeValue);

		if (
			equal (
				oldGenericValue,
				newGenericValue)
		) {

			updateResult

				.updated (
					false);

			return;

		}

		// set the new value

		accessor.write (
			container,
			newNativeValue);

		updateResult

			.updated (
				true)

			.oldGenericValue (
				oldGenericValue)

			.newGenericValue (
				newGenericValue)

			.oldNativeValue (
				oldNativeValue)

			.newNativeValue (
				newNativeValue);

	}

	@Override
	public
	void runUpdateHook (
			@NonNull UpdateResult<Generic,Native> updateResult,
			@NonNull Container container,
			@NonNull PermanentRecord<?> linkObject,
			@NonNull Optional<Object> objectRef,
			@NonNull Optional<String> objectType) {

		updateHook.onUpdate (
			updateResult,
			container,
			linkObject,
			objectRef,
			objectType);

	}

}
