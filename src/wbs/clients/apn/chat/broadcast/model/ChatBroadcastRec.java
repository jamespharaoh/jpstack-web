package wbs.clients.apn.chat.broadcast.model;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.LinkField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatBroadcastRec
	implements CommonRecord<ChatBroadcastRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	ChatRec chat;

	@ReferenceField
	UserRec user;

	@SimpleField
	Date timestamp;

	@ReferenceField
	ChatUserRec chatUser;

	@ReferenceField
	TextRec text;

	@ReferenceField
	BatchRec batch;

	@SimpleField
	Integer numberCount;

	@LinkField (
		table = "chat_broadcast_number")
	Set<NumberRec> numbers =
		new HashSet<NumberRec> ();

	@SimpleField
	Boolean search;

	@SimpleField (nullable = true)
	Date searchLastActionFrom;

	@SimpleField (nullable = true)
	Date searchLastActionTo;

	@SimpleField (nullable = true)
	Gender searchGender;

	@SimpleField (nullable = true)
	Orient searchOrient;

	@SimpleField (nullable = true)
	Boolean searchPicture;

	@SimpleField (nullable = true)
	Boolean searchAdult;

	@SimpleField (nullable = true)
	Integer searchSpendMin;

	@SimpleField (nullable = true)
	Integer searchSpendMax;

	@SimpleField
	Boolean includeBlocked;

	@SimpleField
	Boolean includeOptedOut;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatBroadcastRec> otherRecord) {

		ChatBroadcastRec other =
			(ChatBroadcastRec) otherRecord;

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
	interface ChatBroadcastDaoMethods {

		List<ChatBroadcastRec> findRecentWindow (
				ChatRec chat,
				int firstResult,
				int maxResults);

	}

}
