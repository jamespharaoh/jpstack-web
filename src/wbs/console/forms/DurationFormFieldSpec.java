package wbs.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.forms.DurationFormFieldInterfaceMapping.Format;
import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("duration-field")
@PrototypeComponent ("durationFormFieldSpec")
@ConsoleModuleData
public
class DurationFormFieldSpec {

	@DataAttribute
	String name;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	Format format;

}
