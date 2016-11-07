package wbs.framework.entity.generate;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.model.Model;
import wbs.framework.logging.TaskLogger;

@Log4j
public
class ObjectHelperGeneratorTool {

	// singleton dependencies

	@SingletonDependency
	EntityHelper entityHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <ObjectHelperGenerator> objectHelperGeneratorProvider;

	// implementation

	public
	void generateObjectHelpers (
			@NonNull TaskLogger taskLogger,
			@NonNull List <String> params) {

		taskLogger =
			taskLogger.nest (
				this,
				"generateObjectHelpers",
				log);

		List <Model <?>> models =
			entityHelper.models ();

		taskLogger.noticeFormat (
			"About to generate %s object helpers",
			integerToDecimalString (
				collectionSize (
					models)));

		long numSuccess = 0;
		long numFailures = 0;

		for (
			Model <?> model
				: models
		) {

			try {

				objectHelperGeneratorProvider.get ()

					.model (
						model)

					.generateHelper (
						taskLogger);

				numSuccess ++;

			} catch (Exception exception) {

				taskLogger.errorFormatException (
					exception,
					"Error writing object helper for %s",
					model.objectName ());

				numFailures ++;

			}

		}

		taskLogger.noticeFormat (
			"Successfully generated %s object helpers",
			integerToDecimalString (
				numSuccess));

		if (numFailures > 0) {

			taskLogger.errorFormat (
				"Aborting due to %s errors",
				integerToDecimalString (
					numFailures));

		}

	}

}
