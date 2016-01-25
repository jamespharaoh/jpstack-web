package wbs.platform.text.model;

import static wbs.framework.utils.etc.Misc.contains;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.database.Database;

public
class TextObjectHelperImplementation
	implements TextObjectHelperMethods {

	int maxExistingTexts = 65536;
	boolean optimistic = false;

	// dependencies

	@Inject
	Database database;

	@Inject
	Provider<TextObjectHelper> textHelperProvider;

	// state

	Set<Integer> existingTexts =
		Collections.synchronizedSet (
			new HashSet<> ());

	List<Integer> existingTextsList =
		Collections.synchronizedList (
			new LinkedList<> (
				Stream.generate (() -> Integer.MAX_VALUE)
					.limit (maxExistingTexts)
					.collect (Collectors.toList ())));

	// implementation

	@Override
	public
	TextRec findOrCreate (
			@NonNull String textValue) {

		TextObjectHelper textHelper =
			textHelperProvider.get ();

		// null maps to null

		//if (textValue == null)
		//	return null;

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

		if (

			! optimistic

			|| contains (
				existingTexts,
				textValue.hashCode ())

		) {

			text =
				textHelper.findByText (
					textValue);

			if (text != null) {

				textCache.byText.put (
					textValue,
					text);

				return text;

			}

		} else if (optimistic) {

			existingTexts.add (
				textValue.hashCode ());

			existingTextsList.add (
				textValue.hashCode ());

			existingTexts.remove (
				existingTextsList.remove (0));

		}

		// insert new

		text =
			textHelper.insert (
				textHelper.createInstance ()

			.setText (
				textValue)

		);

		textCache.byText.put (
			textValue,
			text);

		return text;

	}

}