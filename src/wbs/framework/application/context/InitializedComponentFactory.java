package wbs.framework.application.context;

public
interface InitializedComponentFactory
	extends ComponentFactory {

	@Override
	default 
	Boolean initialized () {
		return true;
	}

}
