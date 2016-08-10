package wbs.integrations.clockworksms.api;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("DlrList")
public
class ClockworkSmsRouteReportRequest {

	@DataChildren (
		direct = true,
		childElement = "Dlr")
	List<Item> items =
		new ArrayList<> ();

	@Accessors (fluent = true)
	@Data
	@DataClass ("Dlr")
	public static
	class Item {

		@DataAttribute (
			name = "type")
		String type;

		@DataChild (
			name = "DlrID",
			required = true)
		String dlrId;

		@DataChild (
			name = "ClientID",
			required = false)
		String clientId;

		@DataChild (
			name = "MsgID",
			required = true)
		String messageId;

		@DataChild (
			name = "Status",
			required = true)
		String status;

		@DataChild (
			name = "DestAddr",
			required = true)
		String destAddr;

		@DataChild (
			name = "ErrCode",
			required = true)
		String errCode;

		@DataChild (
			name = "SrcAddr",
			required = true)
		String srcAddr;

		@DataChild (
			name = "SubmitDate",
			required = true)
		String submitDate;

		@DataChild (
			name = "StatusDate",
			required = true)
		String statusDate;

	}

}
