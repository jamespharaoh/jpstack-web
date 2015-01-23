package wbs.wallet.model;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public class WalletServiceRec
	implements CommonRecord<WalletServiceRec>{
	
	// identity

	@GeneratedIdField
	Integer id;
	
	@CodeField
	String code;

	@Override
	public int compareTo(Record<WalletServiceRec> otherRecord) {
		
		WalletServiceRec other =
				(WalletServiceRec) otherRecord;
		
		return new CompareToBuilder ()
		
		.append (
				getCode (),
				other.getCode ())
		
		.toComparison ();
		
	}
	
}