package wbs.sms.message.core.console;

import wbs.sms.message.core.model.MessageRec;

public
interface MessageConsolePlugin {

	String getCode ();

	String messageSummaryText (
			MessageRec message);

	String messageSummaryHtml (
			MessageRec message);

}
