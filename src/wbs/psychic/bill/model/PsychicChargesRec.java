package wbs.psychic.bill.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.object.ObjectHelper;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.psychic.core.model.PsychicRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MinorEntity
public
class PsychicChargesRec
	implements MinorRecord<PsychicChargesRec> {

	@ForeignIdField (
		field = "psychic")
	Integer id;

	@MasterField
	PsychicRec psychic;

	@SimpleField
	Integer creditLimit = 0;

	@SimpleField
	Integer dailyLimit = 0;

	@SimpleField
	Integer requestCharge = 0;

	@SimpleField
	Integer initialCredit = 0;

	// compare to

	@Override
	public
	int compareTo (
			Record<PsychicChargesRec> otherRecord) {

		PsychicChargesRec other =
			(PsychicChargesRec) otherRecord;

		return new CompareToBuilder ()
			.append (getPsychic (), other.getPsychic ())
			.toComparison ();

	}

	// object hooks

	public static
	class PsychicChargesHooks
		extends AbstractObjectHooks<PsychicChargesRec> {

		@Override
		public
		void createSingletons (
				ObjectHelper<PsychicChargesRec> psychicChargesHelper,
				ObjectHelper<?> parentHelper,
				Record<?> parent) {

			if (! ((Object) parent instanceof PsychicRec))
				return;

			PsychicRec psychic =
				(PsychicRec) (Object)
				parent;

			PsychicChargesRec psychicCharges =
				psychicChargesHelper.insert (
					new PsychicChargesRec ()
						.setPsychic (psychic));

			psychic
				.setCharges (psychicCharges);

		}

	}

}
