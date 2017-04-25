package wbs.console.priv;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import java.util.Collection;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.misc.ConsoleUserHelper;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.ProxiedRequestComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
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
	Provider <UserPrivCheckerBuilder> privCheckerBuilderProvider;

	// state

	UserPrivChecker target;

	// lifecycle

	@NormalLifecycleSetup
	public
	void init (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"init");

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
				privCheckerBuilderProvider.get ()

				.userId (
					userId)

				.build (
					taskLogger);

		} else {

			target =
				new NullUserPrivChecker ();

		}

	}

	// implementation

	@Override
	public
	Long userIdRequired () {

		// TODO Auto-generated method stub
		return null;
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
	void refresh (
			@NonNull TaskLogger parentTaskLogger) {

		privDataLoader.refresh (
			parentTaskLogger);

		init (
			parentTaskLogger);

	}

}
