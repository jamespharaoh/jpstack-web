package wbs.platform.console.forms;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("currency-field")
@PrototypeComponent ("currencyFormFieldSpec")
@ConsoleModuleData
public
class CurrencyFormFieldSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute ("min")
	Long minimum =
		Long.MIN_VALUE;

	@DataAttribute ("max")
	Long maximum =
		Long.MAX_VALUE;

	@DataAttribute
	Integer size;

	@DataAttribute (
		value = "currency",
		required = true)
	String currencyPath;

	@DataAttribute
	Boolean blankIfZero;

}
