package wbs.applications.imchat.model;

import java.util.Random;

import javax.inject.Inject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.integrations.paypal.model.PaypalPaymentRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public
class ImChatPurchaseRec
	implements CommonRecord<ImChatPurchaseRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ImChatCustomerRec imChatCustomer;

	@IndexField
	Integer index;

	// details

	@ReferenceField
	ImChatPricePointRec imChatPricePoint;

	@SimpleField
	Integer price;

	@SimpleField
	Integer value;

	@SimpleField
	Instant createdTime;

	@SimpleField (
		nullable = true)
	Instant completedTime;

	@SimpleField (
		nullable = true)
	Instant cancelledTime;

	@ReferenceField (
		nullable = true)
	PaypalPaymentRec paypalPayment;

	@SimpleField (
		nullable = true)
	String token;

	// compare to

	@Override
	public
	int compareTo (
			Record<ImChatPurchaseRec> otherRecord) {

		ImChatPurchaseRec other =
			(ImChatPurchaseRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getCreatedTime (),
				getCreatedTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	// dao methods

	public
	interface ImChatPurchaseDaoMethods {

		ImChatPurchaseRec findByToken (
				String token);

	}

	// object helper methods

	public
	interface ImChatPurchaseObjectHelperMethods {

		String generateToken ();

	}

	// object helper implementation

	public static
	class ImChatPurchaseObjectHelperImplementation
		implements ImChatPurchaseObjectHelperMethods {

		// dependencies

		@Inject
		Random random;

		// implementation

		@Override
		public
		String generateToken () {

			StringBuilder stringBuilder =
				new StringBuilder ();

			for (int i = 0; i < 20; i ++) {

				stringBuilder.append (
					tokenChars.charAt (
						random.nextInt (
							tokenChars.length ())));

			}

			return stringBuilder.toString ();

		}

		// data

		public static
		String tokenChars = "abcdefghijklmnopqrstuvwxyz";

	}

}
