package wbs.integrations.hybyte.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.sms.network.model.NetworkRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class HybyteNetworkRec
	implements MajorRecord<HybyteNetworkRec> {

	// id

	@ForeignIdField (
		field = "network")
	Integer id;

	// identity

	@MasterField
	NetworkRec network;

	// settings

	@SimpleField
	String text;

	@SimpleField
	String inText;

	@Override
	public
	int compareTo (
			Record<HybyteNetworkRec> otherRecord) {

		HybyteNetworkRec other =
			(HybyteNetworkRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getNetwork (),
				other.getNetwork ())

			.toComparison ();

	}

}
