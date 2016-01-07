package wbs.platform.service.fixture;

import static wbs.framework.utils.etc.CodeUtils.simplifyToCodeRequired;
import static wbs.framework.utils.etc.Misc.camelToUnderscore;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.sql.SQLException;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.ModelMetaBuilderHandler;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.record.GlobalId;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.service.metamodel.ServiceTypeSpec;
import wbs.platform.service.model.ServiceTypeObjectHelper;

@Log4j
@PrototypeComponent ("serviceTypeBuilder")
@ModelMetaBuilderHandler
public
class ServiceTypeBuilder {

	// dependencies

	@Inject
	Database database;

	@Inject
	EntityHelper entityHelper;

	@Inject
	ObjectTypeObjectHelper objectTypeHelper;

	@Inject
	ServiceTypeObjectHelper serviceTypeHelper;

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
			@NonNull Builder builder) {

		try {

			log.info (
				stringFormat (
					"Create service type %s.%s",
					camelToUnderscore (
						ifNull (
							spec.subject (),
							parent.name ())),
					simplifyToCodeRequired (
						spec.name ())));

			createServiceType ();

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating service type %s.%s",
					camelToUnderscore (
						ifNull (
							spec.subject (),
							parent.name ())),
					simplifyToCodeRequired (
						spec.name ())),
				exception);

		}

	}

	private
	void createServiceType ()
		throws SQLException {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		// lookup parent type

		String parentTypeCode =
			camelToUnderscore (
				ifNull (
					spec.subject (),
					parent.name ()));

		ObjectTypeRec parentType =
			objectTypeHelper.findByCode (
				GlobalId.root,
				parentTypeCode);

		// create service type

		serviceTypeHelper.insert (
			serviceTypeHelper.createInstance ()

			.setParentType (
				parentType)

			.setCode (
				simplifyToCodeRequired (
					spec.name ()))

			.setDescription (
				spec.description ())

		);

		// commit transaction

		transaction.commit ();

	}

}
