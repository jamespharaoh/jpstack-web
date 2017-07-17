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
import static wbs.utils.etc.Misc.todo;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.keyEqualsString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringReplaceAllSimple;
import static wbs.utils.string.StringUtils.stringSplitFullStop;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.cachedview.CachedAggregateFieldSpec;
import wbs.framework.entity.meta.cachedview.CachedGroupFieldSpec;
import wbs.framework.entity.meta.cachedview.CachedViewSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.ModelField;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.sql.SqlLogic;

import wbs.utils.data.Pair;
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

		) {

			CachedViewSpec cachedView =
				model.cachedView ();

			Model <?> sourceModel =
				mapItemForKeyRequired (
					entityHelper.recordModelsByName (),
					cachedView.sourceObjectName ());

			// collect column data

			List <String> columnDefinitions =
				new ArrayList<> ();

			columnDefinitions.add (
				stringFormat (
					"id bigint PRIMARY KEY DEFAULT nextval (%s)",
					sqlLogic.quoteStringFormat (
						"%s_updates_pending_id_seq",
						model.tableName ())));

			List <String> triggerIndexColumns =
				new ArrayList<> ();

			List <Pair <String, String>> triggerValueColumns =
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

						triggerIndexColumns.add (
							sqlLogic.quoteIdentifier (
								columnName));

					}

				} else {

					throw todo ();

				}

			}

			for (
				CachedAggregateFieldSpec aggregateField
					: cachedView.aggregateFields ()
			) {

				String fieldName =
					ifNull (
						aggregateField.fieldName (),
						aggregateField.name ());

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

						triggerValueColumns.add (
							Pair.of (
								sqlLogic.quoteIdentifier (
									columnName),
								aggregateField.when ()));

					}

				} else {

					throw todo ();

				}

			}

			// create sequence

			try (

				LazyFormatWriter formatWriter =
					new LazyFormatWriter ("  ");

			) {

				formatWriter.writeLineFormat (
					"CREATE SEQUENCE %s;",
					sqlLogic.quoteIdentifierFormat (
						"%s_updates_pending_id_seq",
						model.tableName ()));

				sqlStatements.add (
					formatWriter.toString ());

			}

			// create table

			try (

				LazyFormatWriter formatWriter =
					new LazyFormatWriter ("  ");

			) {

				formatWriter.writeLineFormatIncreaseIndent (
					"CREATE TABLE %s (",
					sqlLogic.quoteIdentifierFormat (
						"%s_updates_pending",
						model.tableName ()));

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

				sqlStatements.add (
					formatWriter.toString ());

			}

			// create trigger

			try (

				LazyFormatWriter formatWriter =
					new LazyFormatWriter ("  ");

			) {

				formatWriter.writeLineFormat (
					"CREATE OR REPLACE FUNCTION %s ()",
					stringFormat (
						"%s_update_log_insert",
						model.tableName ()));

				formatWriter.writeLineFormat (
					"RETURNS trigger AS $$");

				formatWriter.writeLineFormatIncreaseIndent (
					"BEGIN");

				formatWriter.writeNewline ();

				formatWriter.writeLineFormatIncreaseIndent (
					"IF TG_OP IN ('UPDATE', 'DELETE') THEN");

				formatWriter.writeNewline ();

				formatWriter.writeLineFormatIncreaseIndent (
					"INSERT INTO %s VALUES (",
					sqlLogic.quoteIdentifierFormat (
						"%s_updates_pending",
						model.tableName ()));

				List <String> minusValues =
					new ArrayList<> ();

				minusValues.add (
					"DEFAULT");

				for (
					String triggerIndexColumn
						: triggerIndexColumns
				) {

					minusValues.add (
						stringFormat (
							"OLD.%s",
							triggerIndexColumn));

				}

				for (
					Pair <String, String> triggerValueColumn
						: triggerValueColumns
				) {

					minusValues.add (
						stringFormat (
							"CASE WHEN %s THEN -1 ELSE 0 END",
							stringReplaceAllSimple (
								"$",
								"OLD",
								triggerValueColumn.right ())));

				}

				for (
					String minusValue
						: listSliceAllButLastItemRequired (
							minusValues)
				) {

					formatWriter.writeLineFormat (
						"%s,",
						minusValue);

				}

				formatWriter.writeLineFormat (
					"%s",
					listLastItemRequired (
						minusValues));

				formatWriter.writeLineFormatDecreaseIndent (
					");");

				formatWriter.writeNewline ();

				formatWriter.writeLineFormatDecreaseIndent (
					"END IF;");

				formatWriter.writeNewline ();

				formatWriter.writeLineFormatIncreaseIndent (
					"IF TG_OP IN ('INSERT', 'UPDATE') THEN");

				formatWriter.writeNewline ();

				formatWriter.writeLineFormatIncreaseIndent (
					"INSERT INTO %s VALUES (",
					sqlLogic.quoteIdentifierFormat (
						"%s_updates_pending",
						model.tableName ()));

				List <String> plusValues =
					new ArrayList<> ();

				plusValues.add (
					"DEFAULT");

				for (
					String triggerIndexColumn
						: triggerIndexColumns
				) {

					plusValues.add (
						stringFormat (
							"NEW.%s",
							triggerIndexColumn));

				}

				for (
					Pair <String, String> triggerValueColumn
						: triggerValueColumns
				) {

					plusValues.add (
						stringFormat (
							"CASE WHEN %s THEN 1 ELSE 0 END",
							stringReplaceAllSimple (
								"$",
								"NEW",
								triggerValueColumn.right ())));

				}

				for (
					String plugValue
						: listSliceAllButLastItemRequired (
							plusValues)
				) {

					formatWriter.writeLineFormat (
						"%s,",
						plugValue);

				}

				formatWriter.writeLineFormat (
					"%s",
					listLastItemRequired (
						plusValues));

				formatWriter.writeLineFormatDecreaseIndent (
					");");

				formatWriter.writeNewline ();

				formatWriter.writeLineFormatDecreaseIndent (
					"END IF;");

				formatWriter.writeNewline ();

				formatWriter.writeLineFormat (
					"RETURN NULL;");

				formatWriter.writeNewline ();

				formatWriter.writeLineFormatDecreaseIndent (
					"END;");

				formatWriter.writeLineFormatDecreaseIndent (
					"$$ LANGUAGE 'plpgsql';");

				sqlStatements.add (
					formatWriter.toString ());

			}

			// create triggers

			sqlStatements.add (
				stringFormat (
					"CREATE TRIGGER %s\n",
					sqlLogic.quoteIdentifierFormat (
						"%s_after_insert_%s_updates_pending",
						sourceModel.tableName (),
						model.tableName ()),
					"AFTER INSERT ON %s\n",
					sqlLogic.quoteIdentifier (
						sourceModel.tableName ()),
					"FOR EACH ROW\n",
					"EXECUTE PROCEDURE %s ();\n",
					sqlLogic.quoteIdentifierFormat (
						"%s_update_log_insert",
						model.tableName ())));

			sqlStatements.add (
				stringFormat (
					"CREATE TRIGGER %s\n",
					sqlLogic.quoteIdentifierFormat (
						"%s_after_update_%s_updates_pending",
						sourceModel.tableName (),
						model.tableName ()),
					"AFTER UPDATE ON %s\n",
					sqlLogic.quoteIdentifier (
						sourceModel.tableName ()),
					"FOR EACH ROW\n",
					"EXECUTE PROCEDURE %s ();\n",
					sqlLogic.quoteIdentifierFormat (
						"%s_update_log_insert",
						model.tableName ())));

			sqlStatements.add (
				stringFormat (
					"CREATE TRIGGER %s\n",
					sqlLogic.quoteIdentifierFormat (
						"%s_after_delete_%s_updates_pending",
						sourceModel.tableName (),
						model.tableName ()),
					"AFTER DELETE ON %s\n",
					sqlLogic.quoteIdentifier (
						sourceModel.tableName ()),
					"FOR EACH ROW\n",
					"EXECUTE PROCEDURE %s ();\n",
					sqlLogic.quoteIdentifierFormat (
						"%s_update_log_insert",
						model.tableName ())));

		}

	}

}
