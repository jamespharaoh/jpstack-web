package wbs.framework.schema.tool;

import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.model.Model;
import wbs.framework.logging.TaskLogger;
import wbs.framework.schema.builder.SchemaFromModel;
import wbs.framework.schema.helper.SchemaTypesHelper;
import wbs.framework.schema.model.Schema;
import wbs.framework.utils.formatwriter.AtomicFileWriter;

@Log4j
public
class SchemaTool {

	// dependencies

	@Inject
	DataSource dataSource;

	@Inject
	EntityHelper entityHelper;

	@Inject
	SchemaToSql schemaToSql;

	@Inject
	SchemaTypesHelper schemaTypesHelper;

	// prototype dependencies

	@Inject
	Provider<SchemaFromModel> schemaFromModel;

	// state

	TaskLogger taskLog;
	Schema schema;
	List<String> sqlStatements;

	// implementation

	public
	void schemaCreate (
			List <String> args) {

		taskLog =
			new TaskLogger (
				log);

		defineTables ();
		createSchemaSqlScript ();
		executeSchemaSqlScript ();
		createObjectTypes ();

	}

	void defineTables () {

		schema =
			schemaFromModel.get ()

			.taskLog (
				taskLog)

			.enumTypes (
				schemaTypesHelper.enumTypes ())

			.modelsByClass (
				entityHelper.modelsByClass ())

			.build ();

		if (taskLog.errors ()) {

			throw new RuntimeException (
				stringFormat (
					"Aborting due to %s errors",
					taskLog.errorCount ()));

		}

	}

	void createSchemaSqlScript () {

		sqlStatements =
			new ArrayList <String> ();

		schemaToSql.forSchema (
			sqlStatements,
			schema);

		// write sql

		@Cleanup
		AtomicFileWriter fileWriter =
			new AtomicFileWriter (		
				"work/schema.sql");

		for (
			String sqlStatement
				: sqlStatements
		) {

			fileWriter.writeString (
				sqlStatement);

			fileWriter.writeString (
				";\n");

		}

		fileWriter.close ();

	}

	void executeSchemaSqlScript () {

		try {

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
	
			for (
				String sqlStatement
					: sqlStatements
			) {
	
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

		} catch (SQLException sqlException) {

			throw new RuntimeException (
				sqlException);

		}

	}

	void createObjectTypes () {

		try {

			@Cleanup
			Connection connection =
				dataSource.getConnection ();
	
			connection.setAutoCommit (
				false);
	
			@Cleanup
			PreparedStatement nextObjectTypeIdStatement =
				connection.prepareStatement (
					stringFormat (
						"SELECT ",
							"nextval ('object_type_id_seq')"));
	
			@Cleanup
			PreparedStatement insertObjectTypeStatement =
				connection.prepareStatement (
					stringFormat (
						"INSERT INTO object_type (",
							"id, ",
							"code) ",
						"VALUES (",
							"?, ",
							"?)"));
	
			log.info (
				"Creating object types");
	
			for (
				Model model
					: entityHelper.models ()
			) {
	
				int objectTypeId;
	
				if (
					stringEqualSafe (
						model.objectTypeCode (),
						"root")
				) {
	
					objectTypeId = 0;
	
				} else {
	
					ResultSet objectTypeIdResultSet =
						nextObjectTypeIdStatement.executeQuery ();
	
					objectTypeIdResultSet.next ();
	
					objectTypeId =
						objectTypeIdResultSet.getInt (
							1);
	
				}
	
				insertObjectTypeStatement.setInt (
					1,
					objectTypeId);
	
				insertObjectTypeStatement.setString (
					2,
					model.objectTypeCode ());
	
				insertObjectTypeStatement.executeUpdate ();
	
			}
	
			connection.commit ();
	
			log.info (
				"Object types created successfully");
	
			connection.close ();

		} catch (SQLException sqlException) {

			throw new RuntimeException (
				sqlException);

		}

	}

}
