package wbs.platform.postgresql.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.AssignedIdField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "oid")
@ToString (of = "oid")
@CommonEntity (
	create = false,
	mutable = false)
public
class PostgresqlClassRec
	implements CommonRecord<PostgresqlClassRec> {

	// id

	@AssignedIdField
	Integer oid;

	// details

	@SimpleField (
		column = "relname")
	String name;

	@SimpleField (
		column = "relkind")
	Character kind;

	// compare to

	@Override
	public
	int compareTo (
			Record<PostgresqlClassRec> otherRecord) {

		PostgresqlClassRec other =
			(PostgresqlClassRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getName (),
				other.getName ())

			.toComparison ();

	}

	@Override
	public
	Integer getId () {
		return oid;
	}

}
