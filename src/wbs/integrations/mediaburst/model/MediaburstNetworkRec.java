package wbs.integrations.mediaburst.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.sms.network.model.NetworkRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MinorEntity
public
class MediaburstNetworkRec
	implements MinorRecord<MediaburstNetworkRec> {

	@ForeignIdField (
		field = "network")
		Integer id;

	@MasterField
	NetworkRec network;

	@SimpleField
	Integer otherId;

	@Override
	public
	int compareTo (
			Record<MediaburstNetworkRec> otherRecord) {

		MediaburstNetworkRec other =
			(MediaburstNetworkRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getNetwork (),
				other.getNetwork ())

			.toComparison ();

	}

}
