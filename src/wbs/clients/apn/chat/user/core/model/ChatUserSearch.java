package wbs.clients.apn.chat.user.core.model;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.Range;
import org.joda.time.Interval;

import wbs.clients.apn.chat.bill.model.ChatUserCreditMode;
import wbs.clients.apn.chat.contact.model.ChatMessageMethod;

@Accessors (fluent = true)
@Data
public
class ChatUserSearch
	implements Serializable {

	Integer chatId;

	ChatUserType type;

	String code;

	String numberLike;

	Boolean blockAll;
	Boolean deleted;

	Gender gender;
	Orient orient;
	Integer categoryId;

	String name;
	String info;
	String location;

	Boolean online;

	Boolean hasPicture;
	Boolean hasVideo;

	Boolean adultVerified;

	ChatUserCreditMode creditMode;

	Range<Long> creditFailed;
	Range<Long> creditNoReports;
	Range<Long> valueSinceEver;

	Interval firstJoin;
	Interval lastAction;
	Interval lastJoin;

	ChatUserDateMode datingMode;

	Collection<ChatMessageMethod> deliveryMethodIn;

}
