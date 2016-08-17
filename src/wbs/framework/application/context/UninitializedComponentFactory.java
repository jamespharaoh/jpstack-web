package wbs.framework.application.context;

public
interface UninitializedComponentFactory
	extends ComponentFactory {

	@Override
	default 
	Boolean initialized () {
		return false;
	}

}
