package wbs.services.ticket.create;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.console.module.ConsoleModuleData;
import wbs.console.module.ConsoleModuleSpec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("object-ticket-create-page")
@PrototypeComponent ("objectTicketCreatePageSpec")
@ConsoleModuleData
public class ObjectTicketCreatePageSpec {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute ("ticket-manager")
	String ticketManager;

	@DataAttribute ("fields-provider")
	String fieldsProviderName;

	@DataAttribute
	String typeCode;

	@DataAttribute
	String name;

	@DataAttribute
	String title;

	@DataAttribute ("tab")
	String tabName;

	@DataAttribute
	String tabLabel;

	@DataAttribute
	String localFile;

	@DataAttribute ("responder")
	String responderName;

	@DataAttribute ("target-context-type")
	String targetContextTypeName;

	@DataAttribute ("target-responder")
	String targetResponderName;

	@DataAttribute
	Boolean hideTab = false;

	@DataAttribute
	String createPrivDelegate;

	@DataAttribute
	String createPrivCode;

	// children

	@DataChildren (
		direct = true)
	List<ObjectTicketCreateSetFieldSpec> ticketFields;

}
