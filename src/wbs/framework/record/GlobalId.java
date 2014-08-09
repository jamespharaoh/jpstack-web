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
	int typeId;

	@Getter
	int objectId;

	public
	GlobalId (
			int newTypeId,
			int newObjectId) {

		typeId =
			newTypeId;

		objectId =
			newObjectId;

	}

	public final static
	GlobalId root =
		new GlobalId (
			0,
			0);

}
