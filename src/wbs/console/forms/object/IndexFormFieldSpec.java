package wbs.console.forms.object;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("index-field")
@PrototypeComponent ("indexFormFieldSpec")
public
class IndexFormFieldSpec
	implements ConsoleSpec {

	@DataAttribute
	String name;

	@DataAttribute
	String label;

}
