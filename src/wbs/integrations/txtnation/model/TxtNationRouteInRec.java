package wbs.integrations.txtnation.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.sms.number.format.model.NumberFormatRec;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class TxtNationRouteInRec
	implements MajorRecord<TxtNationRouteInRec> {

	// id

	@ForeignIdField (
		field = "route")
	Integer id;

	// identity

	@MasterField
	RouteRec route;

	// details

	@DeletedField
	Boolean deleted = false;

	// settings

	@ReferenceField
	NumberFormatRec numberFormat;

	// compare to

	@Override
	public
	int compareTo (
			Record<TxtNationRouteInRec> otherRecord) {

		TxtNationRouteInRec other =
			(TxtNationRouteInRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getRoute (),
				other.getRoute ())

			.toComparison ();

	}

}
