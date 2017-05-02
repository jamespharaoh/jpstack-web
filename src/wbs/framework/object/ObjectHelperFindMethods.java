package wbs.framework.object;

import java.util.List;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;

public
interface ObjectHelperFindMethods <
	RecordType extends Record <RecordType>
> {

	List <RecordType> findAll (
			Transaction parentTransaction);

	List <RecordType> findNotDeleted (
			Transaction parentTransaction);

	List <RecordType> findByParent (
			Transaction parentTransaction,
			Record <?> parent);

	List <RecordType> findByParent (
			Transaction parentTransaction,
			GlobalId parentGlobalId);

	List <RecordType> findByParentAndType (
			Transaction parentTransaction,
			Record <?> parent,
			String typeCode);

	List <RecordType> findByParentAndType (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			String typeCode);

	List <RecordType> search (
			Transaction parentTransaction,
			Object search);

	List <Long> searchIds (
			Transaction parentTransaction,
			Object search);

}
