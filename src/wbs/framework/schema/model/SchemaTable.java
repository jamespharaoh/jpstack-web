package wbs.framework.schema.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class SchemaTable {

	String name;
	SchemaPrimaryKey primaryKey;

	Map<String,SchemaColumn> columns =
		new LinkedHashMap<String,SchemaColumn> ();

	List<SchemaForeignKey> foreignKeys =
		new ArrayList<SchemaForeignKey> ();

	Map<String,SchemaSequence> sequences =
		new HashMap<String,SchemaSequence> ();

	Map<String,SchemaIndex> indexes =
		new HashMap<String,SchemaIndex> ();

	public
	SchemaTable addColumn (
			SchemaColumn schemaColumn) {

		columns.put (
			schemaColumn.name (),
			schemaColumn);

		return this;

	}

	public
	SchemaTable addForeignKey (
			SchemaForeignKey foreignKey) {

		foreignKeys.add (
			foreignKey);

		return this;

	}

	public
	SchemaTable addSequence (
			SchemaSequence schemaSequence) {

		sequences.put (
			schemaSequence.name (),
			schemaSequence);

		return this;

	}

	public
	SchemaTable addIndex (
			SchemaIndex schemaIndex) {

		indexes.put (
			schemaIndex.name (),
			schemaIndex);

		return this;

	}

}
