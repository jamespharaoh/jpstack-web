package wbs.smsapps.orderer.model;

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
class OrdererOrderRec
	implements CommonRecord<OrdererOrderRec> {

	// id

	@GeneratedIdField
	Integer id;

	// etc

	@ReferenceField
	OrdererRec orderer;

	@ReferenceField
	NumberRec number;

	@ReferenceField
	MessageRec receivedMessage;

	@ReferenceField
	MessageRec billedMessage;

	@SimpleField
	Date receivedTime = new Date ();

	@SimpleField
	Date deliveredTime;

	@SimpleField
	String text;

	@Override
	public
	int compareTo (
			Record<OrdererOrderRec> otherRecord) {

		OrdererOrderRec other =
			(OrdererOrderRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getReceivedTime (),
				getReceivedTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	public static
	interface OrdererOrderDaoMethods {

		List<OrdererOrderRec> find (
				OrdererRec orderer,
				NumberRec number);

	}

}
