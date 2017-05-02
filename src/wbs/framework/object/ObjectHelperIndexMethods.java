package wbs.framework.object;

import java.util.List;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;

public
interface ObjectHelperIndexMethods <
	RecordType extends Record <RecordType>
> {

	Optional <RecordType> findByIndex (
			Transaction parentTransaction,
			Record <?> parent,
			Long index);

	RecordType findByIndexRequired (
			Transaction parentTransaction,
			Record <?> parent,
			Long index);

	@Deprecated
	RecordType findByIndexOrNull (
			Transaction parentTransaction,
			Record <?> parent,
			Long index);

	RecordType findByIndex (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			Long index);

	List <RecordType> findByIndexRange (
			Transaction parentTransaction,
			Record <?> parent,
			Long indexStart,
			Long indexEnd);

	List <RecordType> findByIndexRange (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			Long indexStart,
			Long indexEnd);

}
