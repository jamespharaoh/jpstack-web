package wbs.services.ticket.create;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("set-field")
@PrototypeComponent ("objectTicketCreateSetFieldSpec")
@ConsoleModuleData
public
class ObjectTicketCreateSetFieldSpec {
	
	// attributes
	
	@DataAttribute
	String fieldTypeCode;
	
	@DataAttribute
	String valuePath;

}
