package wbs.console.priv;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerNotEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("userPrivData")
public
class UserPrivData {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	public
	SharedData sharedData;

	public
	UserData userData;

	public
	boolean canSingle (
			@NonNull Long privId) {

		PrivPair privPair =
			userData.privPairsByPrivId.get (
				privId);

		return privPair != null
			&& privPair.can;

	}

	public
	boolean canChain (
			@NonNull Long originalPrivId) {

		Long currentPrivId =
			originalPrivId;

		for (;;) {

			if (
				canSingle (
					currentPrivId)
			) {
				return true;
			}

			Long nextPrivId =
				sharedData.chainedPrivIds.get (
					currentPrivId);

			if (nextPrivId == null)
				return false;

			currentPrivId =
				nextPrivId;

		}

	}

	public
	boolean canNormal (
			@NonNull Long privId) {

		// try this priv and chain

		if (
			canChain (
				privId)
		) {
			return true;
		}

		// look for a manage priv and try that and its chain

		Long managePrivId =
			sharedData.managePrivIds.get (
				privId);

		return (

			isNotNull (
				managePrivId)

			&& integerNotEqualSafe (
				managePrivId,
				privId)

			&& canChain (
				managePrivId)

		);

	}

	public
	Long coreGetObjectTypeId (
			@NonNull Class <?> objectClassParam) {

		Class <?> objectClass =
			objectClassParam;

		while (
			Record.class.isAssignableFrom (
				objectClass)
		) {

			Long id =
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
				classNameSimple (
					objectClassParam)));

	}

	public
	boolean canList (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull GlobalId parentObjectId,
			@NonNull Collection <String> privCodes,
			@NonNull Boolean recurse) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"canList");

		) {

			ObjectData objectData =
				sharedData.objectDatasByObjectId.get (
					parentObjectId);

			if (
				isNull (
					objectData)
			) {

				taskLogger.warningFormat (
					"No priv data for %s",
					parentObjectId.toString ());

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
						stringEqualSafe (
							privCode,
							"manage")
					) {
						continue;
					}

					Long privId =
						objectData.privIdsByCode.get (
							privCode);

					if (
						isNull (
							privId)
					) {

						String objectTypeCode =
							sharedData.objectTypeCodesById.get (
								parentObjectId.typeId ());

						if (
							isNull (
								objectTypeCode)
						) {

							throw new IllegalArgumentException (
								stringFormat (
									"Unknown object type %s",
									integerToDecimalString (
										parentObjectId.typeId ())));

						}

						taskLogger.warningFormat (
							"Unknown priv %s on object type %s (%s)",
							privCode,
							objectTypeCode,
							integerToDecimalString (
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
					Long privId
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

	}

	public static
	class SharedData {

		public
		Map <GlobalId, ObjectData> objectDatasByObjectId =
			new HashMap<> ();

		public
		Map <String, Long> objectTypeIdsByClassName =
			new HashMap<> ();

		public
		Map <Long, Long> chainedPrivIds =
			new HashMap<> ();

		public
		Map <Long, Long> managePrivIds =
			new HashMap<> ();

		public
		Map <Long, String> objectTypeCodesById;

	}

	public static
	class UserData {

		public
		Map <Long, PrivPair> privPairsByPrivId =
			new HashMap <> ();

	}

	public static
	class PrivPair {

		public
		boolean can;

	}

	public static
	class ObjectData {

		public
		Map <String, Long> privIdsByCode =
			new HashMap <String, Long> ();

		public
		Long managePrivId;

	}

}
