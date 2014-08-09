package wbs.psychic.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.object.ObjectHelper;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.psychic.affiliate.model.PsychicAffiliateRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MinorEntity
public
class PsychicSettingsRec
	implements MinorRecord<PsychicSettingsRec> {

	// id

	@ForeignIdField (
		field = "psychic")
	Integer id;

	// identity

	@MasterField
	PsychicRec psychic;

	// settings

	@ReferenceField (
		nullable = true)
	PsychicAffiliateRec defaultPsychicAffiliate;

	@SimpleField
	Integer requestPreferredQueueTime = 0;

	@SimpleField
	Integer helpPreferredQueueTime = 0;

	// compare to

	@Override
	public
	int compareTo (
			Record<PsychicSettingsRec> otherRecord) {

		PsychicSettingsRec other =
			(PsychicSettingsRec) otherRecord;

		return new CompareToBuilder ()
			.append (getPsychic (), other.getPsychic ())
			.toComparison ();

	}

	// object hooks

	public static
	class PsychicSettingsHooks
		extends AbstractObjectHooks<PsychicSettingsRec> {

		@Override
		public
		void createSingletons (
				ObjectHelper<PsychicSettingsRec> psychicSettingsHelper,
				ObjectHelper<?> parentHelper,
				Record<?> parent) {

			if (! ((Object) parent instanceof PsychicRec))
				return;

			PsychicRec psychic =
				(PsychicRec) (Object)
				parent;

			PsychicSettingsRec psychicSettings =
				psychicSettingsHelper.insert (
					new PsychicSettingsRec ()
						.setPsychic (psychic));

			psychic
				.setSettings (psychicSettings);

		}

	}

}
