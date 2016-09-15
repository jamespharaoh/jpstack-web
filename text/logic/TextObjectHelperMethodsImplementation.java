package wbs.platform.text.logic;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.LateLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Database;
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
			@NonNull String stringValue) {

		return textCache.findOrCreate (
			stringValue);

	}

	@Override
	public
	TextRec findOrCreateMapNull (
			String stringValue) {

		if (
			isNull (
				stringValue)
		) {
			return null;
		}

		return findOrCreate (
			stringValue);

	}

	private
	TextRec createReal (
			@NonNull String stringValue) {

		return textHelper.insert (
			textHelper.createInstance ()

			.setText (
				stringValue)

		);

	}

}