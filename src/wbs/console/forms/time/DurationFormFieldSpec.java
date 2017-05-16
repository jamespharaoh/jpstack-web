package wbs.console.forms.time;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.forms.time.DurationFormFieldInterfaceMapping.Format;
import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("duration-field")
@PrototypeComponent ("durationFormFieldSpec")
public
class DurationFormFieldSpec
	implements ConsoleSpec {

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
