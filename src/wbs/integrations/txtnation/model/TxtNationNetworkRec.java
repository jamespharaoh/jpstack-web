package wbs.integrations.txtnation.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.sms.network.model.NetworkRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class TxtNationNetworkRec
	implements MajorRecord<TxtNationNetworkRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@CodeField
	String code;

	// details

	@ReferenceField
	NetworkRec network;

	@DeletedField
	Boolean deleted = false;

	// compare to

	@Override
	public
	int compareTo (
			Record<TxtNationNetworkRec> otherRecord) {

		TxtNationNetworkRec other =
			(TxtNationNetworkRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
