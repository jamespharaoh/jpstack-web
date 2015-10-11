package wbs.smsapps.manualresponder.model;

import java.util.Date;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ManualResponderRequestRec
	implements CommonRecord<ManualResponderRequestRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ManualResponderRec manualResponder;

	// TODO parent should be mr-number
	// TODO add index

	// details

	@ReferenceField
	MessageRec message;

	@SimpleField
	Date timestamp;

	@ReferenceField (
		nullable = true)
	UserRec user;

	@ReferenceField
	NumberRec number;

	// state

	@SimpleField
	Boolean pending;

	@ReferenceField (
		nullable = true)
	QueueItemRec queueItem;

	// children

	@CollectionField
	Set<ManualResponderReplyRec> replies;

	// compare to

	@Override
	public
	int compareTo (
			Record<ManualResponderRequestRec> otherRecord) {

		ManualResponderRequestRec other =
			(ManualResponderRequestRec) otherRecord;

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
