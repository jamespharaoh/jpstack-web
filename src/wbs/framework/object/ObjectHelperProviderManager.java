package wbs.framework.object;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;

@SingletonComponent ("objectHelperProviderManager")
@Log4j
public
class ObjectHelperProviderManager {

	@Inject
	Database database;

	@Inject
	ObjectTypeRegistry objectTypeRegistry;

	@Inject
	Map<String,ObjectHelperProvider> objectHelperProvidersByBeanName =
		Collections.emptyMap ();

	List<ObjectHelperProvider> objectHelperProviders =
		new ArrayList<ObjectHelperProvider> ();

	Map<Class<?>,ObjectHelperProvider> objectHelperProvidersByObjectClass =
		new HashMap<Class<?>,ObjectHelperProvider> ();

	Map<Integer,ObjectHelperProvider> objectHelperProvidersByTypeId =
		new HashMap<Integer,ObjectHelperProvider> ();

	@PostConstruct
	public
	void init () {

		log.info (
			stringFormat (
				"Initialising"));

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		for (
			Map.Entry<String,ObjectHelperProvider> entry
				: objectHelperProvidersByBeanName.entrySet ()
		) {

			String beanName =
				entry.getKey ();

			ObjectHelperProvider objectHelperProvider =
				entry.getValue ();

			// verify the object helper provider

			// TODO

			// check for dupes

			if (objectHelperProvidersByObjectClass.containsKey (
					objectHelperProvider.objectClass ()))
				throw new RuntimeException ();

			// find object type

			ObjectTypeEntry objectType =
				objectTypeRegistry.findByCode (
					objectHelperProvider.objectTypeCode ());

			if (objectType == null) {

				log.error (
					stringFormat (
						"Object type not found: %s",
						objectHelperProvider.objectTypeCode ()));

					continue;

			}

			// store with various indexes

			log.debug (
				stringFormat (
					"Registering object helper provider %s %s %s from %s",
					objectHelperProvider.objectName (),
					objectHelperProvider.objectTypeCode (),
					objectType.getId (),
					beanName));

			objectHelperProviders.add (
				objectHelperProvider);

			objectHelperProvidersByObjectClass.put (
				objectHelperProvider.objectClass (),
				objectHelperProvider);

			objectHelperProvidersByTypeId.put (
				objectType.getId (),
				objectHelperProvider);

		}

	}

	public
	ObjectHelperProvider forObjectClass (
			Class<?> objectClass) {

		return objectHelperProvidersByObjectClass.get (
			objectClass);

	}

	public
	ObjectHelperProvider forObjectTypeId (
			@NonNull Integer objectTypeId) {

		return objectHelperProvidersByTypeId.get (
			objectTypeId);

	}

	public
	List<ObjectHelperProvider> list () {

		return objectHelperProviders;

	}

	public
	ObjectHelperProvider forObjectClassSearch (
			Class<?> objectClass) {

		while (objectClass != Object.class) {

			ObjectHelperProvider ret =
				forObjectClass (objectClass);

			if (ret != null)
				return ret;

			objectClass =
				objectClass.getSuperclass ();

		}

		return null;

	}

}
