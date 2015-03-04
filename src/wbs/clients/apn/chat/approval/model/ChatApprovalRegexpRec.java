package wbs.clients.apn.chat.approval.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class ChatApprovalRegexpRec
	implements EphemeralRecord<ChatApprovalRegexpRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatRec chat;

	// TODO

	// settings

	@SimpleField
	String regexp = "";

	@SimpleField
	Boolean auto = false;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatApprovalRegexpRec> otherRecord) {

		ChatApprovalRegexpRec other =
			(ChatApprovalRegexpRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChat (),
				other.getChat ())

			.append (
				getRegexp (),
				other.getRegexp ())

			.append (
				getId (),
				other.getId ())

			.toComparison ();

	}

}
