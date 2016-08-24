package wbs.framework.object;

import java.util.List;

import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;

public
interface ObjectHelperFindMethods<RecordType extends Record<RecordType>> {

	List<RecordType> findAll ();

	List<RecordType> findByParent (
			Record<?> parent);

	List<RecordType> findByParent (
			GlobalId parentGlobalId);

	List<RecordType> findByParentAndType (
			Record<?> parent,
			String typeCode);

	List<RecordType> findByParentAndType (
			GlobalId parentGlobalId,
			String typeCode);

	List<RecordType> search (
			Object search);

	List<Long> searchIds (
			Object search);

}
