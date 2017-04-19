package wbs.framework.object;

import java.util.List;

import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

public
interface ObjectHelperFindMethods <
	RecordType extends Record <RecordType>
> {

	List <RecordType> findAll ();

	List <RecordType> findNotDeleted ();

	List <RecordType> findByParent (
			Record <?> parent);

	List <RecordType> findByParent (
			GlobalId parentGlobalId);

	List <RecordType> findByParentAndType (
			Record <?> parent,
			String typeCode);

	List <RecordType> findByParentAndType (
			GlobalId parentGlobalId,
			String typeCode);

	List <RecordType> search (
			TaskLogger parentTaskLogger,
			Object search);

	List <Long> searchIds (
			TaskLogger parentTaskLogger,
			Object search);

}
