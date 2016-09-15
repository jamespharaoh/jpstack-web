package wbs.imchat.api;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ImChatServiceData {

	@DataAttribute
	Boolean profilePageBeforeLogin;

	@DataAttribute
	List<ImChatCustomerDetailData> createDetails;

}
