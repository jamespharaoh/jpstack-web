package wbs.psychic.contact.model;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.queue.model.QueueItemRec;
import wbs.psychic.profile.model.PsychicProfileRec;
import wbs.psychic.request.model.PsychicRequestRec;
import wbs.psychic.user.core.model.PsychicUserRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class PsychicContactRec
	implements CommonRecord<PsychicContactRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	PsychicUserRec psychicUser;

	@IndexField
	Integer index;

	@ReferenceField
	PsychicProfileRec psychicProfile;

	@SimpleField (nullable = true)
	Instant firstProfile;

	@SimpleField (nullable = true)
	Instant firstRequest;

	@SimpleField (nullable = true)
	Instant firstResponse;

	@SimpleField (nullable = true)
	Instant lastProfile;

	@SimpleField (nullable = true)
	Instant lastRequest;

	@SimpleField (nullable = true)
	Instant lastResponse;

	@SimpleField
	Integer numProfiles = 0;

	@SimpleField
	Integer numRequests = 0;

	@SimpleField
	Integer numResponses = 0;

	@ReferenceField (nullable = true)
	QueueItemRec queueItem;

	@CollectionField (
		index = "index")
	Map<Integer,PsychicRequestRec> requestsByIndex =
		new LinkedHashMap<Integer,PsychicRequestRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<PsychicContactRec> otherRecord) {

		PsychicContactRec other =
			(PsychicContactRec) otherRecord;

		return new CompareToBuilder ()
			.append (getPsychicUser (), other.getPsychicUser ())
			.append (getPsychicProfile (), other.getPsychicProfile ())
			.toComparison ();

	}

}
