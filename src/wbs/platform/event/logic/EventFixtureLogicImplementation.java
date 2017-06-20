package wbs.platform.event.logic;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

@SingletonComponent ("eventFixtureLogic")
public
class EventFixtureLogicImplementation
	implements EventFixtureLogic {

	// singleton dependencies

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	void createEvents (
			@NonNull Transaction parentTransaction,
			@NonNull String fixtureProviderName,
			@NonNull Record <?> parent,
			@NonNull Record <?> object,
			@NonNull Map <String, Object> fields) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createEvents");

		) {

			eventLogic.createEvent (
				transaction,
				"admin_object_created_by_fixture",
				fixtureProviderName,
				object,
				parent);

			fields.forEach (
				(fieldName, fieldValue) -> {

				if (fieldValue instanceof Optional) {

					Optional <?> fieldValueOptional =
						(Optional <?>) fieldValue;

					if (
						optionalIsPresent (
							fieldValueOptional)
					) {

						eventLogic.createEvent (
							transaction,
							"admin_object_field_updated_by_fixture",
							fixtureProviderName,
							fieldName,
							object,
							optionalGetRequired (
								fieldValueOptional));

					}

				} else {

					eventLogic.createEvent (
						transaction,
						"admin_object_field_updated_by_fixture",
						fixtureProviderName,
						fieldName,
						object,
						fieldValue);

				}

			});

		}

	}

}
