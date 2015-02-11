package wbs.imchat.core.api;

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
	Integer balance;

	@DataAttribute
	List<ImChatPurchaseData> purchases = new ArrayList<ImChatPurchaseData> ();

}
