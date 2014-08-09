package wbs.apn.chat.tv.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatTvUserRec
	implements CommonRecord<ChatTvUserRec> {

	// id

	@ForeignIdField (
		field = "chatUser")
	Integer id;

	// identity

	@MasterField
	ChatUserRec chatUser;

	// statistics

	@SimpleField (
		column = "to_screen_message_count")
	Integer toScreenLegacyCount = 0;

	@SimpleField (
		column = "to_screen_message_charge")
	Integer toScreenLegacyCharge = 0;

	@SimpleField
	Integer toScreenTextCount = 0;

	@SimpleField
	Integer toScreenTextCharge = 0;

	@SimpleField
	Integer toScreenTextFree = 0;

	@SimpleField
	Integer toScreenPhotoCount = 0;

	@SimpleField
	Integer toScreenPhotoCharge = 0;

	@SimpleField
	Integer toScreenPhotoFree = 0;

	@Override
	public
	int compareTo (
			Record<ChatTvUserRec> otherRecord) {

		ChatTvUserRec other =
			(ChatTvUserRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChatUser (),
				other.getChatUser ())

			.toComparison ();

	}

}
