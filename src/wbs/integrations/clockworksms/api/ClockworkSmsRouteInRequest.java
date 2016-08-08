package wbs.integrations.clockworksms.api;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("MO")
public
class ClockworkSmsRouteInRequest {

	@DataChild (
		name = "Id",
		required = true)
	String id;

	@DataChild (
		name = "To",
		required = true)
	String to;

	@DataChild (
		name = "From",
		required = true)
	String from;

	@DataChild (
		name = "Keyword",
		required = false)
	String keyword;

	@DataChild (
		name = "Content",
		required = true)
	String content;

	@DataChild (
		name = "Network",
		required = false)
	Long network;

}
