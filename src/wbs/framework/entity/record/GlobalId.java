package wbs.framework.entity.record;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@EqualsAndHashCode
@ToString
public
class GlobalId {

	@Getter
	Long typeId;

	@Getter
	Long objectId;

	public
	GlobalId (
			@NonNull Long newTypeId,
			@NonNull Long newObjectId) {

		typeId =
			newTypeId;

		objectId =
			newObjectId;

	}

	public static
	GlobalId of (
			@NonNull Long typeId,
			@NonNull Long objectId) {

		return new GlobalId (
			typeId,
			objectId);

	}


	public final static
	GlobalId root =
		new GlobalId (
			0l,
			0l);

}
