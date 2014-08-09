package wbs.integrations.mig.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class MigRouteInRec
	implements MajorRecord<MigRouteInRec> {

	@ForeignIdField (
		field = "route")
	Integer id;

	@MasterField
	RouteRec route;

	@SimpleField
	Boolean setNetwork;

	@Override
	public
	int compareTo (
			Record<MigRouteInRec> otherRecord) {

		MigRouteInRec other =
			(MigRouteInRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getRoute (),
				other.getRoute ())

			.toComparison ();

	}

}
