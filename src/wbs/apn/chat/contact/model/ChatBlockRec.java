package wbs.apn.chat.contact.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class ChatBlockRec
	implements EphemeralRecord<ChatBlockRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField
	ChatUserRec chatUser;

	@ReferenceField
	ChatUserRec blockedChatUser;

	@SimpleField
	Date timestamp =
		new Date ();

	@Override
	public
	int compareTo (
			Record<ChatBlockRec> otherRecord) {

		ChatBlockRec other =
			(ChatBlockRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.toComparison ();

	}

	public static
	interface ChatBlockDaoMethods {

		ChatBlockRec find (
				ChatUserRec chatUser,
				ChatUserRec blockedChatUser);

	}

}
