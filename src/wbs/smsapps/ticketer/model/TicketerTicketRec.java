package wbs.smsapps.ticketer.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
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
class TicketerTicketRec
	implements CommonRecord<TicketerTicketRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	TicketerRec ticketer;

	// TODO what?

	// reference

	@ReferenceField
	NumberRec number;

	// details

	@SimpleField
	String ticket;

	@SimpleField
	Date createdTime = new Date ();

	@SimpleField
	Date retrievedTime;

	@SimpleField
	Date expiresTime;

	@ReferenceField
	MessageRec message;

	// compare to

	@Override
	public
	int compareTo (
			Record<TicketerTicketRec> otherRecord) {

		TicketerTicketRec other =
			(TicketerTicketRec) otherRecord;

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
	interface TicketerTicketDaoMethods {

		TicketerTicketRec findByTicket (
				TicketerRec ticketer,
				NumberRec number,
				String ticket);

	}

}
