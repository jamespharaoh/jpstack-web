package wbs.framework.object;

import java.util.List;

import wbs.framework.entity.record.Record;

public
interface ObjectTypeRegistry {

	// database methods

	ObjectTypeEntry findById (
			Long id);

	ObjectTypeEntry findByCode (
			String code);

	List <? extends ObjectTypeEntry> findAll ();

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

}
