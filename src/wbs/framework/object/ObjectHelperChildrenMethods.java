package wbs.framework.object;

import static wbs.utils.etc.TypeUtils.dynamicCastRequired;

import java.util.List;

import lombok.NonNull;

import wbs.framework.codegen.DoNotDelegate;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

public
interface ObjectHelperChildrenMethods <
	RecordType extends Record <RecordType>
> {

	@DoNotDelegate
	ObjectHelper <RecordType> objectHelper ();

	List <Record <?>> getChildren (
			Transaction parentTransaction,
			RecordType object);

	<ChildType extends Record <?>>
	List <ChildType> getChildren (
			Transaction parentTransaction,
			RecordType object,
			Class <ChildType> childClass);

	default <ChildType extends Record <?>>
	List <ChildType> getChildrenGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object,
			@NonNull Class <ChildType> childClass) {

		return getChildren (
			parentTransaction,
			dynamicCastRequired (
				objectHelper ().objectClass (),
				object),
			childClass);

	}

	List <Record <?>> getMinorChildren (
			Transaction parentTransaction,
			RecordType object);

	default
	List <Record <?>> getMinorChildrenGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Record <?> object) {

		return getMinorChildren (
			parentTransaction,
			dynamicCastRequired (
				objectHelper ().objectClass (),
				object));

	}

}
