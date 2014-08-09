package wbs.platform.exception.model;

import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity (
	table = "exception_type")
public
class ExceptionLogTypeRec
	implements MajorRecord<ExceptionLogTypeRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@CodeField
	String code;

	// details

	@SimpleField
	String description;

	// children

	@CollectionField (
		key = "type_id")
	Set<ExceptionLogRec> entries;

	// compare to

	@Override
	public
	int compareTo (
			Record<ExceptionLogTypeRec> otherRecord) {

		ExceptionLogTypeRec other =
			(ExceptionLogTypeRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
