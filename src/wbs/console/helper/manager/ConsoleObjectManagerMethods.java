package wbs.console.helper.manager;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.FormatWriterUtils.currentFormatWriter;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.helper.core.ConsoleHelper;
import wbs.framework.entity.record.Record;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.StringFormatWriter;

public
interface ConsoleObjectManagerMethods {

	ConsoleHelper <?> findConsoleHelper (
			Record <?> dataObject);

	<ObjectType extends Record <ObjectType>>
	ConsoleHelper <ObjectType> findConsoleHelper (
			Class <?> objectClass);

	ConsoleHelper <?> findConsoleHelper (
			String objectTypeName);

	void writeTdForObject (
			FormatWriter formatWriter,
			Record <?> object,
			Optional <Record <?>> assumedRootOptional,
			Boolean mini,
			Boolean link,
			Long colspan);

	default
	void writeTdForObjectMiniLink (
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object) {

		writeTdForObject (
			formatWriter,
			object,
			optionalAbsent (),
			true,
			true,
			1l);

	}

	default
	void writeTdForObjectMiniLink (
			@NonNull Record <?> object) {

		writeTdForObject (
			currentFormatWriter (),
			object,
			optionalAbsent (),
			true,
			true,
			1l);

	}

	default
	void writeTdForObjectMiniLink (
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Record <?> assumedRoot) {

		writeTdForObject (
			formatWriter,
			object,
			optionalOf (
				assumedRoot),
			true,
			true,
			1l);

	}

	default
	void writeTdForObjectMiniLink (
			@NonNull Record <?> object,
			@NonNull Record <?> assumedRoot) {

		writeTdForObject (
			currentFormatWriter (),
			object,
			optionalOf (
				assumedRoot),
			true,
			true,
			1l);

	}

	default
	void writeTdForObjectMiniLink (
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Long columnSpan) {

		writeTdForObject (
			formatWriter,
			object,
			optionalAbsent (),
			true,
			true,
			columnSpan);

	}

	default
	void writeTdForObjectMiniLink (
			@NonNull Record <?> object,
			@NonNull Long columnSpan) {

		writeTdForObject (
			currentFormatWriter (),
			object,
			optionalAbsent (),
			true,
			true,
			columnSpan);

	}

	default
	void writeTdForObjectMiniLink (
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Record<?> assumedRoot,
			@NonNull Long columnSpan) {

		writeTdForObject (
			formatWriter,
			object,
			optionalOf (
				assumedRoot),
			true,
			true,
			columnSpan);

	}

	default
	void writeTdForObjectLink (
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object) {

		writeTdForObject (
			formatWriter,
			object,
			optionalAbsent (),
			false,
			true,
			1l);

	}

	default
	void writeTdForObjectLink (
			@NonNull Record <?> object) {

		writeTdForObject (
			currentFormatWriter (),
			object,
			optionalAbsent (),
			false,
			true,
			1l);

	}

	default
	void writeTdForObjectLink (
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Record <?> assumedRoot) {

		writeTdForObject (
			formatWriter,
			object,
			optionalOf (
				assumedRoot),
			false,
			true,
			1l);

	}

	default
	void writeTdForObjectLink (
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Long columnSpan) {

		writeTdForObject (
			formatWriter,
			object,
			optionalAbsent (),
			false,
			true,
			columnSpan);

	}

	default
	void writeTdForObjectLink (
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Record <?> assumedRoot,
			@NonNull Long columnSpan) {

		writeTdForObject (
			formatWriter,
			object,
			optionalOf (
				assumedRoot),
			false,
			true,
			columnSpan);

	}


	void writeHtmlForObject (
			FormatWriter formatWriter,
			Record <?> object,
			Optional <Record <?>> assumedRootOptional,
			Boolean mini);

	default
	String htmlForObject (
			@NonNull Record <?> object,
			@NonNull Optional <Record <?>> assumedRootOptional,
			@NonNull Boolean mini) {

		StringFormatWriter formatWriter =
			new StringFormatWriter ();

		writeHtmlForObject (
			formatWriter,
			object,
			assumedRootOptional,
			mini);

		return formatWriter.toString ();

	}

	boolean canView (
			Record <?> object);

	String contextName (
			Record <?> object);

	String contextLink (
			Record <?> object);

	String localLink (
			Record <?> object);

	void objectToSimpleHtml (
			FormatWriter formatWriter,
			Object object,
			Record <?> assumedRoot,
			boolean mini);

}