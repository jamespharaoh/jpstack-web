package wbs.framework.schema.tool;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;
import javax.sql.DataSource;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.model.Model;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.schema.builder.SchemaFromModel;
import wbs.framework.schema.helper.SchemaTypesHelper;
import wbs.framework.schema.model.Schema;
import wbs.utils.string.AtomicFileWriter;

public
class SchemaTool {

	// singleton dependencies

	@SingletonDependency
	DataSource dataSource;

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SchemaToSql schemaToSql;

	@SingletonDependency
	SchemaTypesHelper schemaTypesHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SchemaFromModel> schemaFromModel;

	// state

	Schema schema;
	List <String> sqlStatements;

	// implementation

	public
	void schemaCreate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> arguments) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"schemaCreate");

		defineTables (
			taskLogger);

		createSchemaSqlScript ();

		executeSchemaSqlScript (
			taskLogger);

		createObjectTypes (
			taskLogger);

	}

	void defineTables (
			@NonNull TaskLogger taskLogger) {

		schema =
			schemaFromModel.get ()

			.taskLog (
				taskLogger)

			.enumTypes (
				schemaTypesHelper.enumTypes ())

			.modelsByClass (
				entityHelper.modelsByClass ())

			.build ();

		if (taskLogger.errors ()) {

			throw new RuntimeException (
				stringFormat (
					"Aborting due to %s errors",
					integerToDecimalString (
						taskLogger.errorCount ())));

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

	void executeSchemaSqlScript (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"executeSchemaSqlScript");

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

			taskLogger.noticeFormat (
				"Running schema create script");

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

						taskLogger.errorFormatException (
							exception,
							"Error: %s",
							sqlStatement);

					} else if (errors < 100) {

						taskLogger.errorFormat (
							"Error: %s",
							sqlStatement);

					}

					errors ++;

				}

			}

			statement.close ();

			if (errors > 100) {

				taskLogger.errorFormat (
					"Additional %s errors not shown",
					integerToDecimalString (
						errors - 100));

			}

			if (errors > 0) {

				throw new RuntimeException (
					stringFormat (
						"Aborting due to %s errors",
						integerToDecimalString (
							errors)));

			}

			connection.commit ();

			taskLogger.noticeFormat (
				"Schema created successfully");

			connection.close ();

		} catch (SQLException sqlException) {

			throw new RuntimeException (
				sqlException);

		}

	}

	void createObjectTypes (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createObjectTypes");

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

			taskLogger.noticeFormat (
				"Creating object types");

			for (
				Model <?> model
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

			taskLogger.noticeFormat (
				"Object types created successfully");

			connection.close ();

		} catch (SQLException sqlException) {

			throw new RuntimeException (
				sqlException);

		}

	}

}
