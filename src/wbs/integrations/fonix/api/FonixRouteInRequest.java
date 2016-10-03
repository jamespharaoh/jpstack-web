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

	@DataAttribute
	String ifVersion;

	@DataAttribute
	String moNumber;

	@DataAttribute
	String operator;

	@DataAttribute
	String destination;

	@DataAttribute
	String body;

	@DataAttribute
	String receiveTime;

	@DataAttribute
	String guid;

}
