package wbs.framework.application.context;

public
class NoSuchBeanException
	extends RuntimeException {

	private static final
	long serialVersionUID = -3974499830789636133L;

	public
	NoSuchBeanException () {
	}

	public
	NoSuchBeanException (
			String message) {

		super (
			message);

	}

}
