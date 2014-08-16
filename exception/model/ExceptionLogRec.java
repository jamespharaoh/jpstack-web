package wbs.platform.exception.model;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.platform.user.model.UserRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity (
	table = "exception")
public
class ExceptionLogRec
	implements EphemeralRecord<ExceptionLogRec> {

	// id

	@GeneratedIdField (
		sequence = "exception_id_seq")
	Integer id;

	// details

	@ReferenceField
	ExceptionLogTypeRec type;

	@ReferenceField (
		nullable = true)
	UserRec user;

	@SimpleField (
		sqlType = "timestamp with time zone")
	Date timestamp = new Date ();

	@SimpleField
	String source;

	@SimpleField
	String summary;

	@SimpleField
	String dump;

	@SimpleField
	Boolean alert = true;

	@SimpleField
	Boolean fatal;

	// compare to

	@Override
	public
	int compareTo (
			Record<ExceptionLogRec> otherRecord) {

		ExceptionLogRec other =
			(ExceptionLogRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	// object hooks

	public static
	class ExceptionLogHooks
		extends AbstractObjectHooks<ExceptionLogRec> {

		@Inject
		ExceptionLogDao exceptionLogDao;

		@Override
		public
		List<Integer> searchIds (
				Object searchObject) {

			ExceptionLogSearch search =
				(ExceptionLogSearch) searchObject;

			return exceptionLogDao.searchIds (
				search);

		}

	}

	// dao methods

	public static
	interface ExceptionLogDaoMethods {

		int countWithAlert ();

		int countWithAlertAndFatal ();

		List<Integer> searchIds (
				ExceptionLogSearch search);

	}

}
