package wbs.psychic.user.admin.console;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (chain = true)
@Data
public
class PsychicUserAdminCreditForm {
	Integer creditAmount;
	Integer paymentAmount;
	String details;
}
