package wbs.platform.queue.fixture;

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
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.fixtures.ModelMetaBuilderHandler;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.queue.metamodel.QueueTypeSpec;
import wbs.platform.queue.model.QueueTypeObjectHelper;

@PrototypeComponent ("queueTypeBuilder")
@ModelMetaBuilderHandler
public
class QueueTypeBuilder
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
	QueueTypeObjectHelper queueTypeHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	QueueTypeSpec spec;

	@BuilderTarget
	Model <?> model;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			taskLogger.noticeFormat (
				"Create queue type %s.%s",
				camelToUnderscore (
					ifNull (
						spec.subject (),
						parent.name ())),
				simplifyToCodeRequired (
					spec.name ()));

			createQueueType (
				taskLogger);

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating queue type %s.%s",
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
	void createQueueType (
			@NonNull TaskLogger parentTaskLogger)
		throws SQLException {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createQueueType");

			OwnedTransaction transaction =
				database.beginReadWrite (
					taskLogger,
					"QueueTypeBuilder.createQueueType ()",
					this);

		) {

			// lookup parent type

			String parentTypeCode =
				camelToUnderscore (
					ifNull (
						spec.parent (),
						parent.name ()));

			ObjectTypeRec parentType =
				objectTypeHelper.findByCodeRequired (
					GlobalId.root,
					parentTypeCode);

			// lookup subject type

			String subjectTypeCode =
				camelToUnderscore (
					spec.subject ());

			ObjectTypeRec subjectType =
				objectTypeHelper.findByCodeRequired (
					GlobalId.root,
					subjectTypeCode);

			// lookup ref type

			String refTypeCode =
				camelToUnderscore (
					spec.ref ());

			ObjectTypeRec refType =
				objectTypeHelper.findByCodeRequired (
					GlobalId.root,
					refTypeCode);

			// create queue type

			queueTypeHelper.insert (
				taskLogger,
				queueTypeHelper.createInstance ()

				.setParentType (
					parentType)

				.setCode (
					simplifyToCodeRequired (
						spec.name ()))

				.setDescription (
					spec.description ())

				.setSubjectType (
					subjectType)

				.setRefType (
					refType)

				.setDefaultPriority (
					spec.defaultPriority ())

			);

			// commit transaction

			transaction.commit ();

		}

	}

}
