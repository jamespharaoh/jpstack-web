package wbs.framework.fixtures;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectHelper;

public
interface FixturesLogic {

	Map <String, Object> resolveParams (
			Transaction parentTransaction,
			ObjectHelper <?> objectHelper,
			Map <String, String> unresolvedParams,
			Set <String> ignoreParams,
			Map <Class <?>, Function <String, ?>> recordLookups);

	<Type extends Record <Type>>
	Type createRecord (
			Transaction parentTransaction,
			ObjectHelper <Type> objectHelper,
			Record <?> parent,
			Map <String, Object> resolvedParams);

	Function <String, String> placeholderFunction (
			Map <String, Object> hints,
			Map <String, String> params);

}
