package wbs.console.helper.core;

import static wbs.utils.etc.TypeUtils.dynamicCast;

import com.google.common.base.Optional;

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
			RecordType object);

	default
	String getPathIdGeneric (
			Record <?> object) {

		return getPathId (
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

	String getPathId (
			Long objectId);

	String getDefaultContextPath (
			RecordType object);

	default
	String getDefaultContextPathGeneric (
			Record <?> object) {

		return getDefaultContextPath (
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

	String getDefaultLocalPath (
			RecordType object);

	default
	String getDefaultLocalPathGeneric (
			Record <?> object) {

		return getDefaultLocalPath (
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
			FormatWriter formatWriter,
			RecordType object,
			Optional <Record <?>> assumedRoot,
			Boolean mini);

	default
	void writeHtmlGeneric (
			FormatWriter formatWriter,
			Record <?> object,
			Optional <Record <?>> assumedRoot,
			Boolean mini) {

		writeHtml (
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
