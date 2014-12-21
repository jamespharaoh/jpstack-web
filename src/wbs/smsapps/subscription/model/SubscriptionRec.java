package wbs.smsapps.subscription.model;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.text.model.TextRec;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.model.RouterRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class SubscriptionRec
	implements MajorRecord<SubscriptionRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SliceRec slice;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description = "";

	@DeletedField
	Boolean deleted = false;

	// settings

	@ReferenceField (
		nullable = true)
	RouteRec billedRoute;

	@SimpleField (
		nullable = true)
	String billedNumber;

	@ReferenceField (
		nullable = true)
	TextRec billedMessage;

	@ReferenceField (
		nullable = true)
	RouterRec freeRouter;

	@SimpleField (
		nullable = true)
	String freeNumber = "";

	@SimpleField (
		nullable = true)
	Integer creditsPerBill;

	@SimpleField (
		nullable = true)
	Integer debitsPerSend;

	// messages

	@ReferenceField (
		nullable = true)
	TextRec subscribeMessageText;

	@ReferenceField (
		nullable = true)
	TextRec unsubscribeMessageText;

	// statistics

	@SimpleField
	Integer numSubscribers = 0;

	@SimpleField
	Integer numSendsTotal = 0;

	// children

	@CollectionField (
		where = "active")
	Set<SubscriptionNumberRec> activeSubscriptionNumbers =
		new LinkedHashSet<SubscriptionNumberRec> ();

	@CollectionField
	Set<SubscriptionAffiliateRec> subscriptionAffiliates =
		new LinkedHashSet<SubscriptionAffiliateRec> ();

	@CollectionField
	Set<SubscriptionListRec> subscriptionLists =
		new LinkedHashSet<SubscriptionListRec> ();

	// TODO remove these

	public
	void incNumSubscribers () {
		numSubscribers ++;
	}

	public
	void decNumSubscribers () {
		numSubscribers --;
	}

	// compare to

	@Override
	public
	int compareTo (
			Record<SubscriptionRec> otherRecord) {

		SubscriptionRec other =
			(SubscriptionRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSlice (),
				other.getSlice ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
