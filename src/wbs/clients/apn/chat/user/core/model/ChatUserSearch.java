package wbs.clients.apn.chat.user.core.model;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.lang3.Range;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.clients.apn.chat.bill.model.ChatUserCreditMode;
import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.framework.utils.TextualInterval;

@Accessors (fluent = true)
@Data
public
class ChatUserSearch
	implements Serializable {

	Long chatId;

	ChatUserType type;
	Collection<ChatUserType> typeIn;

	String code;
	Collection<String> codeIn;

	Long numberId;
	String numberLike;

	Boolean blockAll;
	Boolean barred;
	Boolean deleted;

	Boolean hasGender;
	Gender gender;
	Collection<Gender> genderIn;

	Boolean hasOrient;
	Orient orient;
	Collection<Orient> orientIn;

	Long chatAffiliateId;

	Boolean hasCategory;
	Long categoryId;

	String name;
	String info;
	String location;

	Boolean online;

	Boolean hasPicture;
	Boolean hasVideo;
	Boolean hasAudio;

	Boolean adultVerified;

	ChatUserCreditMode creditMode;

	Range<Long> creditFailed;
	Range<Long> creditNoReports;
	Range<Long> valueSinceEver;

	TextualInterval firstJoin;
	TextualInterval lastAction;
	TextualInterval lastJoin;

	ChatUserDateMode datingMode;
	Boolean hasDatingMode;

	Collection<ChatMessageMethod> deliveryMethodIn;

}
