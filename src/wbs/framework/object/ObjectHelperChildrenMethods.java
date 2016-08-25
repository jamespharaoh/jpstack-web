package wbs.framework.object;

import java.util.List;

import wbs.framework.entity.record.Record;

public
interface ObjectHelperChildrenMethods<RecordType extends Record<RecordType>> {

	List <Record <?>> getChildren (
			Record <?> object);

	<ChildType extends Record<?>>
	List <ChildType> getChildren (
			Record <?> object,
			Class <ChildType> childClass);

	List <Record <?>> getMinorChildren (
			Record <?> object);

}
