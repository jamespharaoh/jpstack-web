package wbs.console.helper;

import com.google.common.base.Optional;

import wbs.framework.entity.record.Record;
import wbs.utils.string.FormatWriter;

public
interface ConsoleHelperMethods <ObjectType extends Record <ObjectType>> {

	String idKey ();

	String getPathId (
			Record <?> object);

	String getPathId (
			Long objectId);

	String getDefaultContextPath (
			Record <?> object);

	String getDefaultLocalPath (
			Record <?> object);

	boolean canView (
			Record <?> object);

	void writeHtml (
			FormatWriter formatWriter,
			Record <?> object,
			Optional <Record <?>> assumedRoot,
			Boolean mini);

	ConsoleHooks <ObjectType> consoleHooks ();

	<ObjectTypeAgain extends Record <ObjectTypeAgain>>
	ConsoleHelper <ObjectTypeAgain> cast (
			Class <?> objectClass);

}
