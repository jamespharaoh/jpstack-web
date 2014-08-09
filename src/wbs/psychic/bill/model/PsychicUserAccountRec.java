package wbs.psychic.bill.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.LocalDate;

import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.object.ObjectHelper;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.psychic.user.core.model.PsychicUserRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MinorEntity
public
class PsychicUserAccountRec
	implements MinorRecord<PsychicUserAccountRec> {

	// id

	@ForeignIdField (field = "psychicUser")
	Integer id;

	// identity

	@MasterField
	PsychicUserRec psychicUser;

	// state

	@SimpleField
	PsychicBillMode billMode = PsychicBillMode.normal;

	// statistics

	@SimpleField
	Integer requestCount = 0;

	@SimpleField
	Integer requestSpend = 0;

	@SimpleField
	Integer creditFree = 0;

	@SimpleField
	Integer creditPending = 0;

	@SimpleField
	Integer creditSuccess = 0;

	@SimpleField
	Integer creditFailure = 0;

	@SimpleField
	Integer creditAdminPaid = 0;

	@SimpleField
	Integer creditAdminFree = 0;

	@SimpleField (nullable = true)
	LocalDate dailyDate;

	@SimpleField (nullable = true)
	Integer dailyAmount;

	// compare to

	@Override
	public
	int compareTo (
			Record<PsychicUserAccountRec> otherRecord) {

		PsychicUserAccountRec other =
			(PsychicUserAccountRec) otherRecord;

		return new CompareToBuilder ()

			.append (
					getPsychicUser (),
					other.getPsychicUser ())

			.toComparison ();

	}

	// model helper

	public static
	class PsychicUserAccountHooks
		extends AbstractObjectHooks<PsychicUserAccountRec> {

		@Override
		public
		void createSingletons (
				ObjectHelper<PsychicUserAccountRec> psychicUserAccountHelper,
				ObjectHelper<?> parentHelper,
				Record<?> parent) {

			if (! ((Object) parent instanceof PsychicUserRec))
				return;

			PsychicUserRec psychicUser =
				(PsychicUserRec) (Object)
				parent;

			psychicUserAccountHelper.insert (
				new PsychicUserAccountRec ()
					.setPsychicUser (psychicUser));

		}

	}

}
