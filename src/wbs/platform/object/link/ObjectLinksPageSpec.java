package wbs.platform.object.link;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.console.module.ConsoleModuleSpec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("object-links-page")
@PrototypeComponent ("objectLinksPageSpec")
@ConsoleModuleData
public
class ObjectLinksPageSpec {

	// attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		name = "links-field",
		required = true)
	String linksFieldName;

	@DataAttribute (
		name = "target-links-field",
		required = true)
	String targetLinksFieldName;

	@DataAttribute (
		required = true)
	String addEventName;

	@DataAttribute (
		required = true)
	String removeEventName;

	@DataAttribute (
		required = true)
	ObjectLinksAction.EventOrder eventOrder;

	@DataAttribute (
		name = "signal")
	String updateSignalName;

	@DataAttribute (
		name = "target-signal")
	String targetUpdateSignalName;

	@DataAttribute (required = true)
	String successNotice;

	@DataAttribute (
		name = "fields")
	String fieldsName;

	@DataAttribute (
		required = true)
	OwnedBy ownedBy;

	public static
	enum OwnedBy {
		us,
		them;
	}

}
