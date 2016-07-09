package wbs.framework.record;

import lombok.NonNull;

public
interface IdObject {

	Integer getId ();

	public static
	Long objectId (
			@NonNull IdObject object) {

		return (long) (int)
			object.getId ();

	}

}
