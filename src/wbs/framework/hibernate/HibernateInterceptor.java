package wbs.framework.hibernate;

import static wbs.utils.etc.Misc.doNothing;

import org.hibernate.EmptyInterceptor;

import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("hibernateInterceptor")
public
class HibernateInterceptor
	extends EmptyInterceptor {

	// state

	Class <?> chatUserClass;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup () {

		doNothing ();

	}

}
