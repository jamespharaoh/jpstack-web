package wbs.integrations.paypal.model;

import java.util.Random;

import javax.inject.Inject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

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
class PaypalPaymentRec
	implements CommonRecord<PaypalPaymentRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	PaypalAccountRec paypalAccount;

	@SimpleField
	Integer value;

	@SimpleField
	PaypalPaymentState state;

	@SimpleField
	String token;

	// compare to

	@Override
	public
	int compareTo (
			Record<PaypalPaymentRec> otherRecord) {

		PaypalPaymentRec other =
			(PaypalPaymentRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	// dao methods

	public
	interface PaypalPaymentDaoMethods {

		PaypalPaymentRec findByToken (
				String token);

	}

	// object helper methods

	public
	interface PaypalPaymentObjectHelperMethods {

		String generateToken ();

	}

	// object helper implementation

	public static
	class PaypalPaymentObjectHelperImplementation
		implements PaypalPaymentObjectHelperMethods {

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
					chars.charAt (
						random.nextInt (
							chars.length ())));

			}

			return stringBuilder.toString ();

		}

		// data

		public static
		String chars = "abcdefghijklmnopqrstuvwxyz0123456789";

	}

}
