package wbs.smsapps.forwarder.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.sms.route.router.model.RouterRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class ForwarderRouteRec
	implements MajorRecord<ForwarderRouteRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ForwarderRec forwarder;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description = "";

	@DeletedField
	Boolean deleted = false;

	// settings

	@ReferenceField (
		nullable = true)
	RouterRec router;

	@SimpleField
	String number = "";

	// compare to

	@Override
	public
	int compareTo (
			Record<ForwarderRouteRec> otherRecord) {

		ForwarderRouteRec other =
			(ForwarderRouteRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getForwarder (),
				other.getForwarder ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
