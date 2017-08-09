package wbs.framework.object;

import static wbs.utils.string.StringUtils.underscoreToCamel;

import java.util.List;

import lombok.NonNull;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

public
interface ObjectTypeRegistry {

	// database methods

	ObjectTypeEntry findById (
			Transaction parentTransaction,
			Long id);

	ObjectTypeEntry findByCode (
			Transaction parentTransaction,
			String code);

	List <? extends ObjectTypeEntry> findAll (
			Transaction parentTransaction);

	Class <? extends Record <?>> objectTypeRecordClass ();

	Class <? extends Record <?>> rootRecordClass ();

	// cached data methods

	boolean codeExists (
			String code);

	Long typeIdForCodeRequired (
			String code);

	boolean typeIdExists (
			Long typeId);

	String codeForTypeIdRequired (
			Long typeId);

	default
	String nameForTypeIdRequired (
			@NonNull Long typeId) {

		return underscoreToCamel (
			codeForTypeIdRequired (
				typeId));

	}

}
