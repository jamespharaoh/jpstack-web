package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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
			String fieldSetName) {

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
			FormatWriter out,
			Container container,
			boolean link) {

		Native nativeValue =
			accessor.read (
				container);

		Generic genericValue =
			nativeMapping.nativeToGeneric (
				nativeValue);

		Interface interfaceValue =
			interfaceMapping.genericToInterface (
				container,
				genericValue);

		renderer.renderTableCellList (
			out,
			container,
			interfaceValue,
			link);

	}

	@Override
	public
	void renderTableCellProperties (
			FormatWriter out,
			Container container) {

		Native nativeValue =
			accessor.read (
				container);

		Generic genericValue =
			nativeMapping.nativeToGeneric (
				nativeValue);

		Interface interfaceValue =
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
			FormatWriter out,
			Container container) {

		Native nativeValue =
			accessor.read (
				container);

		Generic genericValue =
			nativeMapping.nativeToGeneric (
				nativeValue);

		Interface interfaceValue =
			interfaceMapping.genericToInterface (
				container,
				genericValue);

		renderer.renderTableRow (
			out,
			container,
			interfaceValue);

	}

	@Override
	public
	void renderCsvRow (
			FormatWriter out,
			Container container) {

		Native nativeValue =
			accessor.read (
				container);

		Generic genericValue =
			nativeMapping.nativeToGeneric (
				nativeValue);

		Interface interfaceValue =
			interfaceMapping.genericToInterface (
				container,
				genericValue);

		String stringValue =
			interfaceValue.toString ();

		out.writeFormat (
			"\"%s\"",
			stringValue.replace ("\"", "\"\""));

	}

	@Override
	public
	void update (
			Container container,
			UpdateResult<Generic,Native> updateResult) {

		updateResult

			.updated (
				false);

		return;

	}

	@Override
	public
	void runUpdateHook (
			UpdateResult<Generic,Native> updateResult,
			Container container,
			PermanentRecord<?> linkObject,
			Object objectRef,
			String objectType) {

	}



}
