package wbs.platform.event.fixture;

import static wbs.framework.utils.etc.Misc.codify;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.ModelMetaBuilderHandler;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.platform.event.metamodel.EventTypeSpec;

@PrototypeComponent ("eventTypeBuilder")
@ModelMetaBuilderHandler
public
class EventTypeBuilder {

	// dependencies

	@Inject
	DataSource dataSource;

	@Inject
	EntityHelper entityHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	EventTypeSpec spec;

	@BuilderTarget
	Model model;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		try {

			createEventType ();

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating event type %s",
					codify (
						spec.name ())),
				exception);

		}

	}

	private
	void createEventType ()
		throws SQLException {

		@Cleanup
		Connection connection =
			dataSource.getConnection ();

		connection.setAutoCommit (
			false);

		@Cleanup
		PreparedStatement nextEventTypeIdStatement =
			connection.prepareStatement (
				stringFormat (
					"SELECT ",
						"nextval ('event_type_id_seq')"));

		ResultSet eventTypeIdResultSet =
			nextEventTypeIdStatement.executeQuery ();

		eventTypeIdResultSet.next ();

		int eventTypeId =
			eventTypeIdResultSet.getInt (
				1);

		@Cleanup
		PreparedStatement insertEventTypeStatement =
			connection.prepareStatement (
				stringFormat (
					"INSERT INTO event_type (",
						"id, ",
						"code, ",
						"description) ",
					"VALUES (",
						"?, ",
						"?, ",
						"?)"));

		insertEventTypeStatement.setInt (
			1,
			eventTypeId);

		insertEventTypeStatement.setString (
			2,
			codify (
				spec.name ()));

		insertEventTypeStatement.setString (
			3,
			spec.text ());

		insertEventTypeStatement.executeUpdate ();

		connection.commit ();

	}

}
