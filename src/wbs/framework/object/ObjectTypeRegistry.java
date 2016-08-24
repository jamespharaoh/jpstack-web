package wbs.framework.object;

import java.util.List;

import wbs.framework.entity.record.Record;

public
interface ObjectTypeRegistry {

	ObjectTypeEntry findById (
			Long id);

	ObjectTypeEntry findByCode (
			String code);

	List<? extends ObjectTypeEntry> findAll ();

	Class<? extends Record<?>> objectTypeRecordClass ();

	Class<? extends Record<?>> rootRecordClass ();

}
