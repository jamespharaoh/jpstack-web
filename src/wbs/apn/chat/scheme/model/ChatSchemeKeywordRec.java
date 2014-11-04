package wbs.apn.chat.scheme.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.keyword.model.ChatKeywordJoinType;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
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
class ChatSchemeKeywordRec
	implements EphemeralRecord<ChatSchemeKeywordRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatSchemeRec chatScheme;

	@CodeField
	String keyword;

	// settings

	@SimpleField (
		nullable = true)
	ChatKeywordJoinType joinType;

	@SimpleField (
		nullable = true)
	Gender joinGender;

	@SimpleField (
		nullable = true)
	Orient joinOrient;

	@ReferenceField (
		nullable = true)
	ChatAffiliateRec joinChatAffiliate;

	@ReferenceField (
		nullable = true)
	CommandRec command;

	@SimpleField
	Boolean confirmCharges = false;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatSchemeKeywordRec> otherRecord) {

		ChatSchemeKeywordRec other =
			(ChatSchemeKeywordRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChatScheme (),
				other.getChatScheme ())

			.append (
				getKeyword (),
				other.getKeyword ())

			.toComparison ();

	}

}
