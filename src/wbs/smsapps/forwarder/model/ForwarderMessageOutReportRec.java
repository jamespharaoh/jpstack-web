package wbs.smsapps.forwarder.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.message.core.model.MessageStatus;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ForwarderMessageOutReportRec
	implements CommonRecord<ForwarderMessageOutReportRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	ForwarderMessageOutRec forwarderMessageOut;

	@IndexField
	Integer index;

	@SimpleField
	MessageStatus oldMessageStatus;

	@SimpleField
	MessageStatus newMessageStatus;

	@SimpleField
	Date createdTime;

	@SimpleField (nullable = true)
	Date processedTime;

	// compare to

	@Override
	public
	int compareTo (
			Record<ForwarderMessageOutReportRec> otherRecord) {

		ForwarderMessageOutReportRec other =
			(ForwarderMessageOutReportRec) otherRecord;

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
