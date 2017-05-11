package wbs.framework.component.tools;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveOneElement;
import static wbs.utils.collection.CollectionUtils.collectionHasMoreThanOneElement;
import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.NullUtils.filterNotNullToList;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.classEqualSafe;
import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.isInstanceOf;
import static wbs.utils.etc.TypeUtils.isNotInstanceOf;
import static wbs.utils.etc.TypeUtils.isSubclassOf;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.objectToString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.HiddenComponent;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.ProxiedRequestComponent;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("annotatedClassComponentTools")
public
class AnnotatedClassComponentTools {

	// state

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	public
	Optional <ComponentDefinition> definitionForClass (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <?> componentClass) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"definitionForClass");

		) {

			List <ComponentDefinition> definitions =
				definitionsForClass (
					taskLogger,
					componentClass);

			if (
				collectionIsEmpty (
					definitions)
			) {
				return optionalAbsent ();
			}

			if (
				collectionHasMoreThanOneElement (
					definitions)
			) {
				throw new RuntimeException ();
			}

			return optionalOf (
				listFirstElementRequired (
					definitions));

		}

	}

	public
	List <ComponentDefinition> definitionsForClass (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <?> providedClass) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"definitionsForClass");

		) {

			// check for annotations

			SingletonComponent singletonComponent =
				providedClass.getAnnotation (
					SingletonComponent.class);

			PrototypeComponent prototypeComponent =
				providedClass.getAnnotation (
					PrototypeComponent.class);

			ProxiedRequestComponent proxiedRequestComponent =
				providedClass.getAnnotation (
					ProxiedRequestComponent.class);

			List <Annotation> presentAnnotations =
				filterNotNullToList (
					singletonComponent,
					prototypeComponent,
					proxiedRequestComponent);

			if (
				collectionIsEmpty (
					presentAnnotations)
			) {

				taskLogger.errorFormat (
					"Could not find component annotation on %s",
					providedClass.getName ());

				return emptyList ();

			}

			if (
				collectionHasMoreThanOneElement (
					presentAnnotations)
			) {

				taskLogger.errorFormat (
					"Component class %s ",
					classNameFull (
						providedClass),
					"has multiple component annotations: %s",
					joinWithCommaAndSpace (
						iterableMap (
							annotation ->
								classNameSimple (
									annotation.getClass ()),
							presentAnnotations)));

				return emptyList ();

			}

			// check for hidden component annotation

			HiddenComponent hiddenComponentAnnotation =
				providedClass.getAnnotation (
					HiddenComponent.class);

			boolean hidden =
				isNotNull (
					hiddenComponentAnnotation);

			// handle component factories

			Class <? extends ComponentFactory <?>> factoryClass;
			Class <?> componentClass;

			if (
				isSubclassOf (
					ComponentFactory.class,
					providedClass)
			) {

				componentClass =
					getFactoryComponentClass (
						taskLogger,
						genericCastUnchecked (
							providedClass));

				factoryClass =
					genericCastUnchecked (
						providedClass);

			} else {

				componentClass =
					providedClass;

				factoryClass =
					null;

			}

			// handle singleton componentannotation

			if (
				isNotNull (
					singletonComponent)
			) {

				String componentName =
					singletonComponent.value ();

				return ImmutableList.of (

					new ComponentDefinition ()

						.name (
							componentName)

						.factoryClass (
							factoryClass)

						.componentClass (
							componentClass)

						.scope (
							"singleton")

						.fromAnnotatedClass (
							true)

						.hide (
							hidden)

				);

			}

			// handle prototype component

			if (
				isNotNull (
					prototypeComponent)
			) {

				String componentName =
					prototypeComponent.value ();

				return ImmutableList.of (

					new ComponentDefinition ()

						.name (
							componentName)

						.factoryClass (
							factoryClass)

						.componentClass (
							componentClass)

						.scope (
							"prototype")

						.fromAnnotatedClass (
							true)

						.hide (
							hidden)

				);

			}

			// handle proxied request component

			if (
				isNotNull (
					proxiedRequestComponent)
			) {

				String componentName =
					proxiedRequestComponent.value ();

				String targetComponentName =
					stringFormat (
						"%sTarget",
						componentName);

				return ImmutableList.of (

					new ComponentDefinition ()

						.name (
							targetComponentName)

						.factoryClass (
							factoryClass)

						.componentClass (
							componentClass)

						.scope (
							"prototype")

						.hide (
							true)

						.fromAnnotatedClass (
							true)

					,

					new ComponentDefinition ()

						.name (
							componentName)

						.componentClass (
							proxiedRequestComponent.proxyInterface ())

						.factoryClass (
							genericCastUnchecked (
								ThreadLocalProxyComponentFactory.class))

						.scope (
							"singleton")

						.addValueProperty (
							"componentName",
							componentName)

						.addValueProperty (
							"componentClass",
							proxiedRequestComponent.proxyInterface ())

						.hide (
							hidden)

				);

			}

			throw shouldNeverHappen ();

		}

	}

	private
	Class <?> getFactoryComponentClass (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <? extends ComponentFactory <?>> factoryClass) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleComponentFactory");

		) {

			List <ParameterizedType> factoryInterfaces =
				Arrays.stream (
					factoryClass.getGenericInterfaces ())

				.filter (
					genericInterface ->
						isInstanceOf (
							ParameterizedType.class,
							genericInterface))

				.map (
					genericInterface ->
						(ParameterizedType)
						genericInterface)

				.filter (
					genericInterface ->
						classEqualSafe (
							ComponentFactory.class,
							(Class <?>)
							genericInterface.getRawType ()))

				.collect (
					Collectors.toList ());

			if (
				collectionDoesNotHaveOneElement (
					factoryInterfaces)
			) {

				throw new ClassCastException (
					objectToString (
						factoryClass));

			}

			ParameterizedType factoryInterface =
				listFirstElementRequired (
					factoryInterfaces);

			List <Type> factoryTypeArguments =
				Arrays.asList (
					factoryInterface.getActualTypeArguments ());

			if (
				collectionDoesNotHaveOneElement (
					factoryTypeArguments)
			) {

				throw new ClassCastException (
					objectToString (
						factoryInterface));

			}

			Type factoryTypeArgument =
				listFirstElementRequired (
					factoryTypeArguments);

			if (
				isInstanceOf (
					Class.class,
					factoryTypeArgument)
			) {

				return genericCastUnchecked (
					factoryTypeArgument);

			} else if (
				isInstanceOf (
					ParameterizedType.class,
					factoryTypeArgument)
			) {

				ParameterizedType factoryParameterizedArgument =
					genericCastUnchecked (
						factoryTypeArgument);

				if (
					isNotInstanceOf (
						Class.class,
						factoryParameterizedArgument.getRawType ())
				) {

					throw new ClassCastException (
						objectToString (
							factoryParameterizedArgument.getRawType ()));

				}

				return genericCastUnchecked (
					factoryParameterizedArgument.getRawType ());

			} else {

				throw new ClassCastException (
					objectToString (
						factoryTypeArgument));

			}

		}

	}


}
