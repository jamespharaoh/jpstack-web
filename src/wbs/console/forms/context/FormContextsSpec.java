package wbs.console.forms.context;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.console.module.ConsoleModuleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("form-contexts")
@PrototypeComponent ("formContextsSpec")
public
class FormContextsSpec
	implements ConsoleModuleData {

	// tree attributes

	@DataParent
	ConsoleModuleSpec consoleModule;

	// children

	@DataChildren (
		direct = true)
	List <FormContextSpec> formContexts;


}
