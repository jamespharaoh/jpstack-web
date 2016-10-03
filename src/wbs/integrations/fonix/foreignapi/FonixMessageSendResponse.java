package wbs.integrations.fonix.foreignapi;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;

@Accessors (fluent = true)
@Data
public
class FonixMessageSendResponse {

	@DataChild
	Success success;

	@DataChild
	Failure failure;

	@Accessors (fluent = true)
	@Data
	public static
	class Success {

		@DataAttribute
		String txguid;

		@DataAttribute
		String numbers;

		@DataAttribute
		String smsparts;

		@DataAttribute
		String encoding;

	}

	@Accessors (fluent = true)
	@Data
	public static
	class Failure {

		@DataAttribute
		String parameter;

		@DataAttribute
		String failcode;

	}

}
