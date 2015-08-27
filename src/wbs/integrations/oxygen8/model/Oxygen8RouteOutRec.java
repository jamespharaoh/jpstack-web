package wbs.integrations.oxygen8.model;

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
class Oxygen8RouteOutRec
	implements MajorRecord<Oxygen8RouteOutRec> {

	// id

	@ForeignIdField (
		field = "route")
	Integer id;

	// identity

	@MasterField
	RouteRec route;

	// settings

	@ReferenceField
	Oxygen8ConfigRec oxygen8Config;

	@SimpleField
	String relayUrl;

	@SimpleField
	String shortcode;

	@SimpleField
	Boolean premium;

	@SimpleField
	String username;

	@SimpleField
	String password;

	@SimpleField (
		nullable = true)
	String campaignId;

	@SimpleField
	Boolean multipart = false;

	// timeouts

	@SimpleField
	Integer connectTimeout = 60;

	@SimpleField
	Integer readTimeout = 60;

	// compare to

	@Override
	public
	int compareTo (
			Record<Oxygen8RouteOutRec> otherRecord) {

		Oxygen8RouteOutRec other =
			(Oxygen8RouteOutRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getRoute (),
				other.getRoute ())

			.toComparison ();

	}

}
