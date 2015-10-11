package wbs.clients.apn.chat.scheme.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode
@ToString
@MajorEntity
public
class ChatSchemeChargesRec
	implements MinorRecord<ChatSchemeChargesRec> {

	// id

	@ForeignIdField (
		field = "chatScheme")
	Integer id;

	// identity

	@MasterField
	ChatSchemeRec chatScheme;

	// settings

	@SimpleField
	Integer chargeChatSend = 0;

	@SimpleField
	Integer chargeChatReceive = 0;

	@SimpleField
	Integer chargeChatInfo = 0;

	@SimpleField
	Integer chargeChatPic = 0;

	@SimpleField
	Integer chargeChatPicDiv = 1;

	@SimpleField
	Integer chargeChatVideo = 0;

	@SimpleField
	Integer chargeChatVideoDiv = 1;

	@SimpleField
	Integer creditLimit = 0;

	@SimpleField
	Integer adultVerifyCredit = 0;

	@SimpleField
	Boolean billLimitEnabled = false;

	@SimpleField
	Integer billLimitLow = 0;

	@SimpleField
	Integer billLimitHigh = 0;

	@SimpleField (
		nullable = true)
	Integer spendWarningEvery;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatSchemeChargesRec> otherRecord) {

		ChatSchemeChargesRec other =
			(ChatSchemeChargesRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChatScheme (),
				other.getChatScheme ())

			.toComparison ();

	}

}
