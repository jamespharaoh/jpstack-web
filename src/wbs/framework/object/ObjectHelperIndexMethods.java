package wbs.framework.object;

import java.util.List;

import com.google.common.base.Optional;

import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;

public
interface ObjectHelperIndexMethods<RecordType> {

	Optional<RecordType> findByIndex (
			Record<?> parent,
			Long index);

	RecordType findByIndexRequired (
			Record<?> parent,
			Long index);

	@Deprecated
	RecordType findByIndexOrNull (
			Record<?> parent,
			Long index);

	RecordType findByIndex (
			GlobalId parentGlobalId,
			Long index);

	List<RecordType> findByIndexRange (
			Record<?> parent,
			Long indexStart,
			Long indexEnd);

	List<RecordType> findByIndexRange (
			GlobalId parentGlobalId,
			Long indexStart,
			Long indexEnd);

}
