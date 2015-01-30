package wbs.wallet.model;

import java.util.Random;

import javax.inject.Inject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.scaffold.model.SliceRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public
class WalletServiceRec
	implements CommonRecord<WalletServiceRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SliceRec slice;

	@CodeField
	String code;

	// object helper methods

	public
	interface WalletServiceObjectHelperMethods {

		String generateCode ();

	}

	// object helper implementation

	public static
	class WalletServiceObjectHelperImplementation
		implements WalletServiceObjectHelperMethods {

		// dependencies

		@Inject
		Random random;

		// implementation

		@Override
		public
		String generateCode () {

			int intCode =
				+ random.nextInt (90000000)
				+ 10000000;

			return Integer.toString (
				intCode);

		}

	}

	// compare to

	@Override
	public
	int compareTo (
			Record<WalletServiceRec> otherRecord) {

		WalletServiceRec other =
			(WalletServiceRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getCode (),
				other.getCode ())

			.append (
				getSlice (),
				other.getSlice ())

			.toComparison ();

	}

}
