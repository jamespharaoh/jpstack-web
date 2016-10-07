package wbs.integrations.fonix.api;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class FonixRouteInRequest {

	@DataAttribute (
		name = "IFVERSION",
		required = true)
	String ifVersion;

	@DataAttribute (
		name = "MONUMBER",
		required = true)
	String moNumber;

	@DataAttribute (
		name = "OPERATOR",
		required = true)
	String operator;

	@DataAttribute (
		name = "DESTINATION",
		required = true)
	String destination;

	@DataAttribute (
		name = "BODY",
		required = true)
	String body;

	@DataAttribute (
		name = "RECEIVETIME",
		required = true)
	String receiveTime;

	@DataAttribute (
		name = "GUID",
		required = true)
	String guid;

}
