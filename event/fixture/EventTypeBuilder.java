package wbs.platform.event.fixture;

import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.stringFormat;

import java.sql.SQLException;

import lombok.NonNull;

import wbs.framework.builder.Builder;
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
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.metamodel.EventTypeSpec;
import wbs.platform.event.model.EventTypeObjectHelper;

@PrototypeComponent ("eventTypeBuilder")
@ModelMetaBuilderHandler
public
class EventTypeBuilder {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EntityHelper entityHelper;

	@SingletonDependency
	EventTypeObjectHelper eventTypeHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	EventTypeSpec spec;

	@BuilderTarget
	Model <?> model;

	// build

	@BuildMethod
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
				"Create event type %s",
				simplifyToCodeRequired (
					spec.name ()));

			createEventType (
				taskLogger);

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating event type %s",
					simplifyToCodeRequired (
						spec.name ())),
				exception);

		}

	}

	private
	void createEventType (
			@NonNull TaskLogger parentTaskLogger)
		throws SQLException {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createEventType");

			OwnedTransaction transaction =
				database.beginReadWrite (
					taskLogger,
					"EventTypeBuilder.createEventType ()",
					this);

		) {

			// create event type

			eventTypeHelper.insert (
				taskLogger,
				eventTypeHelper.createInstance ()

				.setCode (
					simplifyToCodeRequired (
						spec.name ()))

				.setDescription (
					spec.text ())

				.setAdmin (
					spec.admin ())

			);

			// commit transaction

			transaction.commit ();

		}

	}

}
