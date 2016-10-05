package wbs.apn.chat.bill.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.Range;

import wbs.utils.time.TextualInterval;

@Accessors (fluent = true)
@Data
public 
class ChatRebillLogSearch
	implements Serializable {

	TextualInterval timestamp;

	Long userId;

	TextualInterval lastAction;

	Range <Long> minimumCreditOwed;

	Boolean includeBlocked;
	Boolean includeFailed; 

	Range <Long> numChatUsers;

}
