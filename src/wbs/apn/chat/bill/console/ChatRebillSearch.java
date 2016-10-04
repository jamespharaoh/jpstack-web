package wbs.apn.chat.bill.console;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

@Accessors (fluent = true)
@Data
public
class ChatRebillSearch {

	Instant lastAction;
	Long minimumCreditOwed;
	Boolean includeBlocked = false;
	Boolean includeFailed = false;

}
