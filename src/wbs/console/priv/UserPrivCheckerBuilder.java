package wbs.console.priv;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("privCheckerBuilder")
public
class UserPrivCheckerBuilder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivDataLoader privDataLoader;

	// properties

	@Getter @Setter
	Long userId;

	// builder

	public
	UserPrivChecker build (
			@NonNull TaskLogger parentTaskLogger) {

		return new Implementation ()

			.userId (
				userId)

			.userPrivData (
				privDataLoader.getUserPrivData (
					parentTaskLogger,
					userId));

	}

	// implementation

	@Accessors (fluent = true)
	static
	class Implementation
		implements UserPrivChecker {

		@Getter @Setter
		Long userId;

		@Getter @Setter
		UserPrivData userPrivData;

		@Override
		public
		Long userIdRequired () {
			return userId;
		}

		@Override
		public
		boolean canRecursive (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Long privId) {

			return userPrivData.canNormal (
				privId);

		}

		@Override
		public
		boolean canRecursive (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull GlobalId parentGlobalId,
				@NonNull String ... privCodes) {

			return userPrivData.canList (
				parentTaskLogger,
				parentGlobalId,
				Arrays.asList (privCodes),
				true);

		}

		@Override
		public
		boolean canRecursive (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Class<? extends Record<?>> parentClass,
				@NonNull Long parentId,
				@NonNull String... privCodes) {

			Long parentTypeId =
				userPrivData.coreGetObjectTypeId (
					parentClass);

			return userPrivData.canList (
				parentTaskLogger,
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
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Record <?> parentObject,
				@NonNull String ... privCodes) {

			Long parentObjectTypeId =
				userPrivData.coreGetObjectTypeId (
					parentObject.getClass ());

			return userPrivData.canList (
				parentTaskLogger,
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
				@NonNull TaskLogger parentTaskLogger,
				@NonNull GlobalId parentGlobalId,
				@NonNull String ... privCodes) {

			return userPrivData.canList (
				parentTaskLogger,
				parentGlobalId,
				Arrays.asList (
					privCodes),
				false);

		}

		@Override
		public
		boolean canSimple (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Record<?> parentObject,
				@NonNull String... privCodes) {

			Long parentObjectTypeId =
				userPrivData.coreGetObjectTypeId (
					parentObject.getClass ());

			return userPrivData.canList (
				parentTaskLogger,
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
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Map <Object, Collection <String>> map) {

			for (
				Map.Entry <Object, Collection <String>> ent
					: map.entrySet ()
			) {

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
						parentTaskLogger,
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
						parentTaskLogger,
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
				@NonNull TaskLogger parentTaskLogger,
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
		void refresh (
				@NonNull TaskLogger parentTaskLogger) {

			throw new UnsupportedOperationException ();

		}

	}

}
