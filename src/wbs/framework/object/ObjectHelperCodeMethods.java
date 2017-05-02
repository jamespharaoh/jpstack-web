package wbs.framework.object;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;

public
interface ObjectHelperCodeMethods <
	RecordType extends Record <RecordType>
> {

	Optional <RecordType> findByCode (
			Transaction parentTransaction,
			Record <?> parent,
			String ... code);

	RecordType findByCodeRequired (
			Transaction parentTransaction,
			Record <?> parent,
			String ... code);

	@Deprecated
	RecordType findByCodeOrNull (
			Transaction parentTransaction,
			Record <?> parent,
			String ... code);

	Optional <RecordType> findByCode (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			String ... code);

	List <Optional <RecordType>> findManyByCode (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			List <String> code);

	RecordType findByCodeRequired (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			String ... code);

	RecordType findByCodeOrThrow (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			String code,
			Supplier <? extends RuntimeException> orThrow);

	RecordType findByCodeOrThrow (
			Transaction parentTransaction,
			Record <?> parent,
			String code,
			Supplier <? extends RuntimeException> orThrow);

	RecordType findByCodeOrThrow (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			String code0,
			String code1,
			Supplier <? extends RuntimeException> orThrow);

	RecordType findByCodeOrThrow (
			Transaction parentTransaction,
			Record <?> parent,
			String code0,
			String code1,
			Supplier <? extends RuntimeException> orThrow);

	@Deprecated
	RecordType findByCodeOrNull (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			String ... code);

	RecordType findByTypeAndCode (
			Transaction parentTransaction,
			Record <?> parent,
			String typeCode,
			String ... code);

	RecordType findByTypeAndCode (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			String typeCode,
			String ... code);

}
