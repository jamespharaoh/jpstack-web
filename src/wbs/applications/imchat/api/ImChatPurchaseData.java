package wbs.applications.imchat.api;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.annotations.SimpleField;

@Accessors (fluent = true)
@Data
@DataClass
public
class ImChatPurchaseData {

	@DataAttribute
	String token;

	@SimpleField
	Integer price;

	@SimpleField
	Integer value;

}
