package wbs.console.priv;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.misc.ConsoleUserHelper;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.ProxiedRequestComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@ProxiedRequestComponent (
	value = "userPrivChecker",
	proxyInterface = UserPrivChecker.class)
public
class UserPrivCheckerImplementation
	implements UserPrivChecker {

	// singleton dependencies

	@SingletonDependency
	ConsoleUserHelper consoleUserHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivDataLoader privDataLoader;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <UserPrivCheckerBuilder> privCheckerBuilderProvider;

	// state

	UserPrivChecker target;

	// lifecycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			Optional <Long> userIdOptional =
				consoleUserHelper.loggedInUserId ();

			if (
				optionalIsPresent (
					userIdOptional)
			) {

				Long userId =
					optionalGetRequired (
						userIdOptional);

				target =
					privCheckerBuilderProvider.provide (
						taskLogger)

					.userId (
						userId)

					.build (
						taskLogger);

			} else {

				target =
					new NullUserPrivChecker ();

			}

		}

	}

	// implementation

	@Override
	public
	Long userIdRequired () {
		return target.userIdRequired ();
	}

	@Override
	public
	boolean canRecursive (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long privId) {

		return target.canRecursive (
			parentTaskLogger,
			privId);

	}

	@Override
	public
	boolean canRecursive (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull GlobalId parentGlobalId,
			@NonNull String ... privCodes) {

		return target.canRecursive (
			parentTaskLogger,
			parentGlobalId,
			privCodes);

	}

	@Override
	public
	boolean canRecursive (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <? extends Record <?>> parentClass,
			@NonNull Long parentId,
			@NonNull String ... privCodes) {

		return target.canRecursive (
			parentTaskLogger,
			parentClass,
			parentId,
			privCodes);

	}

	@Override
	public
	boolean canRecursive (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> parentObject,
			@NonNull String ... privCodes) {

		return target.canRecursive (
			parentTaskLogger,
			parentObject,
			privCodes);

	}

	@Override
	public
	boolean canSimple (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull GlobalId parentGlobalId,
			@NonNull String ... privCodes) {

		return target.canSimple (
			parentTaskLogger,
			parentGlobalId,
			privCodes);

	}

	@Override
	public
	boolean canSimple (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> parentObject,
			@NonNull String... privCodes) {

		return target.canSimple (
			parentTaskLogger,
			parentObject,
			privCodes);

	}

	@Override
	public
	boolean canRecursive (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Map <Object, Collection <String>> map) {

		return target.canRecursive (
			parentTaskLogger,
			map);

	}

	@Override
	public
	boolean canGrant (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long privId) {

		return target.canGrant (
			parentTaskLogger,
			privId);

	}

	@Override
	public
	Set <Long> getObjectIds (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long parentTypeId) {

		return target.getObjectIds (
			parentTaskLogger,
			parentTypeId);

	}

	@Override
	public
	Set <Long> getCanRecursiveObjectIds (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long parentTypeId,
			@NonNull String ... privCodes) {

		return target.getCanRecursiveObjectIds (
			parentTaskLogger,
			parentTypeId,
			privCodes);

	}

	@Override
	public
	void refresh (
			@NonNull TaskLogger parentTaskLogger) {

		privDataLoader.refresh (
			parentTaskLogger);

		setup (
			parentTaskLogger);

	}

}
