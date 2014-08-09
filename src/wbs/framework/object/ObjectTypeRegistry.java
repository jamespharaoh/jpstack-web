package wbs.framework.object;

import java.util.List;

public
interface ObjectTypeRegistry {

	ObjectTypeEntry findById (
			int id);

	ObjectTypeEntry findByCode (
			String code);

	List<? extends ObjectTypeEntry> findAll ();

}
