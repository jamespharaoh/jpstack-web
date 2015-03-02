package wbs.clients.apn.chat.tv.core.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class ChatTvOutboxRec
	implements EphemeralRecord<ChatTvOutboxRec> {

	// id

	@ForeignIdField (
		field = "message",
		column = "id")
	Integer id;

	// identity

	@MasterField
	ChatTvMessageRec message;

	// details

	@SimpleField
	Date createdTime;

	// state

	@SimpleField
	String sendingToken;

	@SimpleField
	Date sendingTime;

	@SimpleField
	Integer sendingPid;

	@SimpleField
	String sendingHostname;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatTvOutboxRec> otherRecord) {

		ChatTvOutboxRec other =
			(ChatTvOutboxRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getCreatedTime (),
				getCreatedTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
