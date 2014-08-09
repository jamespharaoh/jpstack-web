package wbs.apn.chat.tv.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.LocalDate;

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
class ChatTvUserSpendRec
	implements CommonRecord<ChatTvUserSpendRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField (
		column = "chat_user_id")
	ChatTvUserRec chatTvUser;

	@IdentitySimpleField
	LocalDate date;

	// statistics

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

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatTvUserSpendRec> otherRecord) {

		ChatTvUserSpendRec other =
			(ChatTvUserSpendRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChatTvUser (),
				other.getChatTvUser ())

			.append (
				other.getDate (),
				getDate ())

			.toComparison ();

	}

}
