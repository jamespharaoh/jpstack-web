package wbs.console.forms.time;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("timestamp-field")
@PrototypeComponent ("timestampFormFieldSpec")
public
class TimestampFormFieldSpec
	implements ConsoleModuleData {

	@DataAttribute
	String name;

	@DataAttribute (
		name = "field")
	String fieldName;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	Format format;

	public static
	enum Format {
		timestamp,
		date,
		time;
	}

}
