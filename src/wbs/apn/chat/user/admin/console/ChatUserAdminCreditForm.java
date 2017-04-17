package wbs.apn.chat.user.admin.console;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class ChatUserAdminCreditForm {

	Long currentCredit;

	Long creditAmount;
	Long billAmount;

	String details;

}
