package wbs.apn.chat.user.core.model;

public enum Orient {

	gay,
	straight,
	bi;

	public boolean doesSame () {
		return this != straight;
	}

	public boolean doesDifferent () {
		return this != gay;
	}

}
