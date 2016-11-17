package wbs.console.helper.generate;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.model.Model;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

public
class ConsoleHelperGeneratorTool {

	// singleton dependencies

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleHelperGenerator> consoleHelperGeneratorProvider;

	// implementation

	public
	void generateConsoleHelpers (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> params) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"generateConsoleHelpers");

		List <Model <?>> models =
			entityHelper.models ();

		taskLogger.noticeFormat (
			"About to generate %s console helpers",
			integerToDecimalString (
				collectionSize (
					models)));

		long numSuccess = 0;
		long numFailures = 0;

		for (
			Model <?> model
				: models
		) {

			TaskLogger nestedTaskLogger =
				logContext.nestTaskLogger (
					taskLogger,
					stringFormat (
						"generateConsoleHelpers.forEachModel (%s)",
						model.objectName ()));

			consoleHelperGeneratorProvider.get ()

				.taskLogger (
					nestedTaskLogger)

				.model (
					model)

				.generateHelper ();

			if (nestedTaskLogger.errors ()) {

				taskLogger.errorFormat (
					"Error writing console helper for %s",
					model.objectName ());

				numFailures ++;

			} else {

				numSuccess ++;

			}

		}

		taskLogger.noticeFormat (
			"Successfully generated %s console helpers",
			integerToDecimalString (
				numSuccess));

		if (
			moreThanZero (
				numFailures)
		) {

			taskLogger.errorFormat (
				"Failed to generate %s console helpers",
				integerToDecimalString (
					numFailures));

		}

	}

}
