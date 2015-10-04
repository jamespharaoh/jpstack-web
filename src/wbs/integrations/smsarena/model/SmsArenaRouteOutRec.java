package wbs.integrations.smsarena.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.ReferenceField;
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
class SmsArenaRouteOutRec
	implements MajorRecord<SmsArenaRouteOutRec> {

	// id

	@ForeignIdField (
		field = "route")
	Integer id;

	// identity

	@MasterField
	RouteRec route;

	// settings

	@ReferenceField
	SmsArenaConfigRec smsArenaConfig;

	@SimpleField
	String relayUrl;

	@SimpleField
	String authKey;

	@SimpleField (
		nullable = true)
	String cid;

	@SimpleField (
		nullable = true)
	String pid;

	@SimpleField (
		nullable = true)
	String sid;

	@SimpleField (
		nullable = true)
	Integer mclass;

	@SimpleField (
		nullable = true)
	Integer coding;

	@SimpleField (
		nullable = true)
	Integer date;

	@SimpleField (
		nullable = true)
	Integer validity;

	// compare to

	@Override
	public
	int compareTo (
			Record<SmsArenaRouteOutRec> otherRecord) {

		SmsArenaRouteOutRec other =
			(SmsArenaRouteOutRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getRoute (),
				other.getRoute ())

			.toComparison ();

	}

}
