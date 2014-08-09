package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.PermanentRecord;
import wbs.platform.console.html.ScriptRef;

@Accessors (fluent = true)
@PrototypeComponent ("readOnlyFormField")
public
class ReadOnlyFormField<Container,Generic,Native,Interface>
	implements FormField<Container,Generic,Native,Interface> {

	// properties

	@Getter
	Boolean virtual = false;

	@Getter @Setter
	String name;

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
	void renderTableCell (
			PrintWriter out,
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

		renderer.renderTableCell (
			out,
			container,
			interfaceValue,
			link);

	}

	@Override
	public
	void renderFormRow (
			PrintWriter out,
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
			interfaceValue,
			false);

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
