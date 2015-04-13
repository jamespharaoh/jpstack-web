package wbs.platform.priv.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.platform.priv.console.PrivDataLoaderImpl.UnknownObjectException;

public
class UserPrivData {

	SharedData sharedData;
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
			GlobalId parentObjectId,
			Collection<String> privCodes)
		throws UnknownObjectException {

		ObjectData objectData =
			sharedData.objectDatasByObjectId
				.get (parentObjectId);

		if (objectData == null) {

			throw new UnknownObjectException (
				parentObjectId.toString ());

		}

		// check manage priv

		if (objectData.managePrivId != null
				&& canChain (objectData.managePrivId))
			return true;

		if (! privCodes.isEmpty ()) {

			// check each named priv and any chained

			for (String privCode
					: privCodes) {

				if (equal (privCode, "manage"))
					continue;

				Integer privId =
					objectData.privIdsByCode.get (privCode);

				if (privId == null) {

					String objectTypeCode =
						sharedData.objectTypeCodesById.get (
							parentObjectId.typeId ());

					if (objectTypeCode == null) {

						throw new IllegalArgumentException (
							stringFormat (
								"Unknown object type %s",
								parentObjectId.typeId ()));

					}

					throw new IllegalArgumentException (
						stringFormat (
							"Unknown priv %s (%s) on object type %s (%s)",
							privCode,
							privId,
							objectTypeCode,
							parentObjectId.objectId ()));

				}

				if (canChain (privId))
					return true;

			}

		} else {

			// check all this object's privs

			for (Integer privId
					: objectData.privIdsByCode.values ()) {

				if (canSingle (privId))
					return true;

			}

		}

		return false;

	}

	static
	class SharedData {

		Map<GlobalId,ObjectData> objectDatasByObjectId =
			new HashMap<GlobalId,ObjectData> ();

		Map<String,Integer> objectTypeIdsByClassName =
			new HashMap<String,Integer> ();

		Map<Integer,Integer> chainedPrivIds =
			new HashMap<Integer,Integer> ();

		Map<Integer,Integer> managePrivIds =
			new HashMap<Integer,Integer> ();

		Map<Integer,String> objectTypeCodesById =
			new HashMap<Integer,String> ();

	}

	public static
	class UserData {

		Map<Integer,PrivPair> privPairsByPrivId =
			new HashMap<Integer,PrivPair> ();

	}

	public static
	class PrivPair {
		boolean can;
	}

	public static
	class ObjectData {

		Map<String,Integer> privIdsByCode =
			new HashMap<String,Integer> ();

		Integer managePrivId;

	}

}
