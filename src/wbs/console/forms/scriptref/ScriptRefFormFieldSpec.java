package wbs.console.forms.scriptref;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("script-ref")
@PrototypeComponent ("scriptRefFormFieldSpec")
public
class ScriptRefFormFieldSpec
	implements ConsoleModuleData {

	@DataAttribute (
		required = true)
	String path;

}
