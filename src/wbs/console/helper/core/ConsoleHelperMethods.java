package wbs.console.helper.core;

import static wbs.utils.etc.TypeUtils.dynamicCast;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.helper.provider.ConsoleHelperProvider;

import wbs.framework.codegen.DoNotDelegate;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;

import wbs.utils.string.FormatWriter;

public
interface ConsoleHelperMethods <
	RecordType extends Record <RecordType>
> {

	@DoNotDelegate
	ObjectHelper <RecordType> objectHelper ();

	String idKey ();

	String getPathId (
			TaskLogger parentTaskLogger,
			RecordType object);

	default
	String getPathIdGeneric (
			TaskLogger parentTaskLogger,
			Record <?> object) {

		return getPathId (
			parentTaskLogger,
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

	String getPathId (
			TaskLogger parentTaskLogger,
			Long objectId);

	String getDefaultContextPath (
			TaskLogger parentTaskLogger,
			RecordType object);

	default
	String getDefaultContextPathGeneric (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> object) {

		return getDefaultContextPath (
			parentTaskLogger,
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

	String getDefaultLocalPath (
			TaskLogger parentTaskLogger,
			RecordType object);

	default
	String getDefaultLocalPathGeneric (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> object) {

		return getDefaultLocalPath (
			parentTaskLogger,
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

	boolean canView (
			TaskLogger parentTaskLogger,
			RecordType object);

	Optional <RecordType> findFromContext ();

	RecordType findFromContextRequired ();

	void writeHtml (
			TaskLogger parentTaskLogger,
			FormatWriter formatWriter,
			RecordType object,
			Optional <Record <?>> assumedRoot,
			Boolean mini);

	default
	void writeHtmlGeneric (
			TaskLogger parentTaskLogger,
			FormatWriter formatWriter,
			Record <?> object,
			Optional <Record <?>> assumedRoot,
			Boolean mini) {

		writeHtml (
			parentTaskLogger,
			formatWriter,
			dynamicCast (
				objectHelper ().objectClass (),
				object),
			assumedRoot,
			mini);

	}

	ConsoleHooks <RecordType> consoleHooks ();

	ConsoleHelperProvider <RecordType> consoleHelperProvider ();

	<ObjectTypeAgain extends Record <ObjectTypeAgain>>
	ConsoleHelper <ObjectTypeAgain> cast (
			Class <?> objectClass);

}
