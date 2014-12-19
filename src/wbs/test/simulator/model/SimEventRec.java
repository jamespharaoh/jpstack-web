package wbs.test.simulator.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jadira.usertype.dateandtime.joda.PersistentInstantAsTimestamp;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class SimEventRec
	implements CommonRecord<SimEventRec> {

	// id

	@GeneratedIdField
	Integer id;

	// details

	@SimpleField (
		hibernateTypeHelper = PersistentInstantAsTimestamp.class)
	Instant timestamp;

	@SimpleField
	String type;

	@SimpleField
	String data;

	// compare to

	@Override
	public
	int compareTo (
			Record<SimEventRec> otherRecord) {

		SimEventRec other =
			(SimEventRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	// dao methods

	public static
	interface SimEventDaoMethods {

		List<SimEventRec> findAfterLimit (
				int afterId,
				int maxResults);

	}

}
