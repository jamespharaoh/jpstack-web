package wbs.integrations.clockworksms.foreignapi;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@DataClass ("Message_Resp")
@Accessors (fluent = true)
@Data
public
class ClockworkSmsMessageResponse {

	@DataChildren (
		direct = true,
		childElement = "SMS_Resp")
	List<SmsResp> smsResp =
		new ArrayList<SmsResp> ();

	@DataChild (
		name = "ErrNo",
		required = false)
	Integer errNo;

	@DataChild (
		name = "ErrDesc",
		required = false)
	String errDesc;

	@DataClass ("SMS_Resp")
	@Accessors (fluent = true)
	@Data
	public static
	class SmsResp {

		@DataChild (
			name = "To",
			required = true)
		String to;

		@DataChild (
			name = "MessageID",
			required = false)
		String messageId;

		@DataChild (
			name = "ErrNo",
			required = false)
		Integer errNo;

		@DataChild (
			name = "ErrDesc",
			required = false)
		String errDesc;

		@DataChild (
			name = "ClientID",
			required = false)
		String clientId;

	}

}
