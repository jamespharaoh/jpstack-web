package wbs.framework.hibernate;

import javax.annotation.PostConstruct;

import org.hibernate.EmptyInterceptor;

import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("hibernateInterceptor")
public
class HibernateInterceptor
	extends EmptyInterceptor {

	// state

	Class <?> chatUserClass;

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
