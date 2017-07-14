package wbs.framework.schema.tool;

import static wbs.utils.collection.CollectionUtils.collectionHasOneItem;
import static wbs.utils.collection.CollectionUtils.collectionHasTwoItems;
import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.collection.CollectionUtils.listLastItemRequired;
import static wbs.utils.collection.CollectionUtils.listSecondElementRequired;
import static wbs.utils.collection.CollectionUtils.listSliceAllButLastItemRequired;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.DebugUtils.debugFormat;
import static wbs.utils.etc.Misc.todo;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.keyEqualsString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringSplitFullStop;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.cachedview.CachedGroupFieldSpec;
import wbs.framework.entity.meta.cachedview.CachedViewSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.ModelField;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.sql.SqlLogic;

import wbs.utils.string.LazyFormatWriter;

@SingletonComponent ("cachedViewToSql")
public
class CachedViewToSqlImplementation
	implements CachedViewToSql {

	// singleton dependencies

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SqlLogic sqlLogic;

	// public implementation

	@Override
	public
	void forModel (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> sqlStatements,
			@NonNull Model <?> model) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"forModel",
					keyEqualsString (
						"objectName",
						model.objectName ()));

		) {

			createLogTable (
				taskLogger,
				sqlStatements,
				model);

		}

	}

	// private implementation

	private
	void createLogTable (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> sqlStatements,
			@NonNull Model <?> model) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createLogTable");

			LazyFormatWriter formatWriter =
				new LazyFormatWriter ();

		) {

			formatWriter.indentString ("  ");

			CachedViewSpec cachedView =
				model.cachedView ();

			formatWriter.writeLineFormatIncreaseIndent (
				"CREATE TABLE %s (",
				sqlLogic.quoteIdentifierFormat (
					"%s_updates_pending",
					model.tableName ()));

			List <String> columnDefinitions =
				new ArrayList<> ();

			for (
				CachedGroupFieldSpec groupField
					: cachedView.groupFields ()
			) {

				String fieldName =
					ifNull (
						groupField.fieldName (),
						groupField.name ());

				List <String> fieldNameParts =
					stringSplitFullStop (
						fieldName);

				if (
					collectionHasOneItem (
						fieldNameParts)
				) {

					throw todo ();

				} else if (
					collectionHasTwoItems (
						fieldNameParts)
				) {

					ModelField compositeField =
						model.field (
							listFirstElementRequired (
								fieldNameParts));

					Model <?> compositeModel =
						mapItemForKeyRequired (
							entityHelper.compositeModelsByClass (),
							compositeField.valueType ());

					ModelField actualField =
						compositeModel.field (
							listSecondElementRequired (
								fieldNameParts));

					for (
						long index = 0;
						index < collectionSize (
							actualField.columnNames ());
						index ++
					) {

						String columnName =
							listItemAtIndexRequired (
								actualField.columnNames (),
								index);

						String columnSqlType =
							listItemAtIndexRequired (
								actualField.columnSqlTypes (),
								index);

						columnDefinitions.add (
							stringFormat (
								"%s %s NOT NULL",
								sqlLogic.quoteIdentifier (
									columnName),
								columnSqlType));

					}

				} else {

					throw todo ();

				}

			}

			for (
				String columnDefinition
					: listSliceAllButLastItemRequired (
						columnDefinitions)
			) {

				formatWriter.writeLineFormat (
					"%s,",
					columnDefinition);

			}

			formatWriter.writeLineFormat (
				"%s",
				listLastItemRequired (
					columnDefinitions));

			formatWriter.writeLineFormatDecreaseIndent (
				");");

debugFormat ("CACHED VIEW: %s", formatWriter.toString ());

			sqlStatements.add (
				formatWriter.toString ());

		}

	}

}
