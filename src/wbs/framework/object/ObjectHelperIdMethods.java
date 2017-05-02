package wbs.framework.object;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

public
interface ObjectHelperIdMethods <
	RecordType extends Record <RecordType>
> {

	Optional <RecordType> find (
			Transaction parentTransaction,
			Long id);

	@Nonnull
	RecordType findRequired (
			Transaction parentTransaction,
			Long id);

	@Deprecated
	RecordType findOrNull (
			Transaction parentTransaction,
			Long id);

	RecordType findOrThrow (
			Transaction parentTransaction,
			Long id,
			Supplier <? extends RuntimeException> orThrow);

	List <Optional <RecordType>> findMany (
			Transaction parentTransaction,
			List <Long> ids);

	List <RecordType> findManyRequired (
			Transaction parentTransaction,
			List <Long> ids);

}
