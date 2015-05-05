package wbs.platform.media.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class MediaRec
	implements CommonRecord<MediaRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	// TODO?

	// details

	@ReferenceField
	MediaTypeRec mediaType;

	@ReferenceField
	ContentRec content;

	@ReferenceField (
		nullable = true)
	MediaTypeRec thumbMediaType;

	@ReferenceField (
		nullable = true)
	ContentRec thumb100Content;

	@ReferenceField (
		nullable = true)
	ContentRec thumb32Content;

	@SimpleField (
		nullable = true)
	String filename;

	@SimpleField (
		nullable = true)
	String encoding;

	@SimpleField (
		nullable = true)
	Integer width;

	@SimpleField (
		nullable = true)
	Integer height;

	// compare to

	@Override
	public
	int compareTo (
			Record<MediaRec> otherRecord) {

		MediaRec other =
			(MediaRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getFilename (),
				other.getFilename ())

			.append (
				getId (),
				other.getId ())

			.toComparison ();

	}

}
