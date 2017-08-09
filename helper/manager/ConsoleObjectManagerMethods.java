package wbs.console.helper.manager;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrThrow;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.string.FormatWriterUtils.formatWriterConsumerToString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.NoSuchElementException;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.helper.core.ConsoleHelper;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

import wbs.utils.string.FormatWriter;

public
interface ConsoleObjectManagerMethods {

	<RecordType extends Record <RecordType>>
	Optional <ConsoleHelper <RecordType>> consoleHelperForObject (
			Record <?> object);

	default
	<RecordType extends Record <RecordType>>
	ConsoleHelper <RecordType> consoleHelperForObjectRequired (
			@NonNull RecordType object) {

		return optionalGetRequired (
			consoleHelperForObject (
				object));

	}

	<ObjectType extends Record <ObjectType>>
	Optional <ConsoleHelper <ObjectType>> consoleHelperForClass (
			Class <?> objectClass);

	default
	<ObjectType extends Record <ObjectType>>
	ConsoleHelper <ObjectType> consoleHelperForClassRequired (
			@NonNull Class <?> objectClass) {

		return optionalOrThrow (
			consoleHelperForClass (
				objectClass),
			() -> new NoSuchElementException (
				stringFormat (
					"No console helper for class %s",
					classNameSimple (
						objectClass))));

	}

	Optional <ConsoleHelper <?>> consoleHelperForName (
			String objectTypeName);

	default
	ConsoleHelper <?> consoleHelperForNameRequired (
			@NonNull String objectTypeName) {

		return optionalOrThrow (
			consoleHelperForName (
				objectTypeName),
			() -> new NoSuchElementException (
				stringFormat (
					"No console helper for object type %s",
					objectTypeName)));

	}

	void writeTdForObject (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			UserPrivChecker privChecker,
			Record <?> object,
			Optional <Record <?>> assumedRootOptional,
			Boolean mini,
			Boolean link,
			Long colspan);

	default
	void writeTdForObjectMiniLink (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull UserPrivChecker privChecker,
			@NonNull Record <?> object) {

		writeTdForObject (
			parentTransaction,
			formatWriter,
			privChecker,
			object,
			optionalAbsent (),
			true,
			true,
			1l);

	}

	default
	void writeTdForObjectMiniLink (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull UserPrivChecker privChecker,
			@NonNull Record <?> object,
			@NonNull Record <?> assumedRoot) {

		writeTdForObject (
			parentTransaction,
			formatWriter,
			privChecker,
			object,
			optionalOf (
				assumedRoot),
			true,
			true,
			1l);

	}

	default
	void writeTdForObjectMiniLink (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull UserPrivChecker privChecker,
			@NonNull Record <?> object,
			@NonNull Long columnSpan) {

		writeTdForObject (
			parentTransaction,
			formatWriter,
			privChecker,
			object,
			optionalAbsent (),
			true,
			true,
			columnSpan);

	}

	default
	void writeTdForObjectMiniLink (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull UserPrivChecker privChecker,
			@NonNull Record <?> object,
			@NonNull Record<?> assumedRoot,
			@NonNull Long columnSpan) {

		writeTdForObject (
			parentTransaction,
			formatWriter,
			privChecker,
			object,
			optionalOf (
				assumedRoot),
			true,
			true,
			columnSpan);

	}

	default
	void writeTdForObjectLink (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull UserPrivChecker privChecker,
			@NonNull Record <?> object) {

		writeTdForObject (
			parentTransaction,
			formatWriter,
			privChecker,
			object,
			optionalAbsent (),
			false,
			true,
			1l);

	}

	default
	void writeTdForObjectLink (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull UserPrivChecker privChecker,
			@NonNull Record <?> object,
			@NonNull Record <?> assumedRoot) {

		writeTdForObject (
			parentTransaction,
			formatWriter,
			privChecker,
			object,
			optionalOf (
				assumedRoot),
			false,
			true,
			1l);

	}

	default
	void writeTdForObjectLink (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull UserPrivChecker privChecker,
			@NonNull Record <?> object,
			@NonNull Long columnSpan) {

		writeTdForObject (
			parentTransaction,
			formatWriter,
			privChecker,
			object,
			optionalAbsent (),
			false,
			true,
			columnSpan);

	}

	default
	void writeTdForObjectLink (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull UserPrivChecker privChecker,
			@NonNull Record <?> object,
			@NonNull Record <?> assumedRoot,
			@NonNull Long columnSpan) {

		writeTdForObject (
			parentTransaction,
			formatWriter,
			privChecker,
			object,
			optionalOf (
				assumedRoot),
			false,
			true,
			columnSpan);

	}


	void writeHtmlForObject (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			Record <?> object,
			Optional <Record <?>> assumedRootOptional,
			Boolean mini);

	default
	String htmlForObject (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object,
			@NonNull Optional <Record <?>> assumedRootOptional,
			@NonNull Boolean mini) {

		return formatWriterConsumerToString (
			"  ",
			formatWriter ->
				writeHtmlForObject (
					parentTransaction,
					formatWriter,
					object,
					assumedRootOptional,
					mini));

	}

	boolean canView (
			Transaction parentTransaction,
			UserPrivChecker privChecker,
			Record <?> object);

	String contextName (
			Transaction parentTransaction,
			Record <?> object);

	String contextLink (
			Transaction parentTransaction,
			Record <?> object);

	String localLink (
			Transaction parentTransaction,
			Record <?> object);

	void objectToSimpleHtml (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			Object object,
			Record <?> assumedRoot,
			boolean mini);

}