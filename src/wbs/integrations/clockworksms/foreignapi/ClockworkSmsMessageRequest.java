package wbs.integrations.clockworksms.foreignapi;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@DataClass ("Message")
@Data
public
class ClockworkSmsMessageRequest {

	@DataChild
	String key;

	@DataChildren (
		direct = true,
		childElement = "SMS")
	List<Sms> sms =
		new ArrayList<Sms> ();

	@DataClass ("SMS")
	@Accessors (fluent = true)
	@Data
	public static
	class Sms {

		@DataChild (
			name = "To",
			required = true)
		String to;

		@DataChild (
			name = "From",
			required = false)
		String from;

		@DataChild (
			name = "Content",
			required = true)
		String content;

		@DataChild (
			name = "MsgType",
			required = true)
		String msgType;

		@DataChild (
			name = "UDH",
			required = false)
		String udh;

		@DataChild (
			name = "URL",
			required = false)
		String url;

		@DataChild (
			name = "Concat",
			required = false)
		Long concat;

		@DataChild (
			name = "ClientID",
			required = false)
		String clientId;

		@DataChild (
			name = "ExpiryTime",
			required = false)
		Long expiryTime;

		@DataChild (
			name = "DlrType",
			required = false)
		Long dlrType;

		@DataChild (
			name = "DlrUrl",
			required = false)
		String dlrUrl;

		@DataChild (
			name = "DlrContent",
			required = false)
		String dlrContent;

		@DataChild (
			name = "AbsExpiry",
			required = false)
		String absExpiry;

		@DataChild (
			name = "UniqueId",
			required = false)
		Long uniqueId;

		@DataChild (
			name = "InvalidCharAction",
			required = false)
		Long invalidCharAction;

		@DataChild (
			name = "Truncate",
			required = false)
		Long truncate;

	}

}
