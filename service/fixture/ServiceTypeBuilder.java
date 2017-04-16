package wbs.platform.service.fixture;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.camelToUnderscore;
import static wbs.utils.string.StringUtils.stringFormat;

import java.sql.SQLException;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.fixtures.ModelMetaBuilderHandler;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.service.metamodel.ServiceTypeSpec;
import wbs.platform.service.model.ServiceTypeObjectHelper;

@PrototypeComponent ("serviceTypeBuilder")
@ModelMetaBuilderHandler
public
class ServiceTypeBuilder
	implements BuilderComponent {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	@SingletonDependency
	ServiceTypeObjectHelper serviceTypeHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	ServiceTypeSpec spec;

	@BuilderTarget
	Model <?> model;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"build");

		try {

			taskLogger.noticeFormat (
				"Create service type %s.%s",
				camelToUnderscore (
					ifNull (
						spec.subject (),
						parent.name ())),
				simplifyToCodeRequired (
					spec.name ()));

			createServiceType (
				taskLogger);

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
	void createServiceType (
			@NonNull TaskLogger parentTaskLogger)
		throws SQLException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createServiceType");

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadWrite (
					"ServiceTypeBuilder.createServiceType ()",
					this);

		) {

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

			// create service type

			serviceTypeHelper.insert (
				taskLogger,
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

}
