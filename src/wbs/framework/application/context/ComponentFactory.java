package wbs.framework.application.context;

public 
interface ComponentFactory {

	Object makeComponent ();

	Boolean initialized ();

}
