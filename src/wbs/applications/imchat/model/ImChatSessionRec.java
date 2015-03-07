package wbs.applications.imchat.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public
class ImChatSessionRec
	implements CommonRecord<ImChatSessionRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ImChatCustomerRec imChatCustomer;

	@CodeField
	String secret;

	// details

	@SimpleField
	Instant startTime;

	@SimpleField
	Instant updateTime;

	@SimpleField (
		nullable = true)
	Instant endTime;

	// state

	@SimpleField
	Boolean active;

	// compare to

	@Override
	public
	int compareTo (
			Record<ImChatSessionRec> otherRecord) {

		ImChatSessionRec other =
			(ImChatSessionRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getStartTime (),
				getStartTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	// dao methods

	public
	interface ImChatSessionDaoMethods {

		ImChatSessionRec findBySecret (
				String secret);

	}

}
