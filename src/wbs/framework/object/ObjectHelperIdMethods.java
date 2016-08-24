package wbs.framework.object;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.google.common.base.Optional;

import wbs.framework.entity.record.Record;

public
interface ObjectHelperIdMethods<RecordType extends Record<RecordType>> {

	Optional<RecordType> find (
			Long id);

	@Nonnull
	RecordType findRequired (
			Long id);

	@Deprecated
	RecordType findOrNull (
			Long id);

	RecordType findOrThrow (
			Long id,
			Supplier<? extends RuntimeException> orThrow);

	List<RecordType> findManyRequired (
			List<Long> ids);

}
