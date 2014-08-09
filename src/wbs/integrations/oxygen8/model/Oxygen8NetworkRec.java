package wbs.integrations.oxygen8.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.network.model.NetworkRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class Oxygen8NetworkRec
	implements CommonRecord<Oxygen8NetworkRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	Oxygen8ConfigRec oxygen8Config;

	@IdentityReferenceField
	NetworkRec network;

	// settings

	@SimpleField
	String channel;

	// compare to

	@Override
	public
	int compareTo (
			Record<Oxygen8NetworkRec> otherRecord) {

		Oxygen8NetworkRec other =
			(Oxygen8NetworkRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getNetwork (),
				other.getNetwork ())

			.toComparison ();

	}

	// dao methods

	public static
	interface Oxygen8NetworkDaoMethods {

		Oxygen8NetworkRec find (
				Oxygen8ConfigRec oxygen8Config,
				NetworkRec network);

		Oxygen8NetworkRec findByChannel (
				Oxygen8ConfigRec oxygen8Config,
				String channel);

	}

}
