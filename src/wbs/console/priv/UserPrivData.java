package wbs.console.priv;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;

@Log4j
public
class UserPrivData {

	public
	SharedData sharedData;

	public
	UserData userData;

	public
	boolean canSingle (
			int privId) {

		PrivPair privPair =
			userData.privPairsByPrivId
				.get (privId);

		return privPair != null
			&& privPair.can;

	}

	public
	boolean canChain (
			int privId) {

		while (true) {

			if (canSingle (privId))
				return true;

			Integer nextPrivId =
				sharedData.chainedPrivIds
					.get (privId);

			if (nextPrivId == null)
				return false;

			privId =
				nextPrivId;

		}

	}

	public
	boolean canNormal (
			int privId) {

		// try this priv and chain

		if (canChain (privId))
			return true;

		// look for a manage priv and try that and its chain

		Integer managePrivId =
			sharedData.managePrivIds
				.get (privId);

		return managePrivId != null
			&& ! equal (managePrivId, privId)
			&& canChain (managePrivId);

	}

	public
	int coreGetObjectTypeId (
			Class<?> objectClassParam) {

		Class<?> objectClass =
			objectClassParam;

		while (Record.class.isAssignableFrom (objectClass)) {

			Integer id =
				sharedData.objectTypeIdsByClassName.get (
					objectClass.getName ());

			if (id != null)
				return id;

			objectClass =
				objectClass.getSuperclass ();

		}

		throw new IllegalArgumentException (
			stringFormat (
				"Unknown data object class %s",
				objectClassParam));

	}

	public
	boolean canList (
			@NonNull GlobalId parentObjectId,
			@NonNull Collection<String> privCodes,
			@NonNull Boolean recurse) {

		ObjectData objectData =
			sharedData.objectDatasByObjectId.get (
				parentObjectId);

		if (
			isNull (
				objectData)
		) {

			log.warn (
				stringFormat (
					"No priv data for %s",
					parentObjectId.toString ()));

			return false;

		}

		// check manage priv

		if (

			recurse

			&& isNotNull (
				objectData.managePrivId)

			&& canChain (
				objectData.managePrivId)

		) {
			return true;
		}

		if (! privCodes.isEmpty ()) {

			// check each named priv and any chained

			for (
				String privCode
					: privCodes
			) {

				if (
					equal (
						privCode,
						"manage")
				) {
					continue;
				}

				Integer privId =
					objectData.privIdsByCode.get (
						privCode);

				if (
					isNull (
						privId)
				) {

					String objectTypeCode =
						sharedData.objectTypeCodesById.get (
							(int) (long)
							parentObjectId.typeId ());

					if (
						isNull (
							objectTypeCode)
					) {

						throw new IllegalArgumentException (
							stringFormat (
								"Unknown object type %s",
								parentObjectId.typeId ()));

					}

					log.warn (
						stringFormat (
							"Unknown priv %s on object type %s (%s)",
							privCode,
							objectTypeCode,
							parentObjectId.objectId ()));

					return false;

				}

				if (recurse) {

					if (
						canChain (
							privId)
					) {
						return true;
					}

				} else {

					if (
						canSingle (
							privId)
					) {
						return true;
					}

				}

			}

		} else {

			// check all this object's privs

			for (
				Integer privId
					: objectData.privIdsByCode.values ()
			) {

				if (
					canSingle (
						privId)
				) {
					return true;
				}

			}

		}

		return false;

	}

	public static
	class SharedData {

		public
		Map<GlobalId,ObjectData> objectDatasByObjectId =
			new HashMap<GlobalId,ObjectData> ();

		public
		Map<String,Integer> objectTypeIdsByClassName =
			new HashMap<String,Integer> ();

		public
		Map<Integer,Integer> chainedPrivIds =
			new HashMap<Integer,Integer> ();

		public
		Map<Integer,Integer> managePrivIds =
			new HashMap<Integer,Integer> ();

		public
		Map<Integer,String> objectTypeCodesById;

	}

	public static
	class UserData {

		public
		Map<Integer,PrivPair> privPairsByPrivId =
			new HashMap<Integer,PrivPair> ();

	}

	public static
	class PrivPair {

		public
		boolean can;

	}

	public static
	class ObjectData {

		public
		Map<String,Integer> privIdsByCode =
			new HashMap<String,Integer> ();

		public
		Integer managePrivId;

	}

}
