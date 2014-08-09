package wbs.smsapps.manualresponder.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

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
import wbs.sms.message.core.model.MessageRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ManualResponderReplyRec
	implements CommonRecord<ManualResponderReplyRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ManualResponderRequestRec manualResponderRequest;

	// TODO index?

	// details

	@SimpleField
	Date timestamp;

	@ReferenceField
	UserRec user;

	@ReferenceField
	TextRec text;

	// children

	@LinkField (
		table = "manual_responder_reply_message",
		index = "index")
	List<MessageRec> messages =
		new ArrayList<MessageRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<ManualResponderReplyRec> otherRecord) {

		ManualResponderReplyRec other =
			(ManualResponderReplyRec) otherRecord;

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
