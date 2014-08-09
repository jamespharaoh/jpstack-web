package wbs.smsapps.photograbber.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.core.model.MessageRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class PhotoGrabberReissueRec
	implements CommonRecord<PhotoGrabberReissueRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	PhotoGrabberRequestRec photoGrabberRequest;

	// TODO index

	// details

	@SimpleField
	Date timestamp;

	@ReferenceField
	UserRec user;

	@ReferenceField
	MessageRec message;

	// compare to

	@Override
	public
	int compareTo (
			Record<PhotoGrabberReissueRec> otherRecord) {

		PhotoGrabberReissueRec other =
			(PhotoGrabberReissueRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
