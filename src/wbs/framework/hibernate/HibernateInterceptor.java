package wbs.framework.hibernate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import org.hibernate.EmptyInterceptor;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.object.ObjectManager;

@PrototypeComponent ("hibernateInterceptor")
public
class HibernateInterceptor
	extends EmptyInterceptor {

	// indirect dependencies

	@Inject
	Provider<ObjectManager> objectManagerProvider;

	@Inject
	Provider<HibernateDatabase> hibernateDatabaseProvider;

	// state

	Class<?> chatUserClass;

	// life cycle

	@PostConstruct
	public
	void setup () {

		try {

			chatUserClass =
				Class.forName (
					"wbs.clients.apn.chat.user.core.model.ChatUserRec");

		} catch (ClassNotFoundException exception) {

			throw new RuntimeException (
				exception);

		}

	}

}
