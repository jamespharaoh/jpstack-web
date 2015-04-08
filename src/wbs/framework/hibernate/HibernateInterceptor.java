package wbs.framework.hibernate;

import static wbs.framework.utils.etc.Misc.notIn;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectManager;

@PrototypeComponent ("hibernateInterceptor")
public
class HibernateInterceptor
	extends EmptyInterceptor {

	// indirect dependencies

	@Inject
	Provider<ObjectManager> objectManagerProvider;

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

		if (
			chatUserClass.isInstance (
				entity)
		) {

			System.out.println (
				stringFormat (
					"--- CHAT USER %s ---",
					id));

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
				stringFormat (
					"--- CHAT USER %s ---",
					id));

		}

		return false;

	}

}
