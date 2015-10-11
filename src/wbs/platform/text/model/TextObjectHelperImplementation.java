package wbs.platform.text.model;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.database.Database;

public
class TextObjectHelperImplementation
	implements TextObjectHelperMethods {

	@Inject
	Database database;

	@Inject
	Provider<TextObjectHelper> textHelperProvider;

	@Override
	public
	TextRec findOrCreate (
			String textValue) {

		TextObjectHelper textHelper =
			textHelperProvider.get ();

		// null maps to null

		if (textValue == null)
			return null;

		// get cache

		TextCache textCache =
			(TextCache)
			database.currentTransaction ().getMeta (
				"textCache");

		if (textCache == null) {

			textCache =
				new TextCache ();

			database.currentTransaction ().setMeta (
				"textCache",
				textCache);

		}

		// look in cache

		TextRec text =
			textCache.byText.get (
				textValue);

		if (text != null)
			return text;

		// look in database

		text =
			textHelper.findByText (
				textValue);

		if (text != null) {

			textCache.byText.put (
				textValue,
				text);

			return text;

		}

		// insert new

		text =
			textHelper.insert (
				new TextRec ()
					.setText (textValue));

		textCache.byText.put (
			textValue,
			text);

		return text;

	}

}