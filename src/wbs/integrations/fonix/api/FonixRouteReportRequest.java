package wbs.integrations.fonix.api;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class FonixRouteReportRequest {

	@DataAttribute (
		name = "IFVERSION",
		required = true)
	String ifVersion;

	@DataAttribute (
		name = "OPERATOR",
		required = true)
	String operator;

	@DataAttribute (
		name = "MONUMBER",
		required = true)
	String moNumber;

	@DataAttribute (
		name = "DESTINATION",
		required = true)
	String destination;

	@DataAttribute (
		name = "STATUSCODE",
		required = true)
	String statusCode;

	@DataAttribute (
		name = "STATUSTEXT",
		required = true)
	String statusText;

	@DataAttribute (
		name = "STATUSTIME",
		required = true)
	String statusTime;

	@DataAttribute (
		name = "PRICE",
		required = true)
	String price;

	@DataAttribute (
		name = "GUID",
		required = true)
	String guid;

	@DataAttribute (
		name = "REQUESTID",
		required = true)
	String requestId;

}
