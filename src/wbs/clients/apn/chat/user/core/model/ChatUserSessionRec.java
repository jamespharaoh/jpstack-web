package wbs.clients.apn.chat.user.core.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
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
class ChatUserSessionRec
	implements CommonRecord<ChatUserSessionRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	ChatUserRec chatUser;

	@SimpleField
	Date startTime;

	@SimpleField (
		nullable = true)
	Date endTime;

	@SimpleField (
		nullable = true)
	Boolean automatic;

	@Override
	public
	int compareTo (
			Record<ChatUserSessionRec> otherRecord) {

		ChatUserSessionRec other =
			(ChatUserSessionRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getStartTime (),
				getStartTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
