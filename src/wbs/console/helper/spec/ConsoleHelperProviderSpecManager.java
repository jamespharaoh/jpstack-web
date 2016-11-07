package wbs.console.helper.spec;

import static wbs.utils.etc.TypeUtils.dynamicCast;
import static wbs.utils.etc.TypeUtils.isInstanceOf;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleSpec;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

@Accessors (fluent = true)
@SingletonComponent ("consoleHelperProviderSpecManager")
public
class ConsoleHelperProviderSpecManager {

	// collection components

	@SingletonDependency
	List <ConsoleModuleSpec> consoleModuleSpecs;

	// state

	@Getter
	Map <String, ConsoleHelperProviderSpec> specsByName;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup () {

		specsByName =
			ImmutableMap.copyOf (
				consoleModuleSpecs.stream ()

			.flatMap (
				consoleModuleSpec ->
					consoleModuleSpec.builders ().stream ())

			.filter (
				builder ->
					isInstanceOf (
						ConsoleHelperProviderSpec.class,
						builder))

			.map (
				builder ->
					dynamicCast (
						ConsoleHelperProviderSpec.class,
						builder))

			.collect (
				Collectors.toMap (
					ConsoleHelperProviderSpec::objectName,
					Function.identity ()))

		);

	}

}
