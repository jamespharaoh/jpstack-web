package wbs.platform.text.model;

import static wbs.framework.utils.etc.Misc.contains;
import static wbs.framework.utils.etc.Misc.isNull;

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
			@NonNull String stringValue) {

		TextObjectHelper textHelper =
			textHelperProvider.get ();

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
				stringValue);

		if (text != null)
			return text;

		// look in database

		if (

			! optimistic

			|| contains (
				existingTexts,
				stringValue.hashCode ())

		) {

			text =
				textHelper.findByText (
					stringValue);

			if (text != null) {

				textCache.byText.put (
					stringValue,
					text);

				return text;

			}

		} else if (optimistic) {

			existingTexts.add (
				stringValue.hashCode ());

			existingTextsList.add (
				stringValue.hashCode ());

			existingTexts.remove (
				existingTextsList.remove (0));

		}

		// insert new

		text =
			textHelper.insert (
				textHelper.createInstance ()

			.setText (
				stringValue)

		);

		textCache.byText.put (
			stringValue,
			text);

		return text;

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

}