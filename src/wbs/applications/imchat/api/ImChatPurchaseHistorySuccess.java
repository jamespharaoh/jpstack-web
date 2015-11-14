package wbs.applications.imchat.api;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ImChatPurchaseHistorySuccess {

	@DataAttribute
	String status = "success";

	@DataAttribute
	ImChatCustomerData customer;

	@DataAttribute
	List<ImChatPurchaseHistoryData> purchases =
		new ArrayList<ImChatPurchaseHistoryData> ();

}
