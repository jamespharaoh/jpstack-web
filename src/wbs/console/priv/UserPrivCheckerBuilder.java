package wbs.console.priv;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;

@Accessors (fluent = true)
@PrototypeComponent ("privCheckerBuilder")
public
class UserPrivCheckerBuilder {

	// dependencies

	@Inject
	UserPrivDataLoader privDataLoader;

	// properties

	@Getter @Setter
	Long userId;

	// builder

	public
	UserPrivChecker build () {

		return new Implementation ()

			.userPrivData (
				privDataLoader.getUserPrivData (
					userId));

	}

	// implementation

	@Accessors (fluent = true)
	static
	class Implementation
		implements UserPrivChecker {

		@Getter @Setter
		UserPrivData userPrivData;

		@Override
		public
		boolean canRecursive (
				@NonNull Long privId) {

			return userPrivData.canNormal (
				privId);

		}

		@Override
		public
		boolean canRecursive (
				GlobalId parentGlobalId,
				String... privCodes) {

			return userPrivData.canList (
				parentGlobalId,
				Arrays.asList (privCodes),
				true);

		}

		@Override
		public
		boolean canRecursive (
				@NonNull Class<? extends Record<?>> parentClass,
				@NonNull Long parentId,
				@NonNull String... privCodes) {

			Long parentTypeId =
				userPrivData.coreGetObjectTypeId (
					parentClass);

			return userPrivData.canList (
				new GlobalId (
					parentTypeId,
					parentId),
				Arrays.asList (
					privCodes),
				true);

		}

		@Override
		public
		boolean canRecursive (
				@NonNull Record<?> parentObject,
				@NonNull String... privCodes) {

			Long parentObjectTypeId =
				userPrivData.coreGetObjectTypeId (
					parentObject.getClass ());

			return userPrivData.canList (
				new GlobalId (
					parentObjectTypeId,
					parentObject.getId ()),
				Arrays.asList (
					privCodes),
				true);

		}

		@Override
		public
		boolean canSimple (
				GlobalId parentGlobalId,
				String... privCodes) {

			return userPrivData.canList (
				parentGlobalId,
				Arrays.asList (privCodes),
				false);

		}

		@Override
		public
		boolean canSimple (
				@NonNull Record<?> parentObject,
				@NonNull String... privCodes) {

			Long parentObjectTypeId =
				userPrivData.coreGetObjectTypeId (
					parentObject.getClass ());

			return userPrivData.canList (
				new GlobalId (
					parentObjectTypeId,
					parentObject.getId ()),
				Arrays.asList (
					privCodes),
				false);

		}

		@Override
		public
		boolean canRecursive (
				Map<Object,Collection<String>> map) {

			for (Map.Entry<Object,Collection<String>> ent
					: map.entrySet ()) {

				Object key =
					ent.getKey ();

				Collection<String> privCodes =
					ent.getValue ();

				GlobalId parentObjectId;

				if (key instanceof GlobalId) {

					parentObjectId =
						(GlobalId) key;

				} else if (key instanceof Record) {

					Record<?> parentObject =
						(Record<?>) key;

					Long parentObjectTypeId =
						userPrivData.coreGetObjectTypeId (
							parentObject.getClass ());

					parentObjectId =
						new GlobalId (
							parentObjectTypeId,
							parentObject.getId ());

				} else {

					throw new IllegalArgumentException ();

				}

				if (

					isNotNull (
						privCodes)

					&& userPrivData.canList (
						parentObjectId,
						privCodes,
						true)

				) {
					return true;
				}

				if (

					isNull (
						privCodes)

					&& userPrivData.canList (
						parentObjectId,
						Collections.<String>emptyList (),
						true)

				) {
					return true;
				}

			}

			return false;

		}

		@Override
		public
		boolean canGrant (
				@NonNull Long privId) {

			Long managePrivId =
				userPrivData.sharedData.managePrivIds.get (
					privId);

			return (

				isNotNull (
					managePrivId)

				&& userPrivData.canChain (
					managePrivId)

			);

		}

		@Override
		public
		void refresh () {

			throw new UnsupportedOperationException ();

		}

	}

}
