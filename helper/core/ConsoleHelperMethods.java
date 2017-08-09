package wbs.console.helper.core;

import static wbs.utils.etc.TypeUtils.dynamicCastRequired;

import com.google.common.base.Optional;
import com.google.common.collect.Ordering;

import lombok.NonNull;

import wbs.console.helper.provider.ConsoleHelperProvider;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.codegen.DoNotDelegate;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
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
			Transaction parentTransaction,
			RecordType object);

	default
	String getPathIdGeneric (
			Transaction parentTransaction,
			Record <?> object) {

		return getPathId (
			parentTransaction,
			dynamicCastRequired (
				objectHelper ().objectClass (),
				object));

	}

	String getPathId (
			Transaction parentTransaction,
			Long objectId);

	String getDefaultContextPath (
			Transaction parentTransaction,
			RecordType object);

	default
	String getDefaultContextPathGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		return getDefaultContextPath (
			parentTransaction,
			dynamicCastRequired (
				objectHelper ().objectClass (),
				object));

	}

	String getDefaultLocalPath (
			Transaction parentTransaction,
			RecordType object);

	default
	String getDefaultLocalPathGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		return getDefaultLocalPath (
			parentTransaction,
			dynamicCastRequired (
				objectHelper ().objectClass (),
				object));

	}

	boolean canView (
			Transaction parentTransaction,
			UserPrivChecker privChecker,
			RecordType object);

	boolean canCreateIn (
			Transaction parentTransaction,
			UserPrivChecker privChecker,
			Record <?> parent);

	Optional <RecordType> findFromContext (
			Transaction parentTransaction);

	RecordType findFromContextRequired (
			Transaction parentTransaction);

	void writeHtml (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			RecordType object,
			Optional <Record <?>> assumedRoot,
			Boolean mini);

	default
	void writeHtmlGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Record <?> object,
			@NonNull Optional <Record <?>> assumedRoot,
			@NonNull Boolean mini) {

		writeHtml (
			parentTransaction,
			formatWriter,
			dynamicCastRequired (
				objectHelper ().objectClass (),
				object),
			assumedRoot,
			mini);

	}

	default
	Ordering <RecordType> defaultOrdering () {
		return consoleHooks ().defaultOrdering ();
	}

	ConsoleHooks <RecordType> consoleHooks ();

	ConsoleHelperProvider <RecordType> consoleHelperProvider ();

	<ObjectTypeAgain extends Record <ObjectTypeAgain>>
	ConsoleHelper <ObjectTypeAgain> cast (
			Class <?> objectClass);

}
