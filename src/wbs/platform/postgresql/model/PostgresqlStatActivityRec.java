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
@EqualsAndHashCode (of = "processPid")
@ToString (of = "processPid")
@CommonEntity (
	create = false,
	mutable = false,
	table = "pg_stat_activity")
public
class PostgresqlStatActivityRec
	implements CommonRecord<PostgresqlStatActivityRec> {

	@AssignedIdField (
		column = "procpid")
	Integer processPid;

	@SimpleField (
		column = "datname")
	String databaseName;

	@SimpleField (
		column = "usename")
	String userName;

	@SimpleField (
		column = "current_query")
	String currentQuery;

	@Override
	public
	Integer getId () {
		return processPid;
	}

	@Override
	public
	int compareTo (
			Record<PostgresqlStatActivityRec> otherRecord) {

		PostgresqlStatActivityRec other =
			(PostgresqlStatActivityRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getId (),
				other.getId ())

			.toComparison ();

	}

}
