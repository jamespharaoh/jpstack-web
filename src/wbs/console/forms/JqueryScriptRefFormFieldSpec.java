package wbs.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("jquery-script-ref")
@PrototypeComponent ("jqueryScriptRefFormFieldSpec")
@ConsoleModuleData
public
class JqueryScriptRefFormFieldSpec {

}
