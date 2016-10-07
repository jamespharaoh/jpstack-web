package wbs.console.forms;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("group")
@PrototypeComponent ("formFieldGroupSpec")
@ConsoleModuleData
public
class FormFieldGroupSpec {

	@DataAttribute
	String name;

	@DataAttribute
	String label;

	@DataChildren (
		direct = true)
	List <Object> children;

}
