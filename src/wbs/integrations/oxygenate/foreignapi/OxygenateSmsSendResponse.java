package wbs.integrations.oxygenate.foreignapi;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class OxygenateSmsSendResponse {

	String statusCode;
	String statusMessage;
	String messageReferences;

}
