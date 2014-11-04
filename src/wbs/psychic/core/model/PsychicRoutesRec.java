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
import wbs.sms.magicnumber.model.MagicNumberSetRec;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.model.RouterRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MinorEntity
public
class PsychicRoutesRec
	implements MinorRecord<PsychicRoutesRec> {

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
	RouteRec billRoute;

	@SimpleField
	String billNumber = "";

	@ReferenceField (
		nullable = true)
	RouterRec freeRouter;

	@ReferenceField (
		nullable = true)
	MagicNumberSetRec magicNumberSet;

	@ReferenceField (
		nullable = true)
	RouterRec magicRouter;

	// compare to

	@Override
	public
	int compareTo (
			Record<PsychicRoutesRec> otherRecord) {

		PsychicRoutesRec other =
			(PsychicRoutesRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getPsychic (),
				other.getPsychic ())

			.toComparison ();

	}

	// object hooks

	public static
	class PsychicRoutesHooks
		extends AbstractObjectHooks<PsychicRoutesRec> {

		@Override
		public
		void createSingletons (
				ObjectHelper<PsychicRoutesRec> psychicRoutesHelper,
				ObjectHelper<?> parentHelper,
				Record<?> parent) {

			if (! ((Object) parent instanceof PsychicRec))
				return;

			PsychicRec psychic =
				(PsychicRec) (Object)
				parent;

			PsychicRoutesRec psychicRoutes =
				psychicRoutesHelper.insert (
					new PsychicRoutesRec ()
						.setPsychic (psychic));

			psychic.setRoutes (psychicRoutes);

		}

	}

}
