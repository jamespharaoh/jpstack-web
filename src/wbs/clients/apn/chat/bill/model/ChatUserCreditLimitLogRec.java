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
class ChatUserCreditLimitLogRec
	implements CommonRecord<ChatUserCreditLimitLogRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatUserRec chatUser;

	// details

	@SimpleField
	Date timestamp;

	@SimpleField
	Integer oldCreditLimit;

	@SimpleField
	Integer newCreditLimit;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatUserCreditLimitLogRec> otherRecord) {

		ChatUserCreditLimitLogRec other =
			(ChatUserCreditLimitLogRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
