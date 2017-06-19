package wbs.framework.builder;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveTwoElements;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listSecondElementRequired;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.etc.Misc.fullClassName;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.etc.TypeUtils.classNotEqual;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.checkerframework.checker.nullness.qual.Nullable;

import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("builderFactory")
@Accessors (fluent = true)
public
class BuilderFactoryImplementation <Context>
	implements BuilderFactory <
		BuilderFactoryImplementation <Context>,
		Context
	> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <BuilderImplementation <Context>> builderImplementationProvider;

	// properties

	@Getter @Setter
	Class <Context> contextClass;

	// state

	List <BuilderInfo> builderInfos =
		new ArrayList<> ();

	// implementation

	@Override
	public
	BuilderFactoryImplementation <Context> addBuilder (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <?> builderClass,
			@NonNull Provider <?> builderProvider) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"addBuilder");

		) {

			checkContextClassIsNotNull ();

			BuilderInfo builderInfo =
				new BuilderInfo ();

			builderInfo.builderClass =
				builderClass;

			builderInfo.builderProvider =
				builderProvider;

			for (
				Field field
					: builderClass.getDeclaredFields ()
			) {

				BuilderParent builderParentAnnotation =
					field.getAnnotation (
						BuilderParent.class);

				if (builderParentAnnotation != null) {

					if (builderInfo.parentField != null)
						throw new RuntimeException ();

					builderInfo.parentField =
						field;

				}

				BuilderSource builderSourceAnnotation =
					field.getAnnotation (
						BuilderSource.class);

				if (builderSourceAnnotation != null) {

					if (builderInfo.sourceField != null)
						throw new RuntimeException ();

					builderInfo.sourceField =
						field;

				}

				BuilderTarget builderTargetAnnotation =
					field.getAnnotation (
						BuilderTarget.class);

				if (builderTargetAnnotation != null) {

					if (builderInfo.targetField != null)
						throw new RuntimeException ();

					builderInfo.targetField =
						field;

				}

			}

			for (
				Method method
					: builderClass.getDeclaredMethods ()
			) {

				@Nullable
				BuildMethod buildMethodAnnotation =
					method.getAnnotation (
						BuildMethod.class);

				if (buildMethodAnnotation == null)
					continue;

				if (builderInfo.buildMethod != null)
					throw new RuntimeException ();

				List <Class <?>> buildMethodParameters =
					ImmutableList.copyOf (
						method.getParameterTypes ());

				if (

					collectionDoesNotHaveTwoElements (
						buildMethodParameters)

					|| classNotEqual (
						listFirstElementRequired (
							buildMethodParameters),
						contextClass)

					|| classNotEqual (
						listSecondElementRequired (
							buildMethodParameters),
							Builder.class)

				) {

					taskLogger.errorFormat (
						"Build method %s.%s ",
						fullClassName (
							builderClass),
						method.getName (),
						"has invalid type signature (%s)",
						joinWithCommaAndSpace (
							iterableMap (
								buildMethodParameters,
								parameterType ->
									classNameSimple (
										parameterType))));

					return this;

				}

				builderInfo.buildMethod =
					method;

			}

			if (builderInfo.parentField == null) {

				taskLogger.errorFormat (
					"No @BuilderParent field for %s",
					classNameFull (
						builderClass));

				return this;

			}

			if (builderInfo.sourceField == null)
				throw new RuntimeException ();

			if (builderInfo.targetField == null)
				throw new RuntimeException ();

			if (builderInfo.buildMethod == null)
				throw new RuntimeException ();

			if (
				builderInfo.buildMethod.getParameterTypes ().length < 1
				|| builderInfo.buildMethod.getParameterTypes ().length > 2
			) {

				taskLogger.errorFormat (
					"Build method %s.%s should have one or two arguments",
					builderClass.getSimpleName (),
					builderInfo.buildMethod.getName ());

				return this;

			}

			builderInfo.parentField.setAccessible (true);
			builderInfo.sourceField.setAccessible (true);
			builderInfo.targetField.setAccessible (true);

			builderInfos.add (
				builderInfo);

			return this;

		}

	}

	@Override
	public <Type>
	BuilderFactoryImplementation <Context> addBuilders (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Map <Class <?>, ComponentProvider <Type>> buildersMap) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"addBuilders");

		) {

			checkContextClassIsNotNull ();

			for (
				Map.Entry <Class <?>, ComponentProvider <Type>> builderEntry
					: buildersMap.entrySet ()
			) {

				addBuilder (
					taskLogger,
					builderEntry.getKey (),
					builderEntry.getValue ());

			}

			return this;

		}

	}

	@Override
	public
	Builder <Context> create (
			@NonNull TaskLogger parentTaskLogger) {

		checkContextClassIsNotNull ();

		parentTaskLogger.makeException ();

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"create");

		) {

			return builderImplementationProvider.get ()

				.contextClass (
					contextClass)

				.builderInfos (
					builderInfos)

			;

		}

	}

	// private implementation

	private
	void checkContextClassIsNotNull () {

		if (
			isNull (
				contextClass)
		) {

			throw new NullPointerException (
				stringFormat (
					"BuilderFactoryImplementation.contextClass must be ",
					"specified."));

		}

	}

}
