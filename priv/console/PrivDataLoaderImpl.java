package wbs.platform.priv.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.platform.group.model.GroupRec;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.priv.console.UserPrivData.ObjectData;
import wbs.platform.priv.console.UserPrivData.PrivPair;
import wbs.platform.priv.console.UserPrivData.SharedData;
import wbs.platform.priv.console.UserPrivData.UserData;
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
class PrivDataLoaderImpl
	implements PrivDataLoader {

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

	@Getter @Setter
	int reloadTimeSeconds = 60 * 15;

	UpdateGetter<SharedData> privDataCache;

	Map<Integer,UpdateGetter<UserData>> userDataCachesByUserId =
		new HashMap<Integer,UpdateGetter<UserData>>();

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

	SharedData getPrivData () {

		return privDataCache
			.get ();

	}

	private UserData getUserData (
			int userId) {

		Provider<UserData> userDataCache =
			getUserDataCache (userId);

		return userDataCache.get ();

	}

	@Override
	public
	UserPrivData getUserPrivData (
			int userId) {

		UserPrivData ret =
			new UserPrivData ();

		ret.sharedData =
			getPrivData ();

		ret.userData =
			getUserData (userId);

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
			int userId) {

		UpdateGetter<UserData> ret =
			userDataCachesByUserId.get (userId);

		if (ret == null) {

			ret =
				updateManager.makeUpdateGetterAdaptor (
					new UserDataReloader (userId),
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
					this);

			// start timer

			log.debug (
				"Priv reload started");

			long startTime =
				System.currentTimeMillis ();

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
							priv.getParentObjectType ().getCode (),
							"id %s",
							priv.getParentObjectId ()),
						exception);

				}

			}

			// build objectDatasByObjectId

			for (PrivRec priv : privs) {

				GlobalId objectId =
					new GlobalId (
						priv.getParentObjectType ().getId (),
						priv.getParentObjectId ());

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
						objectManager.objectTypeCodeToClass (
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

						Integer chainedPrivId =
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

					if (equal (
							priv.getCode (),
							"manage")) {

						newData.managePrivIds.put (
							priv.getId (),
							priv.getId ());

						objectData.managePrivId =
							priv.getId ();

					} else {

						Integer managePrivId =
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

			for (ObjectTypeRec objectType
					: objectTypeHelper.findAll ()) {

				newData.objectTypeCodesById.put (
					objectType.getId (),
					objectType.getCode ());

			}

			// end timer

			long endTime =
				new Date ().getTime ();

			log.debug (
				stringFormat (
					"Reload complete (%sms",
					endTime - startTime));

			return newData;
		}

		/**
		 * Searches upwards through the object heirachy for a priv of the given
		 * name. Uses newData.privIdsByCodeByObjectId so only searches objects
		 * included there. That is sufficient for our purposes here.
		 */
		private Integer getChainedPrivId (
				SharedData newData,
				Record<?> object,
				String code) {

			while (object != null) {

				GlobalId objectId =
					objectManager.getGlobalId (object);

				ObjectData objectData =
					newData.objectDatasByObjectId.get (objectId);

				if (objectData != null) {

					Integer chainedPrivId =
						objectData.privIdsByCode.get (code);

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
		int userId;

		private
		UserDataReloader (
				int newUserId) {

			userId = newUserId;

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
					this);

			// start timer

			log.debug (
				stringFormat (
					"User %s priv reload started",
					userId));

			long startTime =
				new Date ().getTime ();

			// get user

			UserRec user =
				userHelper.find (userId);

			// do user-specific privs

			for (UserPrivRec userPriv :
					user.getUserPrivs ()) {

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

			long endTime =
				new Date ().getTime ();

			log.debug (
				stringFormat (
					"User %s priv reload complere (%sms)",
					userId,
					endTime - startTime));

			return newData;

		}

	}

	public static
	class UnknownObjectException
		extends RuntimeException {

		public
		UnknownObjectException (
				String message) {

			super (
				message);

		}

	}

}
