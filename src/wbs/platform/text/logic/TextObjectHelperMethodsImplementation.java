package wbs.platform.text.logic;

import static wbs.utils.etc.OptionalUtils.optionalFromNullable;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.LateLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextObjectHelperMethods;
import wbs.platform.text.model.TextRec;

import wbs.utils.cache.AdvancedCache;
import wbs.utils.cache.IdCacheBuilder;

public
class TextObjectHelperMethodsImplementation
	implements TextObjectHelperMethods {

	int maxExistingTexts = 65536;
	boolean optimistic = false;

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	TextObjectHelper textHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <IdCacheBuilder <String, Long, TextRec>> textCacheBuilderProvider;

	// state

	AdvancedCache <String, TextRec> textCache;

	// life cycle

	@LateLifecycleSetup
	public
	void setup () {

		// from and to user id

		textCache =
			textCacheBuilderProvider.get ()

			.lookupByIdFunction (
				textHelper::find)

			.lookupByKeyFunction (
				textValue ->
					optionalFromNullable (
						textHelper.findByTextNoFlush (
							textValue)))

			.getIdFunction (
				TextRec::getId)

			.createFunction (
				this::createReal)

			.build ();

	}

	// implementation

	@Override
	public
	TextRec findOrCreate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String stringValue) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"findOrCreate");

		return textCache.findOrCreate (
			taskLogger,
			stringValue);

	}

	private
	TextRec createReal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String stringValue) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createReal");

		return textHelper.insert (
			taskLogger,
			textHelper.createInstance ()

			.setText (
				stringValue)

		);

	}

}