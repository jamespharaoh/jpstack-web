package wbs.clients.apn.chat.keyword.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.sms.command.model.CommandRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class ChatKeywordRec
	implements EphemeralRecord<ChatKeywordRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatRec chat;

	@CodeField
	String keyword;

	// settings

	@ReferenceField (
		nullable = true)
	CommandRec command;

	@SimpleField
	Boolean global = false;

	@SimpleField
	Boolean chatBlock = false;

	@SimpleField
	Boolean chatInfo = false;

	@SimpleField (
		nullable = true)
	ChatKeywordJoinType joinType;

	@SimpleField (
		nullable = true,
		column = "gender")
	Gender joinGender;

	@SimpleField (
		nullable = true,
		column = "orient")
	Orient joinOrient;

	@ReferenceField (
		nullable = true)
	ChatAffiliateRec joinChatAffiliate;

	@SimpleField
	Boolean noCreditCheck = false;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatKeywordRec> otherRecord) {

		ChatKeywordRec other =
			(ChatKeywordRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChat (),
				other.getChat ())

			.append (
				getKeyword (),
				other.getKeyword ())

			.toComparison ();

	}

}
