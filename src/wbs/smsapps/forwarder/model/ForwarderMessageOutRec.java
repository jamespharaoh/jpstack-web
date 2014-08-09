package wbs.smsapps.forwarder.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CollectionField;
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
class ForwarderMessageOutRec
	implements CommonRecord<ForwarderMessageOutRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField
	ForwarderRec forwarder;

	@ReferenceField (
		nullable = true,
		column = "in_id")
	ForwarderMessageInRec forwarderMessageIn;

	@ReferenceField
	ForwarderRouteRec forwarderRoute;

	@ReferenceField
	NumberRec number;

	@ReferenceField (
		nullable = true)
	MessageRec message;

	@SimpleField (
		column = "created_timestamp")
	Date createdTime = new Date ();

	@SimpleField (
		nullable = true)
	String otherId;

	@SimpleField
	Boolean bill;

	@SimpleField
	Integer reportIndexNext = 0;

	@SimpleField
	Integer reportIndexPending;

	@SimpleField (
		nullable = true)
	Date reportRetryTime;

	@SimpleField
	Integer reportTries;

	@ReferenceField (
		nullable = true)
	ForwarderMessageOutRec nextForwarderMessageOut;

	@ReferenceField (
		nullable = true)
	ForwarderMessageOutRec prevForwarderMessageOut;

	@CollectionField (
		index = "index")
	List<ForwarderMessageOutReportRec> reports =
		new ArrayList<ForwarderMessageOutReportRec> ();

	//Boolean messageReportPending;

	// compare to

	@Override
	public
	int compareTo (
			Record<ForwarderMessageOutRec> otherRecord) {

		ForwarderMessageOutRec other =
			(ForwarderMessageOutRec) otherRecord;

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
	interface ForwarderMessageOutDaoMethods {

		ForwarderMessageOutRec findByOtherId (
				ForwarderRec forwarder,
				String otherId);

		List<ForwarderMessageOutRec> findPendingLimit (
				ForwarderRec forwarder,
				int maxResults);

	}

}
