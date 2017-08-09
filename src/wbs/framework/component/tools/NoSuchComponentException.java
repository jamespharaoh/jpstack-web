package wbs.framework.component.tools;

public
class NoSuchComponentException
	extends RuntimeException {

	private static final
	long serialVersionUID = -3974499830789636133L;

	public
	NoSuchComponentException () {
	}

	public
	NoSuchComponentException (
			String message) {

		super (
			message);

	}

}
