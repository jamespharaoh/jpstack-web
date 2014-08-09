package wbs.smsapps.photograbber.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.media.model.MediaRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class PhotoGrabberRequestRec
	implements CommonRecord<PhotoGrabberRequestRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	PhotoGrabberRec photoGrabber;

	// details

	@ReferenceField
	NumberRec number;

	@SimpleField
	Integer threadId;

	@ReferenceField
	MessageRec billedMessage;

	@SimpleField
	Boolean found;

	@SimpleField
	Date requestTime;

	@SimpleField
	Date responseTime;

	@SimpleField
	String mediaRef;

	@SimpleField
	String mediaUrl;

	@ReferenceField
	MediaRec media;

	@SimpleField
	String code;

	// children

	@CollectionField (
		orderBy = "timestamp desc")
	Set<PhotoGrabberReissueRec> photoGrabberReissues =
		new HashSet<PhotoGrabberReissueRec> ();

	@Override
	public
	int compareTo (
			Record<PhotoGrabberRequestRec> otherRecord) {

		PhotoGrabberRequestRec other =
			(PhotoGrabberRequestRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getRequestTime (),
				getRequestTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	// dao methods

	public static
	interface PhotoGrabberRequestDaoMethods {

		PhotoGrabberRequestRec findByBilledMessage (
				MessageRec billedMessage);

	}

}
