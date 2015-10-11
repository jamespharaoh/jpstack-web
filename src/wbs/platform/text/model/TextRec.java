package wbs.platform.text.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
//@ToString (of = "id")
@CommonEntity
public
class TextRec
	implements CommonRecord<TextRec> {

	// id

	@GeneratedIdField
	Integer id;

	// details

	@SimpleField
	String text;

	// to string

	// TODO fix this properly
	@Override
	public
	String toString () {
		return text;
	}

	// compare to

	@Override
	public
	int compareTo (
			Record<TextRec> otherRecord) {

		TextRec other =
			(TextRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getText (),
				other.getText ())

			.toComparison ();

	}

}
