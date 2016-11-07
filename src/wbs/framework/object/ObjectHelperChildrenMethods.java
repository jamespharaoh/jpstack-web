package wbs.framework.object;

import static wbs.utils.etc.TypeUtils.dynamicCast;

import java.util.List;

import wbs.framework.codegen.DoNotDelegate;
import wbs.framework.entity.record.Record;

public
interface ObjectHelperChildrenMethods <
	RecordType extends Record <RecordType>
> {

	@DoNotDelegate
	ObjectHelper <RecordType> objectHelper ();

	List <Record <?>> getChildren (
			RecordType object);

	<ChildType extends Record<?>>
	List <ChildType> getChildren (
			RecordType object,
			Class <ChildType> childClass);

	default <ChildType extends Record<?>>
	List <ChildType> getChildrenGeneric (
			Record <?> object,
			Class <ChildType> childClass) {

		return getChildren (
			dynamicCast (
				objectHelper ().objectClass (),
				object),
			childClass);

	}

	List <Record <?>> getMinorChildren (
			RecordType object);

	default
	List <Record <?>> getMinorChildrenGeneric (
			Record <?> object) {

		return getMinorChildren (
			dynamicCast (
				objectHelper ().objectClass (),
				object));

	}

}
