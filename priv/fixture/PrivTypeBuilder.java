package wbs.platform.priv.fixture;

import static wbs.framework.utils.etc.CodeUtils.simplifyToCodeRequired;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.camelToUnderscore;

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
import wbs.platform.priv.metamodel.PrivTypeSpec;
import wbs.platform.priv.model.PrivTypeObjectHelper;

@Log4j
@PrototypeComponent ("privTypeBuilder")
@ModelMetaBuilderHandler
public
class PrivTypeBuilder {

	// dependencies

	@Inject
	Database database ;

	@Inject
	EntityHelper entityHelper;

	@Inject
	ObjectTypeObjectHelper objectTypeHelper;

	@Inject
	PrivTypeObjectHelper privTypeHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	PrivTypeSpec spec;

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
					"Create priv type %s.%s",
					camelToUnderscore (
						ifNull (
							spec.subject (),
							parent.name ())),
					simplifyToCodeRequired (
						spec.name ())));

			createPrivType ();

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating priv type %s.%s",
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
	void createPrivType () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"PrivTypeBuilder.createPrivType ()",
				this);

		// lookup parent type

		String parentTypeCode =
			camelToUnderscore (
				ifNull (
					spec.subject (),
					parent.name ()));

		ObjectTypeRec parentType =
			objectTypeHelper.findByCodeRequired (
				GlobalId.root,
				parentTypeCode);

		// create priv type

		privTypeHelper.insert (
			privTypeHelper.createInstance ()

			.setParentObjectType (
				parentType)

			.setCode (
				simplifyToCodeRequired (
					spec.name ()))

			.setDescription (
				spec.description ())

			.setHelp (
				spec.description ())

			.setTemplate (
				spec.template ())

		);

		// commit transaction

		transaction.commit ();

	}

}
