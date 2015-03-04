package wbs.clients.apn.chat.bill.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.LocalDate;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentitySimpleField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatUserSpendRec
	implements CommonRecord<ChatUserSpendRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatUserRec chatUser;

	@IdentitySimpleField
	LocalDate date;

	// data

	@SimpleField
	Integer userMessageCount = 0;

	@SimpleField
	Integer userMessageCharge = 0;

	@SimpleField
	Integer monitorMessageCount = 0;

	@SimpleField
	Integer monitorMessageCharge = 0;

	@SimpleField
	Integer textProfileCount = 0;

	@SimpleField
	Integer textProfileCharge = 0;

	@SimpleField
	Integer imageProfileCount = 0;

	@SimpleField
	Integer imageProfileCharge = 0;

	@SimpleField
	Integer videoProfileCount = 0;

	@SimpleField
	Integer videoProfileCharge = 0;

	@SimpleField
	Integer receivedMessageCount = 0;

	@SimpleField
	Integer receivedMessageCharge = 0;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatUserSpendRec> otherRecord) {

		ChatUserSpendRec other =
			(ChatUserSpendRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChatUser (),
				other.getChatUser ())

			.append (
				getDate (),
				other.getDate ())

			.toComparison ();

	}

	// dao methods

	public static
	interface ChatUserSpendDaoMethods {

		ChatUserSpendRec findByDate (
				ChatUserRec chatUser,
				LocalDate date);

	}

}
