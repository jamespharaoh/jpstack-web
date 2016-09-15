package wbs.imchat.console;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.imchat.model.ImChatCustomerRec;

@Accessors (fluent = true)
@Data
public
class ImChatCustomerCreditRequest
	implements Serializable {

	transient
	ImChatCustomerRec customer;

	String reason;

	Long creditAmount;
	Long billAmount;

}
