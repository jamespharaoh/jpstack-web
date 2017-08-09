package wbs.framework.entity.record;

import lombok.NonNull;

public
interface IdObject {

	Long getId ();

	public static
	Long objectId (
			@NonNull IdObject object) {

		return object.getId ();

	}

}
