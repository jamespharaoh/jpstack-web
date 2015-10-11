package wbs.platform.affiliate.fixture;

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
import wbs.platform.affiliate.metamodel.AffiliateTypeSpec;

@PrototypeComponent ("affiliateTypeBuilder")
@ModelMetaBuilderHandler
public
class AffiliateTypeBuilder {

	// dependencies

	@Inject
	DataSource dataSource;

	@Inject
	EntityHelper entityHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	AffiliateTypeSpec spec;

	@BuilderTarget
	Model model;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		try {

			createAffiliateType ();

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating affiliate type %s.%s",
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
	void createAffiliateType ()
		throws SQLException {

		@Cleanup
		Connection connection =
			dataSource.getConnection ();

		connection.setAutoCommit (
			false);

		@Cleanup
		PreparedStatement nextAffiliateTypeIdStatement =
			connection.prepareStatement (
				stringFormat (
					"SELECT ",
						"nextval ('affiliate_type_id_seq')"));

		ResultSet affiliateTypeIdResultSet =
			nextAffiliateTypeIdStatement.executeQuery ();

		affiliateTypeIdResultSet.next ();

		int affiliateTypeId =
			affiliateTypeIdResultSet.getInt (
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
		PreparedStatement insertAffiliateTypeStatement =
			connection.prepareStatement (
				stringFormat (
					"INSERT INTO affiliate_type (",
						"id, ",
						"parent_type_id, ",
						"code, ",
						"name, ",
						"description) ",
					"VALUES (",
						"?, ",
						"?, ",
						"?, ",
						"?, ",
						"?)"));

		insertAffiliateTypeStatement.setInt (
			1,
			affiliateTypeId);

		insertAffiliateTypeStatement.setInt (
			2,
			objectTypeId);

		insertAffiliateTypeStatement.setString (
			3,
			codify (
				spec.name ()));

		insertAffiliateTypeStatement.setString (
			4,
			spec.name ());

		insertAffiliateTypeStatement.setString (
			5,
			spec.description ());

		insertAffiliateTypeStatement.executeUpdate ();

		connection.commit ();

	}

}
