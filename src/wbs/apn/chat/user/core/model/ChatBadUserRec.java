package wbs.apn.chat.user.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatBadUserRec
	implements CommonRecord<ChatBadUserRec> {

	@GeneratedIdField
	Integer id;

	@IdentityReferenceField
	ChatUserRec chatUser;

	@Override
	public
	int compareTo (
			Record<ChatBadUserRec> otherRecord) {

		ChatBadUserRec other =
			(ChatBadUserRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChatUser (),
				other.getChatUser ())

			.toComparison ();

	}

}
