package wbs.framework.record;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@EqualsAndHashCode
@ToString
public
class GlobalId {

	@Getter
	long typeId;

	@Getter
	long objectId;

	public
	GlobalId (
			long newTypeId,
			long newObjectId) {

		typeId =
			newTypeId;

		objectId =
			newObjectId;

	}

	public static
	GlobalId of (
			long typeId,
			long objectId) {

		return new GlobalId (
			typeId,
			objectId);

	}
			

	public final static
	GlobalId root =
		new GlobalId (
			0,
			0);

}
