package wbs.platform.queue.fixture;

import static wbs.framework.utils.etc.Misc.camelToUnderscore;
import static wbs.framework.utils.etc.Misc.codify;
import static wbs.framework.utils.etc.Misc.ifNull;
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
import wbs.platform.queue.metamodel.QueueTypeSpec;

@PrototypeComponent ("queueTypeBuilder")
@ModelMetaBuilderHandler
public
class QueueTypeBuilder {

	// dependencies

	@Inject
	DataSource dataSource;

	@Inject
	EntityHelper entityHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	QueueTypeSpec spec;

	@BuilderTarget
	Model model;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		try {

			createQueueType ();

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating queue type %s.%s",
					camelToUnderscore (
						ifNull (
							spec.subject (),
							parent.name ())),
					codify (
						spec.name ())),
				exception);

		}

	}

	private
	void createQueueType ()
		throws SQLException {

		@Cleanup
		Connection connection =
			dataSource.getConnection ();

		connection.setAutoCommit (
			false);

		// allocate id

		@Cleanup
		PreparedStatement nextQueueTypeIdStatement =
			connection.prepareStatement (
				stringFormat (
					"SELECT ",
						"nextval ('queue_type_id_seq')"));

		ResultSet queueTypeIdResultSet =
			nextQueueTypeIdStatement.executeQuery ();

		queueTypeIdResultSet.next ();

		int queueTypeId =
			queueTypeIdResultSet.getInt (
				1);

		// lookup parent

		String parentTypeCode =
			ifNull (
				spec.parent (),
				parent.name ());

		Model parentModel =
			entityHelper.modelsByName ().get (
				parentTypeCode);

		if (parentModel == null) {

			throw new RuntimeException (
				stringFormat (
					"No model for %s",
					parentTypeCode));

		}

		@Cleanup
		PreparedStatement getObjectTypeIdStatement =
			connection.prepareStatement (
				stringFormat (
					"SELECT id ",
					"FROM object_type ",
					"WHERE code = ?"));

		getObjectTypeIdStatement.setString (
			1,
			parentModel.objectTypeCode ());

		ResultSet getObjectTypeIdResult =
			getObjectTypeIdStatement.executeQuery ();

		getObjectTypeIdResult.next ();

		int parentObjectTypeId =
			getObjectTypeIdResult.getInt (
				1);

		// lookup subject

		Model subjectModel =
			entityHelper.modelsByName ().get (
				spec.subject ());

		if (subjectModel == null) {

			throw new RuntimeException (
				stringFormat (
					"No model for %s",
					spec.subject ()));

		}

		getObjectTypeIdStatement.setString (
			1,
			subjectModel.objectTypeCode ());

		getObjectTypeIdResult =
			getObjectTypeIdStatement.executeQuery ();

		getObjectTypeIdResult.next ();

		int subjectObjectTypeId =
			getObjectTypeIdResult.getInt (
				1);

		// lookup ref

		Model refModel =
			entityHelper.modelsByName ().get (
				spec.ref ());

		if (refModel == null) {

			throw new RuntimeException (
				stringFormat (
					"No model for %s",
					spec.ref ()));

		}

		getObjectTypeIdStatement.setString (
			1,
			refModel.objectTypeCode ());

		getObjectTypeIdResult =
			getObjectTypeIdStatement.executeQuery ();

		getObjectTypeIdResult.next ();

		int refObjectTypeId =
			getObjectTypeIdResult.getInt (
				1);

		// create queue type

		@Cleanup
		PreparedStatement insertQueueTypeStatement =
			connection.prepareStatement (
				stringFormat (
					"INSERT INTO queue_type (",
						"id, ",
						"parent_object_type_id, ",
						"code, ",
						"description, ",
						"subject_object_type_id, ",
						"ref_object_type_id) ",
					"VALUES (",
						"?, ",
						"?, ",
						"?, ",
						"?, ",
						"?, ",
						"?)"));

		insertQueueTypeStatement.setInt (
			1,
			queueTypeId);

		insertQueueTypeStatement.setInt (
			2,
			parentObjectTypeId);

		insertQueueTypeStatement.setString (
			3,
			codify (
				spec.name ()));

		insertQueueTypeStatement.setString (
			4,
			spec.description ());

		insertQueueTypeStatement.setInt (
			5,
			subjectObjectTypeId);

		insertQueueTypeStatement.setInt (
			6,
			refObjectTypeId);

		insertQueueTypeStatement.executeUpdate ();

		connection.commit ();

	}

}
