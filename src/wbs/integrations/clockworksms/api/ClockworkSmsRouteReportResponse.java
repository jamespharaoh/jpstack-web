package wbs.integrations.clockworksms.api;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("DlrList_Resp")
public
class ClockworkSmsRouteReportResponse {

	@DataChildren (
		direct = true,
		childElement = "Dlr_Resp")
	List <Item> items =
		new ArrayList<> ();

	@Accessors (fluent = true)
	@Data
	@DataClass ("Dlr_Resp")
	public static
	class Item {

		@DataChild (
			name = "DlrID",
			required = true)
		String dlrId;

		@DataChild (
			name = "Response",
			required = true)
		String response;

	}

}
