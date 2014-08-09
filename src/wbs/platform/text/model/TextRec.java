package wbs.platform.text.model;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.database.Database;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
//@ToString (of = "id")
@CommonEntity
public
class TextRec
	implements CommonRecord<TextRec> {

	// id

	@GeneratedIdField
	Integer id;

	// details

	@SimpleField
	String text;

	// to string

	// TODO fix this properly
	@Override
	public
	String toString () {
		return text;
	}

	// compare to

	@Override
	public
	int compareTo (
			Record<TextRec> otherRecord) {

		TextRec other =
			(TextRec) otherRecord;

		return new CompareToBuilder ()
			.append (getText (), other.getText ())
			.toComparison ();

	}

	// dao methods

	public static
	interface TextDaoMethods {

		TextRec findByText (
				String text);
	}

	// object helper methods

	public static
	interface TextObjectHelperMethods {

		TextRec findOrCreate (
				String textValue);

	}

	// object helper implementation

	public static
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

	// cache

	static
	class TextCache {

		Map<String,TextRec> byText =
			new HashMap<String,TextRec> ();

	}

}
