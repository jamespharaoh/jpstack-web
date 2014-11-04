package wbs.psychic.user.core.model;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.entity.annotations.SlaveField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.queue.model.QueueItemRec;
import wbs.psychic.affiliate.model.PsychicAffiliateRec;
import wbs.psychic.bill.model.PsychicUserAccountRec;
import wbs.psychic.contact.model.PsychicContactRec;
import wbs.psychic.core.model.PsychicRec;
import wbs.psychic.help.model.PsychicHelpRequestRec;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class PsychicUserRec
	implements CommonRecord<PsychicUserRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	PsychicRec psychic;

	@CodeField
	String code;

	@ReferenceField
	NumberRec number;

	@ReferenceField
	PsychicAffiliateRec psychicAffiliate;

	@SimpleField
	Instant createTime;

	@SimpleField (nullable = true)
	Instant firstJoinTime;

	@SimpleField (nullable = true)
	Instant lastJoinTime;

	@SimpleField
	Boolean stopped = false;

	@SimpleField
	Boolean chargesConfirmed = false;

	@SimpleField (nullable = true)
	Integer nextContactIndex;

	@SimpleField
	Integer numHelpRequests = 0;

	@SimpleField
	Integer numHelpResponses = 0;

	@ReferenceField (nullable = true)
	QueueItemRec helpQueueItem;

	@SlaveField
	PsychicUserAccountRec account;

	@CollectionField (
		index = "psychic_profile_id")
	Map<Integer,PsychicContactRec> contactsByProfileId;

	@CollectionField (
		index = "index")
	Map<Integer,PsychicContactRec> contactsByIndex;

	@CollectionField (
		index = "index")
	Map<Integer,PsychicHelpRequestRec> helpRequestsByIndex;

	// compare to

	@Override
	public
	int compareTo (
			Record<PsychicUserRec> otherRecord) {

		PsychicUserRec other =
			(PsychicUserRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getPsychic (),
				other.getPsychic ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

	// object hooks

	public static
	class PsychicUserHooks
		extends AbstractObjectHooks<PsychicUserRec> {

		@Inject
		PsychicUserDao psychicUserDao;

		@Override
		public
		List<Integer> searchIds (
				Object search) {

			PsychicUserSearch psychicUserSearch =
				(PsychicUserSearch) search;

			return psychicUserDao.searchIds (
				psychicUserSearch);

		}

	}

	// dao methods

	public static
	interface PsychicUserDaoMethods {

		PsychicUserRec find (
				PsychicRec psychic,
				NumberRec number);

		List<PsychicUserRec> find (
				NumberRec number);

		List<Integer> searchIds (
				PsychicUserSearch search);

	}

}
