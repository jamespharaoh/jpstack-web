package wbs.platform.media.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class MediaTypeRec
	implements MajorRecord<MediaTypeRec> {

	@GeneratedIdField
	Integer id;

	@CodeField
	String mimeType;

	@SimpleField
	String description;

	@SimpleField
	String extension;

	// compare to

	@Override
	public
	int compareTo (
			Record<MediaTypeRec> otherRecord) {

		MediaTypeRec other =
			(MediaTypeRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getMimeType (),
				other.getMimeType ())

			.toComparison ();

	}

}