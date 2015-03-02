package wbs.clients.apn.chat.bill.model;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Interval;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.user.model.UserRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatUserBillLogRec
	implements CommonRecord<ChatUserBillLogRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField
	ChatUserRec chatUser;

	@SimpleField
	Date timestamp;

	@ReferenceField
	UserRec user;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatUserBillLogRec> otherRecord) {

		ChatUserBillLogRec other =
			(ChatUserBillLogRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	// dao methods

	public static
	interface ChatUserBillLogDaoMethods {

		List<ChatUserBillLogRec> findByTimestamp (
				ChatUserRec chatUser,
				Interval timestampInterval);

	}

}
