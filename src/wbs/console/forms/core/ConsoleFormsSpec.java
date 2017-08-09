package wbs.console.forms.core;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleSpec;
import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("forms")
@PrototypeComponent ("consoleFormsSpec")
public
class ConsoleFormsSpec
	implements ConsoleSpec {

	// tree attributes

	@DataParent
	ConsoleModuleSpec consoleModule;

	// children

	@DataChildren (
		direct = true)
	List <ConsoleFormSpec> forms;


}
