package wbs.clients.apn.chat.broadcast.model;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatBroadcastRec
	implements CommonRecord<ChatBroadcastRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatRec chat;

	// TODO index

	// details

	@ReferenceField
	ChatUserRec chatUser;

	@ReferenceField
	TextRec text;

	// state

	@SimpleField
	ChatBroadcastState state =
		ChatBroadcastState.unsent;

	// various timestamps

	@SimpleField
	Instant createdTime;

	@SimpleField (
		nullable = true)
	Instant scheduledTime;

	@SimpleField (
		nullable = true)
	Instant sentTime;

	@SimpleField (
		nullable = true)
	Instant cancelledTime;

	// various involved users

	@ReferenceField
	UserRec createdUser;

	@ReferenceField (
		nullable = true)
	UserRec sentUser;

	@ReferenceField (
		nullable = true)
	UserRec cancelledUser;

	// statistics about numbers

	@SimpleField
	Integer numRemoved = 0;

	@SimpleField
	Integer numAccepted = 0;

	@SimpleField
	Integer numRejected = 0;

	@SimpleField
	Integer numSent = 0;

	// search details

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
				other.getCreatedTime (),
				getCreatedTime ())

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

		List<ChatBroadcastRec> findSending ();

		List<ChatBroadcastRec> findScheduled (
				Instant now);

	}

}
