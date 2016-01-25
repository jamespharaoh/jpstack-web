package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.Misc.eitherGetLeft;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.requiredSuccess;
import static wbs.framework.utils.etc.Misc.requiredValue;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.LinkedHashSet;
import java.util.Map;
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
	FormFieldNativeMapping<Container,Generic,Native> nativeMapping;

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
					container,
					nativeValue));

		Optional<Interface> interfaceValue =
			requiredValue (
				eitherGetLeft (
					interfaceMapping.genericToInterface (
						container,
						genericValue)));

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
			@NonNull Container container,
			@NonNull Map<String,Object> hints) {

		Optional<Native> nativeValue =
			requiredValue (
				accessor.read (
					container));

		Optional<Generic> genericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					container,
					nativeValue));

		Optional<Interface> interfaceValue =
			requiredValue (
				eitherGetLeft (
					interfaceMapping.genericToInterface (
						container,
						genericValue)));

		renderer.renderTableCellProperties (
			out,
			container,
			hints,
			interfaceValue);

	}

	@Override
	public
	void renderFormRow (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> error,
			@NonNull FormType formType) {

		Optional<Native> nativeValue =
			requiredValue (
				accessor.read (
					container));

		Optional<Generic> genericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					container,
					nativeValue));

		Optional<Interface> interfaceValue =
			requiredValue (
				eitherGetLeft (
					interfaceMapping.genericToInterface (
						container,
						genericValue)));

		renderer.renderTableRow (
			out,
			container,
			hints,
			interfaceValue);

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
			@NonNull Container container,
			@NonNull FormType formType) {

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
					container,
					nativeValue));

		Optional<String> csvValueOptional =
			requiredSuccess (
				csvMapping.genericToInterface (
					container,
					genericValue));

		if (
			isNotPresent (
				csvValueOptional)
		) {

			throw new RuntimeException (
				stringFormat (
					"Missing CSV value for %s.%s",
					container.getClass ().getName (),
					name ()));

		}

		String csvValue =
			csvValueOptional.get ();

		out.writeFormat (
			"\"%s\"",
			csvValue.replace ("\"", "\"\""));

	}

	@Override
	public
	UpdateResult<Generic,Native> update (
			@NonNull FormFieldSubmission submission,
			@NonNull Container container) {

		return new UpdateResult<Generic,Native> ()

			.updated (
				false)

			.error (
				Optional.<String>absent ());

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
