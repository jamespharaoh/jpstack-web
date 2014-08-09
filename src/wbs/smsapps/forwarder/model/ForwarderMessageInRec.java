package wbs.smsapps.forwarder.model;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ForwarderMessageInRec
	implements CommonRecord<ForwarderMessageInRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField
	ForwarderRec forwarder;

	@ReferenceField
	NumberRec number;

	@ReferenceField
	MessageRec message;

	@SimpleField
	Boolean pending = true;

	@SimpleField (column = "sendqueue")
	Boolean sendQueue;

	@SimpleField (
		nullable = true,
		column = "created_timestamp")
	Date createdTime = new Date ();

	@SimpleField (
		nullable = true,
		column = "retry_timestamp")
	Date retryTime;

	@SimpleField (
		nullable = true,
		column = "processed_timestamp")
	Date processedTime;

	@SimpleField (
		nullable = true,
		column = "borrowed_timestamp")
	Date borrowedTime;

	@SimpleField (nullable = true)
	Date cancelledTime;

	@SimpleField
	Integer billRepliesSent = 0;

	@SimpleField
	Integer freeRepliesSent = 0;

	// compare to

	@Override
	public
	int compareTo (
			Record<ForwarderMessageInRec> otherRecord) {

		ForwarderMessageInRec other =
			(ForwarderMessageInRec) otherRecord;

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
	interface ForwarderMessageInDaoMethods {

		ForwarderMessageInRec findNext (
				ForwarderRec forwarder);

		List<ForwarderMessageInRec> findNexts (
				int maxResults);

		List<ForwarderMessageInRec> findPendingLimit (
				ForwarderRec forwarder,
				int maxResults);

	}

}
