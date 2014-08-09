package wbs.platform.console.helper;

import wbs.framework.entity.model.ModelMethods;
import wbs.framework.object.ObjectHelperMethods;
import wbs.framework.record.Record;
import wbs.platform.console.forms.EntityFinder;
import wbs.platform.console.lookup.ObjectLookup;

public
interface ConsoleHelper<ObjectType extends Record<ObjectType>>
	extends
		ConsoleHelperMethods<ObjectType>,
		EntityFinder<ObjectType>,
		ModelMethods,
		ObjectHelperMethods<ObjectType>,
		ObjectLookup<ObjectType> {

}
