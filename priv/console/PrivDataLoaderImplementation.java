package wbs.platform.priv.console;

import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;

import org.joda.time.Instant;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import wbs.console.priv.UserPrivData;
import wbs.console.priv.UserPrivData.ObjectData;
import wbs.console.priv.UserPrivData.PrivPair;
import wbs.console.priv.UserPrivData.SharedData;
import wbs.console.priv.UserPrivData.UserData;
import wbs.console.priv.UserPrivDataLoader;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectManager;
import wbs.platform.group.model.GroupRec;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.priv.model.PrivObjectHelper;
import wbs.platform.priv.model.PrivRec;
import wbs.platform.updatelog.logic.UpdateManager;
import wbs.platform.updatelog.logic.UpdateManager.UpdateGetter;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserPrivRec;
import wbs.platform.user.model.UserRec;

@Log4j
@SingletonComponent ("privDataLoader")
public
class PrivDataLoaderImplementation
	implements UserPrivDataLoader {

	// dependencies

	@Inject
	ObjectTypeObjectHelper objectTypeHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	PrivObjectHelper privHelper;

	@Inject
	Database database;

	@Inject
	DataSource dataSource;

	@Inject
	UpdateManager updateManager;

	@Inject
	UserObjectHelper userHelper;

	// properties

	@Getter @Setter
	int reloadTimeSeconds = 60 * 15;

	// state

	UpdateGetter<SharedData> privDataCache;

	Map<Long,UpdateGetter<UserData>> userDataCachesByUserId =
		new HashMap<Long,UpdateGetter<UserData>>();

	// lifecycle

	@PostConstruct
	public
	void init () {

		privDataCache =
			updateManager.makeUpdateGetterAdaptor (
				new PrivDataReloader (),
				reloadTimeSeconds * 1000,
				"privs",
				0);

	}

	// implementation

	SharedData getPrivData () {

		return privDataCache
			.get ();

	}

	private
	UserData getUserData (
			Long userId) {

		Provider<UserData> userDataCache =
			getUserDataCache (
				userId);

		return userDataCache.get ();

	}

	@Override
	public
	UserPrivData getUserPrivData (
			Long userId) {

		UserPrivData ret =
			new UserPrivData ();

		ret.sharedData =
			getPrivData ();

		ret.userData =
			getUserData (
				userId);

		return ret;

	}

	@Override
	public synchronized
	void refresh () {

		log.info ("Refreshing");

		privDataCache.forceUpdate ();
		userDataCachesByUserId.clear ();

	}

	private synchronized
	UpdateGetter<UserData> getUserDataCache (
			Long userId) {

		UpdateGetter<UserData> ret =
			userDataCachesByUserId.get (
				userId);

		if (ret == null) {

			ret =
				updateManager.makeUpdateGetterAdaptor (
					new UserDataReloader (
						userId),
					reloadTimeSeconds * 1000,
					"user_privs",
					userId);

			userDataCachesByUserId.put (
				userId,
				ret);

		}

		return ret;

	}

	// ================================= priv data reloader

	private
	class PrivDataReloader
		implements Provider<SharedData> {

		@Override
		public
		SharedData get () {

			// create the data

			SharedData newData =
				new SharedData ();

			@Cleanup
			Transaction transaction =
				database.beginReadOnlyJoin (
					"PrivDataLoaderImplementation.PrivDataReloader.get ()",
					this);

			// start timer

			log.debug (
				"Priv reload started");

			Instant startTime =
				Instant.now ();

			// get privs, excluding any whose parent object types are
			// unknown, this is a hack.

			List<PrivRec> privs =
				new ArrayList<PrivRec> ();

			for (
				PrivRec priv
					: privHelper.findAll ()
			) {

				try {

					objectManager.getParent (
						priv);

					privs.add (
						priv);

				} catch (Exception exception) {

					log.warn (
						stringFormat (
							"Error getting parent for priv %s: ",
							priv.getId (),
							"type %s, ",
							priv.getParentType ().getCode (),
							"id %s",
							priv.getParentId ()),
						exception);

				}

			}

			// build objectDatasByObjectId

			for (PrivRec priv : privs) {

				GlobalId objectId =
					new GlobalId (
						priv.getParentType ().getId (),
						priv.getParentId ());

				ObjectData objectData =
					newData.objectDatasByObjectId.get (objectId);

				if (objectData == null) {

					objectData =
						new ObjectData ();

					newData.objectDatasByObjectId.put (
						objectId,
						objectData);

				}

				objectData.privIdsByCode.put (
					priv.getCode (),
					priv.getId ());

			}

			// sort out objectTypeIdsByClassName

			Collection<ObjectTypeRec> objectTypes =
				objectTypeHelper.findAll ();

			for (
				ObjectTypeRec objectType
					: objectTypes
			) {

				try {

					newData.objectTypeIdsByClassName.put (
						objectManager.objectClassForTypeCodeRequired (
							objectType.getCode ()
						).getName (),
						objectType.getId ());

				} catch (IllegalArgumentException exception) {

					log.warn (
						stringFormat (
							"Ignoring unknown object type %s",
							objectType.getCode ()));

				}

			}

			// do chainedPrivIds and managePrivIds

			for (
				PrivRec priv
					: privs
			) {

				Record<?> parent =
					objectManager.getParent (
						priv);

				try {

					// do chainedPrivIds

					Record<?> grandParent =
						objectManager.getParent (
							parent);

					if (grandParent != null) {

						Long chainedPrivId =
							getChainedPrivId (
								newData,
								grandParent,
								priv.getCode ());

						if (chainedPrivId != null)
							newData.chainedPrivIds.put (
								priv.getId (),
								chainedPrivId);

					}

					// do managePrivIds and object data managePrivId

					GlobalId objectId =
						objectManager.getGlobalId (
							parent);

					ObjectData objectData =
						newData.objectDatasByObjectId.get (objectId);

					if (
						stringEqualSafe (
							priv.getCode (),
							"manage")
					) {

						newData.managePrivIds.put (
							priv.getId (),
							priv.getId ());

						objectData.managePrivId =
							priv.getId ();

					} else {

						Long managePrivId =
							getChainedPrivId (
								newData,
								parent,
								"manage");

						if (managePrivId != null) {

							newData.managePrivIds.put (
								priv.getId (),
								managePrivId);

							objectData.managePrivId =
								managePrivId;

						}

					}

				} catch (Exception exception) {

					throw new RuntimeException (
						stringFormat (
							"Error loading priv %s",
							priv.getId ()),
						exception);

				}

			}

			newData.objectTypeCodesById =
				Collections.unmodifiableMap (
					objectTypeHelper.findAll ().stream ()

				.collect (Collectors.toMap (
					ObjectTypeRec::getId,
					ObjectTypeRec::getCode))

			);

			// end timer

			Instant endTime =
				Instant.now ();

			log.debug (
				stringFormat (
					"Reload complete (%sms)",
					endTime.getMillis () - startTime.getMillis ()));

			return newData;

		}

		/**
		 * Searches upwards through the object heirachy for a priv of the given
		 * name. Uses newData.privIdsByCodeByObjectId so only searches objects
		 * included there. That is sufficient for our purposes here.
		 */
		private
		Long getChainedPrivId (
				SharedData newData,
				Record<?> object,
				String code) {

			while (object != null) {

				GlobalId objectId =
					objectManager.getGlobalId (object);

				ObjectData objectData =
					newData.objectDatasByObjectId.get (objectId);

				if (objectData != null) {

					Long chainedPrivId =
						objectData.privIdsByCode.get (
							code);

					if (chainedPrivId != null)
						return chainedPrivId;

				}

				object =
					objectManager.getParent (
						object);

			}

			return null;

		}

	}

	// ================================= user data reloader

	private
	class UserDataReloader
		implements Provider<UserData> {

		private final
		Long userId;

		private
		UserDataReloader (
				Long newUserId) {

			userId =
				newUserId;

		}

		@Override
		public
		UserData get () {

			// create the data

			UserData newData =
				new UserData ();

			@Cleanup
			Transaction transaction =
				database.beginReadOnly (
					"PrivDataLoaderImplementation.UserDataReloader.get ()",
					this);

			// start timer

			log.debug (
				stringFormat (
					"User %s priv reload started",
					userId));

			Instant startTime =
				Instant.now ();

			// get user

			UserRec user =
				userHelper.findRequired (
					userId);

			// do user-specific privs

			for (
				UserPrivRec userPriv
					: user.getUserPrivs ()
			) {

				PrivRec priv =
					userPriv.getPriv ();

				PrivPair privPair =
					new PrivPair ();

				privPair.can =
					userPriv.getCan ();

				newData.privPairsByPrivId.put (
					priv.getId (),
					privPair);

			}

			// do group privs

			for (GroupRec group
					: user.getGroups ()) {

				for (PrivRec priv
						: group.getPrivs ()) {

					PrivPair privPair =
						newData.privPairsByPrivId.get (
							priv.getId ());

					if (privPair == null) {

						privPair =
							new PrivPair ();

						newData.privPairsByPrivId.put (
							priv.getId (),
							privPair);

					}

					privPair.can = true;

				}

			}

			// end timer

			Instant endTime =
				Instant.now ();

			log.debug (
				stringFormat (
					"User %s priv reload complere (%sms)",
					userId,
					endTime.getMillis () - startTime.getMillis ()));

			return newData;

		}

	}

}
