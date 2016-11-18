package wbs.platform.event.fixture;

import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.stringFormat;

import java.sql.SQLException;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.fixtures.ModelMetaBuilderHandler;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.model.Model;

import wbs.platform.event.metamodel.EventTypeSpec;
import wbs.platform.event.model.EventTypeObjectHelper;

@Log4j
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
			@NonNull Builder builder) {

		try {

			log.info (
				stringFormat (
					"Create event type %s",
					simplifyToCodeRequired (
						spec.name ())));

			createEventType ();

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
	void createEventType ()
		throws SQLException {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"EventTypeBuilder.createEventType ()",
				this);

		// create event type

		eventTypeHelper.insert (
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
