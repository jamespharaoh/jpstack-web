package wbs.framework.hibernate;

import static wbs.framework.utils.etc.Misc.notIn;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import lombok.extern.log4j.Log4j;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;

@Log4j
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

	// implementation

	@Override
	public
	boolean onFlushDirty (
			Object entity,
			Serializable id,
			Object[] currentState,
			Object[] previousState,
			String[] propertyNames,
			Type[] types) {

		try {

			HibernateDatabase hibernateDatabase =
				hibernateDatabaseProvider.get ();

			Transaction currentTransaction =
				hibernateDatabase.currentTransaction ();

			if (
				chatUserClass.isInstance (
					entity)
			) {

				String header =
					stringFormat (
						"--- CHAT USER %s (%s) [tx %s] ---",
						id,
						System.identityHashCode (
							entity),
						currentTransaction.getId ());

				System.out.println (
					header);

				for (
					int propertyIndex = 0;
					propertyIndex < propertyNames.length;
					propertyIndex ++
				) {

					String propertyName =
						propertyNames [propertyIndex];

					if (
						notIn (
							propertyName,
							"locationLongLat",
							"locationBackupLongLat",
							"locationTime",
							"locationPlace",
							"locationPlaceLongLat")
					) {
						continue;
					}

					Object currentValue =
						currentState [propertyIndex];

					Object previousValue =
						previousState [propertyIndex];

					System.out.println (
						stringFormat (
							"%s: %s -> %s",
							propertyName,
							previousValue != null
								? previousValue
								: "null",
							currentValue != null
								? currentValue
								: "null"));

				}

				System.out.println (
					header);

			}

			return false;

		} catch (Exception exception) {

			log.error (
				exception);

			return false;

		}

	}

}
