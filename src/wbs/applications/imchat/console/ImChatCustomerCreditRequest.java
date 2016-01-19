package wbs.applications.imchat.console;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.applications.imchat.model.ImChatCustomerRec;

@Accessors (fluent = true)
@Data
public
class ImChatCustomerCreditRequest
	implements Serializable {

	Integer customerId;

	transient
	ImChatCustomerRec customer;

	String reason;

	Long creditAmount;
	Long billAmount;

}
