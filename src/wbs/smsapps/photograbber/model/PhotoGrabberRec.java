package wbs.smsapps.photograbber.model;

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
import wbs.platform.scaffold.model.SliceRec;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class PhotoGrabberRec
	implements MajorRecord<PhotoGrabberRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SliceRec slice;

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

	@SimpleField
	String url = "";

	@ReferenceField (
		nullable = true)
	RouteRec billRoute;

	@SimpleField
	String billNumber = "";

	@SimpleField
	String billTemplate = "Please keep this message: {code}";

	@ReferenceField (
		nullable = true)
	RouteRec mmsRoute;

	@SimpleField
	String mmsNumber = "";

	@SimpleField
	String mmsTemplate = "";

	@SimpleField
	String mmsSubject = "";

	@SimpleField
	Boolean jpeg = false;

	@SimpleField
	Integer jpegWidth = 0;

	@SimpleField
	Integer jpegHeight = 0;

	@SimpleField
	Integer jpegQuality = 90;

	// compare to

	@Override
	public
	int compareTo (
			Record<PhotoGrabberRec> otherRecord) {

		PhotoGrabberRec other =
			(PhotoGrabberRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSlice (),
				other.getSlice ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
