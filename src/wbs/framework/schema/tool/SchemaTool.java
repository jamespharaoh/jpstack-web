package wbs.framework.schema.tool;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.logging.TaskLog;
import wbs.framework.schema.builder.SchemaFromModel;
import wbs.framework.schema.helper.SchemaTypesHelper;
import wbs.framework.schema.model.Schema;

@Log4j
public
class SchemaTool {

	@Inject
	DataSource dataSource;

	@Inject
	EntityHelper entityHelper;

	@Inject
	SchemaToSql schemaToSql;

	@Inject
	SchemaTypesHelper schemaTypesHelper;

	@Inject
	Provider<SchemaFromModel> schemaFromModel;

	public
	void schemaCreate (
			List<String> args)
		throws Exception {

		TaskLog taskLog =
			new TaskLog ()
				.log (log);

		// create schema

		Schema schema =
			schemaFromModel.get ()

			.taskLog (
				taskLog)

			.enumTypes (
				schemaTypesHelper.enumTypes ())

			.modelsByClass (
				entityHelper.modelsByClass ())

			.build ();

		if (taskLog.errors () > 0) {

			throw new RuntimeException (
				stringFormat (
					"Aborting due to %s errors",
					taskLog.errors ()));

		}

		// create sql

		List<String> sqlStatements =
			new ArrayList<String> ();

		schemaToSql.forSchema (
			sqlStatements,
			schema);

		// write sql

		@Cleanup
		Writer fileWriter =
			new FileWriter (
				new File (
					"work/schema.sql"));

		for (String sqlStatement
				: sqlStatements) {

			fileWriter.write (sqlStatement);
			fileWriter.write (";\n");

		}

		fileWriter.close ();

		// execute sql

		@Cleanup
		Connection connection =
			dataSource.getConnection ();

		connection.setAutoCommit (
			false);

		@Cleanup
		Statement statement =
			connection.createStatement ();

		log.info (
			stringFormat (
				"Running schema create script"));

		int errors = 0;

		for (String sqlStatement
				: sqlStatements) {

			try {

				statement.execute (
					sqlStatement);

			} catch (Exception exception) {

				if (errors == 0) {

					log.error (
						stringFormat (
							"Error: %s",
							sqlStatement),
						exception);

				} else if (errors < 100) {

					log.error (
						stringFormat (
							"Error: %s",
							sqlStatement));

				}

				errors ++;

			}

		}

		statement.close ();

		if (errors > 100) {

			log.error (
				stringFormat (
					"Additional %s errors not shown",
					errors - 100));

		}

		if (errors > 0) {

			throw new RuntimeException (
				stringFormat (
					"Aborting due to %s errors",
					errors));

		}

		connection.commit ();

		log.info (
			stringFormat (
				"Schema created successfully"));

		connection.close ();

	}

}
