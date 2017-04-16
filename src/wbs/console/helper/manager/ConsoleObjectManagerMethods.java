package wbs.console.helper.manager;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrThrow;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.string.FormatWriterUtils.currentFormatWriter;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.NoSuchElementException;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.helper.core.ConsoleHelper;

import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;
import wbs.utils.string.StringFormatWriter;

public
interface ConsoleObjectManagerMethods {

	<RecordType extends Record <RecordType>>
	Optional <ConsoleHelper <RecordType>> findConsoleHelper (
			Record <?> object);

	default
	ConsoleHelper <?> findConsoleHelperRequired (
			@NonNull Record <?> object) {

		return optionalGetRequired (
			findConsoleHelper (
				object));

	}

	<ObjectType extends Record <ObjectType>>
	Optional <ConsoleHelper <ObjectType>> findConsoleHelper (
			Class <?> objectClass);

	default
	<ObjectType extends Record <ObjectType>>
	ConsoleHelper <ObjectType> findConsoleHelperRequired (
			@NonNull Class <?> objectClass) {

		return optionalOrThrow (
			findConsoleHelper (
				objectClass),
			() -> new NoSuchElementException (
				stringFormat (
					"No console helper for class %s",
					classNameSimple (
						objectClass))));

	}

	Optional <ConsoleHelper <?>> findConsoleHelper (
			String objectTypeName);

	default
	ConsoleHelper <?> findConsoleHelperRequired (
			@NonNull String objectTypeName) {

		return optionalOrThrow (
			findConsoleHelper (
				objectTypeName),
			() -> new NoSuchElementException (
				stringFormat (
					"No console helper for object type %s",
					objectTypeName)));

	}

	void writeTdForObject (
			TaskLogger parentTaskLogger,
			FormatWriter formatWriter,
			Record <?> object,
			Optional <Record <?>> assumedRootOptional,
			Boolean mini,
			Boolean link,
			Long colspan);

	default
	void writeTdForObjectMiniLink (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object) {

		writeTdForObject (
			parentTaskLogger,
			formatWriter,
			object,
			optionalAbsent (),
			true,
			true,
			1l);

	}

	default
	void writeTdForObjectMiniLink (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> object) {

		writeTdForObject (
			parentTaskLogger,
			currentFormatWriter (),
			object,
			optionalAbsent (),
			true,
			true,
			1l);

	}

	default
	void writeTdForObjectMiniLink (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Record <?> assumedRoot) {

		writeTdForObject (
			parentTaskLogger,
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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> object,
			@NonNull Record <?> assumedRoot) {

		writeTdForObject (
			parentTaskLogger,
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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Long columnSpan) {

		writeTdForObject (
			parentTaskLogger,
			formatWriter,
			object,
			optionalAbsent (),
			true,
			true,
			columnSpan);

	}

	default
	void writeTdForObjectMiniLink (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> object,
			@NonNull Long columnSpan) {

		writeTdForObject (
			parentTaskLogger,
			currentFormatWriter (),
			object,
			optionalAbsent (),
			true,
			true,
			columnSpan);

	}

	default
	void writeTdForObjectMiniLink (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Record<?> assumedRoot,
			@NonNull Long columnSpan) {

		writeTdForObject (
			parentTaskLogger,
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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object) {

		writeTdForObject (
			parentTaskLogger,
			formatWriter,
			object,
			optionalAbsent (),
			false,
			true,
			1l);

	}

	default
	void writeTdForObjectLink (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> object) {

		writeTdForObject (
			parentTaskLogger,
			currentFormatWriter (),
			object,
			optionalAbsent (),
			false,
			true,
			1l);

	}

	default
	void writeTdForObjectLink (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Record <?> assumedRoot) {

		writeTdForObject (
			parentTaskLogger,
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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Long columnSpan) {

		writeTdForObject (
			parentTaskLogger,
			formatWriter,
			object,
			optionalAbsent (),
			false,
			true,
			columnSpan);

	}

	default
	void writeTdForObjectLink (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Record <?> assumedRoot,
			@NonNull Long columnSpan) {

		writeTdForObject (
			parentTaskLogger,
			formatWriter,
			object,
			optionalOf (
				assumedRoot),
			false,
			true,
			columnSpan);

	}


	void writeHtmlForObject (
			TaskLogger parentTaskLogger,
			FormatWriter formatWriter,
			Record <?> object,
			Optional <Record <?>> assumedRootOptional,
			Boolean mini);

	default
	String htmlForObject (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> object,
			@NonNull Optional <Record <?>> assumedRootOptional,
			@NonNull Boolean mini) {

		StringFormatWriter formatWriter =
			new StringFormatWriter ();

		writeHtmlForObject (
			parentTaskLogger,
			formatWriter,
			object,
			assumedRootOptional,
			mini);

		return formatWriter.toString ();

	}

	boolean canView (
			TaskLogger parentTaskLogger,
			Record <?> object);

	String contextName (
			TaskLogger parentTaskLogger,
			Record <?> object);

	String contextLink (
			TaskLogger parentTaskLogger,
			Record <?> object);

	String localLink (
			TaskLogger parentTaskLogger,
			Record <?> object);

	void objectToSimpleHtml (
			TaskLogger parentTaskLogger,
			FormatWriter formatWriter,
			Object object,
			Record <?> assumedRoot,
			boolean mini);

}