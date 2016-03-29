package wbs.clients.apn.chat.user.core.model;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.Range;

import wbs.clients.apn.chat.bill.model.ChatUserCreditMode;
import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.framework.utils.TextualInterval;

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

	Boolean hasCategory;
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

	TextualInterval firstJoin;
	TextualInterval lastAction;
	TextualInterval lastJoin;

	ChatUserDateMode datingMode;

	Collection<ChatMessageMethod> deliveryMethodIn;

}
