package wbs.platform.service.fixture;

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
import wbs.platform.service.metamodel.ServiceTypeSpec;

@PrototypeComponent ("serviceTypeBuilder")
@ModelMetaBuilderHandler
public
class ServiceTypeBuilder {

	// dependencies

	@Inject
	DataSource dataSource;

	@Inject
	EntityHelper entityHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	ServiceTypeSpec spec;

	@BuilderTarget
	Model model;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		try {

			createServiceType ();

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating service type %s.%s",
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
	void createServiceType ()
		throws SQLException {

		@Cleanup
		Connection connection =
			dataSource.getConnection ();

		connection.setAutoCommit (
			false);

		@Cleanup
		PreparedStatement nextServiceTypeIdStatement =
			connection.prepareStatement (
				stringFormat (
					"SELECT ",
						"nextval ('service_type_id_seq')"));

		ResultSet serviceTypeIdResultSet =
			nextServiceTypeIdStatement.executeQuery ();

		serviceTypeIdResultSet.next ();

		int serviceTypeId =
			serviceTypeIdResultSet.getInt (
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
		PreparedStatement insertServiceTypeStatement =
			connection.prepareStatement (
				stringFormat (
					"INSERT INTO service_type (",
						"id, ",
						"parent_object_type_id, ",
						"code, ",
						"description) ",
					"VALUES (",
						"?, ",
						"?, ",
						"?, ",
						"?)"));

		insertServiceTypeStatement.setInt (
			1,
			serviceTypeId);

		insertServiceTypeStatement.setInt (
			2,
			objectTypeId);

		insertServiceTypeStatement.setString (
			3,
			codify (
				spec.name ()));

		insertServiceTypeStatement.setString (
			4,
			spec.description ());

		insertServiceTypeStatement.executeUpdate ();

		connection.commit ();

	}

}
