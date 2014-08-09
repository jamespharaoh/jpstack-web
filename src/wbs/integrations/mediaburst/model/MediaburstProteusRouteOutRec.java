package wbs.integrations.mediaburst.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MinorEntity
public
class MediaburstProteusRouteOutRec
	implements MinorRecord<MediaburstProteusRouteOutRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField
	RouteRec route;

	@SimpleField
	String url;

	@SimpleField
	String username;

	@SimpleField
	String password;

	@SimpleField
	String servType;

	@Override
	public
	int compareTo (
			Record<MediaburstProteusRouteOutRec> otherRecord) {

		MediaburstProteusRouteOutRec other =
			(MediaburstProteusRouteOutRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getRoute (),
				other.getRoute ())

			.toComparison ();

	}

}
