
package wbs.clients.apn.chat.bill.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatDailyLimitLogRec
	implements CommonRecord<ChatDailyLimitLogRec> {

	@GeneratedIdField
	Integer id;

	@SimpleField (
		column = "limit_hit_time")
	Date time;

	@ReferenceField
	ChatUserRec chatUser;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatDailyLimitLogRec> otherRecord) {

		ChatDailyLimitLogRec other =
			(ChatDailyLimitLogRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getId (),
				other.getId ())

			.toComparison ();

	}

}
