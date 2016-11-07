package wbs.framework.object;

import java.util.function.Supplier;

import com.google.common.base.Optional;

import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;

public
interface ObjectHelperCodeMethods <
	RecordType extends Record <RecordType>
> {

	Optional <RecordType> findByCode (
			Record <?> parent,
			String ... code);

	RecordType findByCodeRequired (
			Record <?> parent,
			String ... code);

	@Deprecated
	RecordType findByCodeOrNull (
			Record <?> parent,
			String ... code);

	Optional<RecordType> findByCode (
			GlobalId parentGlobalId,
			String ... code);

	RecordType findByCodeRequired (
			GlobalId parentGlobalId,
			String ... code);

	RecordType findByCodeOrThrow (
			GlobalId parentGlobalId,
			String code,
			Supplier <? extends RuntimeException> orThrow);

	RecordType findByCodeOrThrow (
			Record <?> parent,
			String code,
			Supplier <? extends RuntimeException> orThrow);

	RecordType findByCodeOrThrow (
			GlobalId parentGlobalId,
			String code0,
			String code1,
			Supplier <? extends RuntimeException> orThrow);

	RecordType findByCodeOrThrow (
			Record <?> parent,
			String code0,
			String code1,
			Supplier <? extends RuntimeException> orThrow);

	@Deprecated
	RecordType findByCodeOrNull (
			GlobalId parentGlobalId,
			String ... code);

	RecordType findByTypeAndCode (
			Record <?> parent,
			String typeCode,
			String ... code);

	RecordType findByTypeAndCode (
			GlobalId parentGlobalId,
			String typeCode,
			String ... code);

}
