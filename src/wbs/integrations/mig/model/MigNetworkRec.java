package wbs.integrations.mig.model;

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
class MigNetworkRec
	implements MajorRecord<MigNetworkRec> {

	@ForeignIdField (
		field = "network")
	Integer id;

	@MasterField
	NetworkRec network;

	@SimpleField
	String suffix;

	@SimpleField
	Boolean virtual;

	@Override
	public
	int compareTo (
			Record<MigNetworkRec> otherRecord) {

		MigNetworkRec other =
			(MigNetworkRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getNetwork (),
				other.getNetwork ())

			.toComparison ();

	}

	public static
	interface MigNetworkDaoMethods {

		MigNetworkRec findBySuffix (
				String suffix);

	}

}
