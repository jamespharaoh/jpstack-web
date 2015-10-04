package wbs.platform.priv.model;

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
import lombok.SneakyThrows;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.ModelMetaBuilderHandler;
import wbs.framework.entity.model.ModelMetaSpec;

@PrototypeComponent ("privTypeBuilder")
@ModelMetaBuilderHandler
public
class PrivTypeBuilder {

	// dependencies

	@Inject
	DataSource dataSource;

	@Inject
	EntityHelper entityHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	PrivTypeSpec spec;

	@BuilderTarget
	Model model;

	// build

	@BuildMethod
	@SneakyThrows (SQLException.class)
	public
	void build (
			Builder builder) {

		@Cleanup
		Connection connection =
			dataSource.getConnection ();

		connection.setAutoCommit (
			false);

		@Cleanup
		PreparedStatement nextPrivTypeIdStatement =
			connection.prepareStatement (
				stringFormat (
					"SELECT ",
						"nextval ('priv_type_id_seq')"));

		ResultSet privTypeIdResultSet =
			nextPrivTypeIdStatement.executeQuery ();

		privTypeIdResultSet.next ();

		int privTypeId =
			privTypeIdResultSet.getInt (
				1);

		String objectTypeCode =
			ifNull (
				spec.subject (),
				parent.name ());

		Model model =
			entityHelper.modelsByName ().get (
				objectTypeCode);

		if (model == null) {

			throw new RuntimeException (
				stringFormat (
					"No model for %s",
					objectTypeCode));

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
			model.objectTypeCode ());

		ResultSet getObjectTypeIdResult =
			getObjectTypeIdStatement.executeQuery ();

		getObjectTypeIdResult.next ();

		int objectTypeId =
			getObjectTypeIdResult.getInt (
				1);

		@Cleanup
		PreparedStatement insertPrivTypeStatement =
			connection.prepareStatement (
				stringFormat (
					"INSERT INTO priv_type (",
						"id, ",
						"parent_object_type_id, ",
						"code, ",
						"description, ",
						"help, ",
						"template) ",
					"VALUES (",
						"?, ",
						"?, ",
						"?, ",
						"?, ",
						"?, ",
						"?)"));

		insertPrivTypeStatement.setInt (
			1,
			privTypeId);

		insertPrivTypeStatement.setInt (
			2,
			objectTypeId);

		insertPrivTypeStatement.setString (
			3,
			codify (
				spec.name ()));

		insertPrivTypeStatement.setString (
			4,
			spec.description ());

		insertPrivTypeStatement.setString (
			5,
			spec.description ());

		insertPrivTypeStatement.setBoolean (
			6,
			spec.template ());

		insertPrivTypeStatement.executeUpdate ();

	}

}
