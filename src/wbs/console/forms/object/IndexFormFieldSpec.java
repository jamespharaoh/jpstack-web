package wbs.console.forms.object;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("index-field")
@PrototypeComponent ("indexFormFieldSpec")
@ConsoleModuleData
public
class IndexFormFieldSpec {

	@DataAttribute
	String name;

	@DataAttribute
	String label;

}
