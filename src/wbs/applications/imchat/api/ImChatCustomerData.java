package wbs.applications.imchat.api;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ImChatCustomerData {

	@DataAttribute
	String code;

	@DataAttribute
	String email;

	@DataAttribute
	Boolean conditionsAccepted;

	@DataAttribute
	Boolean detailsCompleted;

	@DataAttribute
	Long balance;

	@DataAttribute
	String balanceString;

	@DataAttribute
	Long minimumBalance;

	@DataAttribute
	String minimumBalanceString;

	@DataAttribute
	Long requiredBalance;

	@DataAttribute
	String requiredBalanceString;

	@DataAttribute
	Boolean developerMode;

	@DataAttribute
	List<ImChatCustomerDetailData> details;

}
