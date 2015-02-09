package wbs.imchat.core.model;

import java.util.Random;

import javax.inject.Inject;

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
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public
class ImChatSessionRec
	implements EphemeralRecord<ImChatSessionRec> {

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

	// object helper methods

	public
	interface ImChatSessionObjectHelperMethods {

		String generateSecret ();

	}

	// object helper implementation

	public static
	class ImChatSessionObjectHelperImplementation
		implements ImChatSessionObjectHelperMethods {

		// dependencies

		@Inject
		Random random;

		// implementation

		@Override
		public
		String generateSecret () {

			StringBuilder stringBuilder =
				new StringBuilder ();

			for (int i = 0; i < 20; i ++) {

				stringBuilder.append (
					chars.charAt (
						random.nextInt (
							chars.length ())));

			}

			return stringBuilder.toString ();

		}

		// data

		public static
		String chars = "abcdefghijklmnopqrstuvwxyz";

	}

}
