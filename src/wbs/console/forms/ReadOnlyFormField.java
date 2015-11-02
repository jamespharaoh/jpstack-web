package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.Misc.optionalRequired;
import static wbs.framework.utils.etc.Misc.requiredValue;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.LinkedHashSet;
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
@PrototypeComponent ("readOnlyFormField")
@DataClass ("read-only-form-field")
public
class ReadOnlyFormField<Container,Generic,Native,Interface>
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
	FormFieldInterfaceMapping<Container,Generic,Interface> interfaceMapping;

	@Getter @Setter
	FormFieldInterfaceMapping<Container,Generic,String> csvMapping;

	@Getter @Setter
	FormFieldRenderer<Container,Interface> renderer;

	// implementation

	@Override
	public
	Boolean fileUpload () {
		return renderer.fileUpload ();
	}

	@Override
	public
	void init (
			@NonNull String fieldSetName) {

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
			requiredValue (
				accessor.read (
					container));

		Optional<Generic> genericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					nativeValue));

		Optional<Interface> interfaceValue =
			requiredValue (
				interfaceMapping.genericToInterface (
					container,
					genericValue));

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
			requiredValue (
				accessor.read (
					container));

		Optional<Generic> genericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					nativeValue));

		Optional<Interface> interfaceValue =
			requiredValue (
				interfaceMapping.genericToInterface (
					container,
					genericValue));

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
			requiredValue (
				accessor.read (
					container));

		Optional<Generic> genericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					nativeValue));

		Optional<Interface> interfaceValue =
			requiredValue (
				interfaceMapping.genericToInterface (
					container,
					genericValue));

		renderer.renderTableRow (
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

	}

	@Override
	public
	void renderCsvRow (
			@NonNull FormatWriter out,
			@NonNull Container container) {

		Optional<Native> nativeValue =
			requiredValue (
				accessor.read (
					container));

		Optional<Generic> genericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					nativeValue));

		String csvValue =
			optionalRequired (
				csvMapping.genericToInterface (
					container,
					genericValue));

		out.writeFormat (
			"\"%s\"",
			csvValue.replace ("\"", "\"\""));

	}

	@Override
	public
	void update (
			@NonNull Container container,
			@NonNull UpdateResult<Generic,Native> updateResult) {

		updateResult

			.updated (
				false);

		return;

	}

	@Override
	public
	void runUpdateHook (
			@NonNull UpdateResult<Generic,Native> updateResult,
			@NonNull Container container,
			@NonNull PermanentRecord<?> linkObject,
			@NonNull Optional<Object> objectRef,
			@NonNull Optional<String> objectType) {

		doNothing ();

	}

}
