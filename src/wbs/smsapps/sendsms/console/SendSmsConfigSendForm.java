package wbs.smsapps.sendsms.console;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class SendSmsConfigSendForm {

	String originator;
	String number;
	String messageBody;

}
