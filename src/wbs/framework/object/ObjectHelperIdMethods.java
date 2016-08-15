package wbs.framework.object;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.google.common.base.Optional;

import wbs.framework.record.Record;

public
interface ObjectHelperIdMethods<RecordType extends Record<RecordType>> {

	Optional<RecordType> find (
			long id);

	@Nonnull
	RecordType findRequired (
			long id);

	@Deprecated
	RecordType findOrNull (
			long id);

	RecordType findOrThrow (
			long id,
			Supplier<? extends RuntimeException> orThrow);

	List<RecordType> findManyRequired (
			List<Long> ids);

}
