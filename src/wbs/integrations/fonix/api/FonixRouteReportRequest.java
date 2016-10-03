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

	@DataAttribute
	String ifVersion;

	@DataAttribute
	String operator;

	@DataAttribute
	String moNumber;

	@DataAttribute
	String destination;

	@DataAttribute
	String statusCode;

	@DataAttribute
	String statusText;

	@DataAttribute
	String statusTime;

	@DataAttribute
	Long price;

	@DataAttribute
	String guid;

}
