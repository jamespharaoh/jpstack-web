package wbs.wallet.model;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public
class WalletRec
	implements CommonRecord<WalletRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	WalletServiceRec walletServiceRec;

	@CodeField
	String code;

	// compare to

	@Override
	public
	int compareTo (
			Record<WalletRec> otherRecord) {

		WalletRec other =
			(WalletRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getWalletServiceRec (),
				other.getWalletServiceRec ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
