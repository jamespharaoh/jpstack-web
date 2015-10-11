package wbs.platform.media.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentitySimpleField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ContentRec
	implements CommonRecord<ContentRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@IdentitySimpleField
	Integer hash;

	@IdentitySimpleField
	Integer i;

	// data

	@SimpleField
	byte[] data;

	// compare to

	@Override
	public
	int compareTo (
			Record<ContentRec> otherRecord) {

		ContentRec other =
			(ContentRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getHash (),
				other.getHash ())

			.append (
				getI (),
				other.getId ())

			.toComparison ();

	}

}
