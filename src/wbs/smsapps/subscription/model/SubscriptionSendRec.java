package wbs.smsapps.subscription.model;

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
import wbs.platform.user.model.UserRec;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.template.model.TemplateRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class SubscriptionSendRec
	implements CommonRecord<SubscriptionSendRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ReferenceField
	SubscriptionRec subscription;

	// TODO?

	// details

	@SimpleField
	Date timestamp = new Date ();

	@ReferenceField
	TemplateRec template;

	@ReferenceField
	UserRec user;

	@ReferenceField
	UserRec creatorUser;

	@ReferenceField
	UserRec senderUser;

	@ReferenceField
	UserRec cancellerUser;

	@ReferenceField
	BatchRec batch;

	@SimpleField
	Date createdTime;

	@SimpleField
	Date sentTime;

	@SimpleField
	Date cancelledTime;

	@SimpleField
	Date scheduledTime;

	@SimpleField
	Date scheduledForTime;

	// statistics

	@SimpleField
	Integer numParts;

	@SimpleField
	Integer numRecipients;

	// state

	@SimpleField
	SubscriptionStatus status;

	// children

	@CollectionField (
		index = "i")
	List<SubscriptionSendPartRec> parts;

	// compare to

	@Override
	public
	int compareTo (
			Record<SubscriptionSendRec> otherRecord) {

		SubscriptionSendRec other =
			(SubscriptionSendRec) otherRecord;

		return new CompareToBuilder ()
			.append (getSubscription (), other.getSubscription ())
			.append (other.getTimestamp (), getTimestamp ())
			.append (other.getId (), getId ())
			.toComparison ();

	}

	// dao methods

	public static
	interface SubscriptionSendDaoMethods {

		SubscriptionSendRec findDue ();

	}

}
