package wbs.framework.component.manager;

import static wbs.utils.collection.CollectionUtils.collectionHasMoreThanOneElement;
import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.IterableUtils.iterableFilterToList;
import static wbs.utils.collection.IterableUtils.iterableOnlyItemRequired;
import static wbs.utils.collection.MapUtils.iterableTransformToMap;
import static wbs.utils.collection.MapUtils.mapContainsKey;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.collection.MapUtils.mapItemForKeyOrElseSet;
import static wbs.utils.etc.DebugUtils.debugFormat;
import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.Misc.doesNotImplement;
import static wbs.utils.etc.Misc.fullClassName;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.Misc.todo;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromJava;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.etc.ReflectionUtils.fieldSet;
import static wbs.utils.etc.TypeUtils.classEqualSafe;
import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.etc.TypeUtils.classNotEqual;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.isInstanceOf;
import static wbs.utils.etc.TypeUtils.isNotSubclassOf;
import static wbs.utils.etc.TypeUtils.rawType;
import static wbs.utils.string.StringUtils.keyEqualsString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.inject.Provider;
import javax.inject.Qualifier;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.ComponentInterface;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.component.config.WbsConfigFactory;
import wbs.framework.component.registry.ComponentRegistryImplementation;
import wbs.framework.component.scaffold.BuildSpec;
import wbs.framework.component.scaffold.BuildSpecFactory;
import wbs.framework.component.scaffold.PluginBootstrapComponentSpec;
import wbs.framework.component.scaffold.PluginLayerSpec;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginManagerFactory;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.component.tools.AnnotatedClassComponentTools;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.component.tools.ComponentManagerBuilder;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.data.tools.DataFromXmlImplementation;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.LoggingLogic;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

public
class BootstrapComponentManager
	implements ComponentManager {

	// state

	LoggingLogic loggingLogic;

	LogContext logContext;

	Map <String, ComponentData> componentsByName =
		new TreeMap<> ();

	Map <Class <?>, List <ComponentData>> componentsByInterface =
		new HashMap<> ();

	BuildSpec buildSpec;

	// constructors

	public
	BootstrapComponentManager (
			@NonNull LoggingLogic loggingLogic) {

		this.loggingLogic =
			loggingLogic;

		logContext =
			loggingLogic.findOrCreateLogContext (
				classNameFull (
					getClass ()));

		try (

			OwnedTaskLogger taskLogger =
				logContext.createTaskLogger (
					"BootstrapComponentManager");

		) {

			registerSingleton (
				taskLogger,
				"loggingLogic",
				LoggingLogic.class,
				loggingLogic);

			registerSingleton (
				taskLogger,
				"componentManager",
				ComponentManager.class,
				this);

		}

	}

	// public implemetation

	public synchronized
	OwnedTaskLogger bootstrapTaskLogger (
			@NonNull Object object) {

		return logContext.createTaskLogger (
			fullClassName (
				object.getClass ()));

	}

	public synchronized <Type>
	BootstrapComponentManager registerSingleton (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String componentName,
			@NonNull Class <Type> interfaceClass,
			@NonNull Type component) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerSingleton");

		) {

			if (
				doesNotImplement (
					component.getClass (),
					interfaceClass)
			) {

				throw new IllegalArgumentException (
					stringFormat (
						"Component does not implement: %s",
						classNameFull (
							interfaceClass)));

			}

			if (
				mapContainsKey (
					componentsByName,
					componentName)
			) {

				throw new IllegalArgumentException (
					stringFormat (
						"Duplicated component name: %s",
						componentName));

			}

			ComponentData componentData =
				new ComponentData ()

				.name (
					componentName)

				.scope (
					ComponentScope.singleton)

				.interfaceClass (
					interfaceClass)

				.implementationClass (
					component.getClass ())

				.component (
					optionalOf (
						component))

			;

			addComponentData (
				taskLogger,
				componentName,
				interfaceClass,
				componentData);

			return this;

		}

	}

	public synchronized
	BootstrapComponentManager registerClass (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <?> interfaceClass,
			@NonNull Class <?> implementationClass) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerClass");

		) {

			SingletonComponent singletonComponentAnnotation =
				implementationClass.getAnnotation (
					SingletonComponent.class);

			PrototypeComponent prototypeComponentAnnotation =
				implementationClass.getAnnotation (
					PrototypeComponent.class);

			if (

				isNotNull (
					singletonComponentAnnotation)

				&& isNotNull (
					prototypeComponentAnnotation)

			) {
				throw new IllegalArgumentException ();
			}

			if (
				isNotNull (
					singletonComponentAnnotation)
			) {

				registerSingletonClass (
					taskLogger,
					singletonComponentAnnotation,
					interfaceClass,
					implementationClass);

			} else if (
				isNotNull (
					prototypeComponentAnnotation)
			) {

				registerPrototypeClass (
					taskLogger,
					prototypeComponentAnnotation,
					interfaceClass,
					implementationClass);

			} else {

				throw new IllegalArgumentException (
					stringFormat (
						"No component annotation found on %s",
						classNameFull (
							implementationClass)));

			}

			return this;

		}

	}

	public synchronized
	void bootstrapComponent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Object component) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"bootstrapComponent");

		) {

			initialiseComponent (
				taskLogger,
				component);

		}

	}

	public synchronized
	void bootstrapComponent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Object component,
			@NonNull String componentName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"bootstrapComponent");

		) {

			initialiseComponent (
				taskLogger,
				component);

		}

	}

	public synchronized
	BootstrapComponentManager setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			componentsByName.values ().stream ()

				.filter (
					componentData ->
						enumEqualSafe (
							componentData.scope (),
							ComponentScope.singleton))

				.forEach (
					componentData ->
						getComponentReal (
							componentData))

			;

			return this;

		}

	}

	public
	BootstrapComponentManager registerStandardClasses (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerStandardClasses");

		) {

			registerClass (
				taskLogger,
				AnnotatedClassComponentTools.class,
				AnnotatedClassComponentTools.class);

			registerClass (
				taskLogger,
				ComponentManagerBuilder.class,
				ComponentManagerBuilder.class);

			registerClass (
				taskLogger,
				ComponentRegistryImplementation.class,
				ComponentRegistryImplementation.class);

			registerClass (
				taskLogger,
				DataFromXmlBuilder.class,
				DataFromXmlBuilder.class);

			registerClass (
				taskLogger,
				DataFromXmlImplementation.class,
				DataFromXmlImplementation.class);

			registerClass (
				taskLogger,
				PluginManager.class,
				PluginManagerFactory.class);

			registerClass (
				taskLogger,
				WbsConfig.class,
				WbsConfigFactory.class);

			registerClass (
				taskLogger,
				BuildSpec.class,
				BuildSpecFactory.class);

			return this;

		}

	}

	public
	BootstrapComponentManager registerPluginBootstrapComponents (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Iterable <String> layerNames) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerPluginBootstrapComponents");

		) {

			layerNames.forEach (
				layerName ->
					registerPluginBootstrapComponents (
						taskLogger,
						layerName));

			return this;

		}

	}

	public
	BootstrapComponentManager registerPluginBootstrapComponents (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String layerName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerPluginBootstrapComponents",
					keyEqualsString (
						"layerName",
						layerName));

		) {

			PluginManager pluginManager =
				getComponentRequired (
					taskLogger,
					"pluginManager",
					PluginManager.class);

			for (
				PluginSpec pluginSpec
					: pluginManager.plugins ()
			) {

				for (
					PluginLayerSpec pluginLayerSpec
						: pluginSpec.layers ()
				) {

					if (
						stringNotEqualSafe (
							pluginLayerSpec.name (),
							layerName)
					) {
						continue;
					}

					for (
						PluginBootstrapComponentSpec pluginComponentSpec
							: pluginLayerSpec.bootstrapComponents ()
					) {

						registerPluginBootstrapComponent (
							taskLogger,
							stringFormat (
								"%s.%s",
								pluginSpec.packageName (),
								pluginComponentSpec.className ()));

					}

				}

			}

			return this;

		}

	}

	// component manager implementation

	@Override
	public <ComponentType>
	Optional <Provider <ComponentType>> getComponentProvider (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String componentName,
			@NonNull Class <ComponentType> componentClass) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getComponentProvider");

		) {

			Optional <ComponentData> componentDataOptional =
				mapItemForKey (
					componentsByName,
					componentName);

			if (
				optionalIsNotPresent (
					componentDataOptional)
			) {
				return optionalAbsent ();
			}

			ComponentData componentData =
				optionalGetRequired (
					componentDataOptional);

			if (
				isNotSubclassOf (
					componentClass,
					componentData.interfaceClass ())
			) {
				throw new ClassCastException ();
			}

			return optionalOf (
				() ->
					componentClass.cast (
						getComponentReal (
							componentData)));

		}

	}

	@Override
	public
	Map <String, Pair <Class <?>, Object>> allSingletonComponents (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"allSingletonComponents");

		) {

			return componentsByName.values ().stream ()

				.filter (
					componentData ->
						enumEqualSafe (
							componentData.scope (),
							ComponentScope.singleton))

				.collect (
					Collectors.toMap (
						componentData ->
							componentData.name (),
						componentData ->
							Pair.of (
								componentData.interfaceClass (),
								getComponentReal (
									componentData))))

			;

		}


	}

	@Override
	public
	List <String> requestComponentNames () {

		throw todo ();

	}

	@Override
	public
	void bootstrapComponent (
			@NonNull Object component) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.createTaskLogger (
					"bootstrapComponent");

		) {

			initialiseComponent (
				taskLogger,
				component);

		}

	}

	@Override
	public
	void bootstrapComponent (
			@NonNull Object component,
			@NonNull String componentName) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.createTaskLogger (
					"bootstrapComponent",
					keyEqualsString (
						"componentName",
						componentName));

		) {

			initialiseComponent (
				taskLogger,
				component);

		}

	}

	@Override
	public
	ComponentMetaData componentMetaData (
			@NonNull Object component) {

		throw todo ();

	}

	// safe closeable implementation

	@Override
	public
	void close () {

		doNothing ();

	}

	// private implementation

	private
	void registerPluginBootstrapComponent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String className) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerSingletonClass");

		) {

debugFormat (
	"Boootstrap component %s",
	className);

			Class <?> providedClass =
				classForNameRequired (
					className);

			ComponentInterface componentInterfaceAnnotation =
				providedClass.getAnnotation (
					ComponentInterface.class);

			if (
				isNotNull (
					componentInterfaceAnnotation)
			) {

				registerClass (
					taskLogger,
					componentInterfaceAnnotation.value (),
					providedClass);

			} else {

				registerClass (
					taskLogger,
					providedClass,
					providedClass);

			}

		}

	}

	private synchronized
	void registerSingletonClass (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SingletonComponent singletonComponentAnnotation,
			@NonNull Class <?> interfaceClass,
			@NonNull Class <?> implementationClass) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerSingletonClass");

		) {

			if (
				mapContainsKey (
					componentsByName,
					singletonComponentAnnotation.value ())
			) {

				throw new RuntimeException (
					stringFormat (
						"Duplicated component name: %s",
						singletonComponentAnnotation.value ()));

			}

			if (
				mapContainsKey (
					componentsByInterface,
					interfaceClass)
			) {

				throw new RuntimeException (
					stringFormat (
						"Duplicated component class: %s",
						classNameFull (
							interfaceClass)));

			}

			ComponentData componentData =
				new ComponentData ()

				.name (
					singletonComponentAnnotation.value ())

				.scope (
					ComponentScope.singleton)

				.interfaceClass (
					interfaceClass)

				.implementationClass (
					implementationClass)

				.component (
					optionalAbsent ())

			;

			addComponentData (
				taskLogger,
				singletonComponentAnnotation.value (),
				interfaceClass,
				componentData);

		}

	}

	private
	void registerPrototypeClass (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull PrototypeComponent prototypeComponentAnnotation,
			@NonNull Class <?> interfaceClass,
			@NonNull Class <?> implementationClass) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerSingletonClass");

		) {

			if (
				mapContainsKey (
					componentsByName,
					prototypeComponentAnnotation.value ())
			) {

				throw new RuntimeException (
					stringFormat (
						"Duplicated component name: %s",
						prototypeComponentAnnotation.value ()));

			}

			if (
				mapContainsKey (
					componentsByInterface,
					interfaceClass)
			) {

				throw new RuntimeException (
					stringFormat (
						"Duplicated component class: %s",
						classNameFull (
							interfaceClass)));

			}

			ComponentData componentData =
				new ComponentData ()

				.name (
					prototypeComponentAnnotation.value ())

				.scope (
					ComponentScope.prototype)

				.interfaceClass (
					interfaceClass)

				.implementationClass (
					implementationClass)

			;

			addComponentData (
				taskLogger,
				prototypeComponentAnnotation.value (),
				interfaceClass,
				componentData);

		}

	}

	private
	void addComponentData (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String componentName,
			@NonNull Class <?> interfaceClass,
			@NonNull ComponentData componentData) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"addComponentData");

		) {

			componentsByName.put (
				componentName,
				componentData);

			Set <Class <?>> allInterfaceClasses =
				new HashSet<> ();

			allInterfaceClasses.add (
				interfaceClass);

			allInterfaceClasses.addAll (
				ClassUtils.getAllSuperclasses (
					interfaceClass));

			allInterfaceClasses.addAll (
				ClassUtils.getAllInterfaces (
					interfaceClass));

			allInterfaceClasses.forEach (
				allInterfaceClass -> {

					List <ComponentData> componentsForInterface =
						mapItemForKeyOrElseSet (
							componentsByInterface,
							allInterfaceClass,
							() -> new ArrayList<> ());

					componentsForInterface.add (
						componentData);

			});

		}

	}

	private synchronized
	Object getComponentReal (
			@NonNull ComponentData componentData) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.createTaskLogger (
					"getComponentReal");

		) {

			switch (componentData.scope ()) {

			case singleton:

				if (
					optionalIsNotPresent (
						componentData.component ())
				) {

					componentData.component (
						optionalOf (
							createComponentReal (
								taskLogger,
								componentData)));

				}

				return optionalGetRequired (
					componentData.component ());

			case prototype:

				return createComponentReal (
					taskLogger,
					componentData);

			default:

				throw shouldNeverHappen ();

			}

		}

	}

	private synchronized
	Object createComponentReal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentData componentData) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createComponentReal",
					keyEqualsString (
						"componentName",
						componentData.name ()));

		) {

			Object component =
				classInstantiate (
					componentData.implementationClass ());

			initialiseComponent (
				taskLogger,
				component);

			if (
				isInstanceOf (
					ComponentFactory.class,
					component)
			) {

				ComponentFactory <?> componentFactory =
					genericCastUnchecked (
						component);

				component =
					componentFactory.makeComponent (
						taskLogger);

			}

			return component;

		}

	}

	private synchronized
	void initialiseComponent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Object component) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initialiseComponent");

		) {

			for (
				Field field
					: FieldUtils.getAllFields (
						component.getClass ())
			) {

				initialiseComponentField (
					taskLogger,
					component,
					field);

			}

		}

	}

	private
	void initialiseComponentField (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Object component,
			@NonNull Field field) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initialiseComponentField");

		) {

			SingletonDependency singletonDependencyAnnotation =
				field.getAnnotation (
					SingletonDependency.class);

			ClassSingletonDependency classSingletonDependencyAnnotation =
				field.getAnnotation (
					ClassSingletonDependency.class);

			PrototypeDependency prototypeDependencyAnnotation =
				field.getAnnotation (
					PrototypeDependency.class);

			StrongPrototypeDependency strongPrototypeDependencyAnnotation =
				field.getAnnotation (
					StrongPrototypeDependency.class);

			UninitializedDependency uninitializedDependencyAnnotation =
				field.getAnnotation (
					UninitializedDependency.class);

			List <Annotation> allAnnotations =
				ImmutableList.<Annotation> copyOf (
					presentInstances (
						optionalFromNullable (
							classSingletonDependencyAnnotation),
						optionalFromNullable (
							prototypeDependencyAnnotation),
						optionalFromNullable (
							singletonDependencyAnnotation),
						optionalFromNullable (
							strongPrototypeDependencyAnnotation),
						optionalFromNullable (
							uninitializedDependencyAnnotation)));

			if (
				collectionIsEmpty (
					allAnnotations)
			) {
				return;
			}

			if (
				collectionHasMoreThanOneElement (
					allAnnotations)
			) {
				throw new RuntimeException ();
			}

			Optional <Annotation> qualifierAnnotationOptional =
				findQualifierAnnotation (
					Arrays.asList (
						field.getAnnotations ()));

			field.setAccessible (
				true);

			if (
				isNotNull (
					singletonDependencyAnnotation)
			) {

				Class <?> targetClass =
					field.getType ();

				List <ComponentData> componentDatas =
					componentDatasForClass (
						taskLogger,
						targetClass,
						qualifierAnnotationOptional);

				if (
					collectionIsEmpty (
						componentDatas)
				) {

					throw new RuntimeException (
						stringFormat (
							"Singleton dependency %s.%s ",
							classNameFull (
								component.getClass ()),
							field.getName (),
							"of type %s ",
							classNameFull (
								targetClass),
							"not found"));

				}

				if (
					collectionHasMoreThanOneElement (
						componentDatas)
				) {

					throw new RuntimeException (
						stringFormat (
							"Singleton dependency %s.%s ",
							classNameFull (
								component.getClass ()),
							field.getName (),
							"of type %s ",
							classNameFull (
								targetClass),
							"has multiple candidates"));

				}

				ComponentData componentData =
					iterableOnlyItemRequired (
						componentDatas);

				fieldSet (
					field,
					component,
					optionalOf (
						getComponentReal (
							componentData)));

			} else if (
				isNotNull (
					classSingletonDependencyAnnotation)
			) {

				if (
					classNotEqual (
						field.getType (),
						LogContext.class)
				) {
					throw todo ();
				}

				fieldSet (
					field,
					component,
					optionalOf (
						loggingLogic.findOrCreateLogContext (
							classNameFull (
								component.getClass ()))));

			} else if (
				isNotNull (
					prototypeDependencyAnnotation)
				|| isNotNull (
					strongPrototypeDependencyAnnotation)
				|| isNotNull (
					uninitializedDependencyAnnotation)
			) {

				if (
					classEqualSafe (
						field.getType (),
						Provider.class)
				) {

					ParameterizedType parameterizedType =
						(ParameterizedType)
						field.getGenericType ();

					Class <?> targetClass =
						(Class <?>)
						parameterizedType.getActualTypeArguments () [0];

					List <ComponentData> componentDatas =
						componentDatasForClass (
							taskLogger,
							targetClass,
							qualifierAnnotationOptional);

					if (
						collectionIsEmpty (
							componentDatas)
					) {

						throw new RuntimeException (
							stringFormat (
								"Prototype dependency %s.%s ",
								classNameFull (
									component.getClass ()),
								field.getName (),
								"of type %s ",
								classNameFull (
									targetClass),
								"not found"));

					}

					if (
						collectionHasMoreThanOneElement (
							componentDatas)
					) {

						throw new RuntimeException (
							stringFormat (
								"Prototype dependency %s.%s ",
								classNameFull (
									component.getClass ()),
								field.getName (),
								"of type %s ",
								classNameFull (
									targetClass),
								"has multiple candidates"));

					}

					ComponentData componentData =
						iterableOnlyItemRequired (
							componentDatas);

					fieldSet (
						field,
						component,
						optionalOf (
							(Provider <?>)
							() -> getComponentReal (
								componentData)));

				} else if (
					classEqualSafe (
						field.getType (),
						Map.class)
				) {

					ParameterizedType parameterizedFieldType =
						genericCastUnchecked (
							field.getGenericType ());

					Class <?> keyType =
						rawType (
							parameterizedFieldType
								.getActualTypeArguments () [0]);

					ParameterizedType providerType =
						genericCastUnchecked (
							parameterizedFieldType
								.getActualTypeArguments () [1]);

					if (
						classNotEqual (
							(Class <?>) providerType.getRawType (),
							Provider.class)
					) {
						throw new RuntimeException ();
					}

					Class <?> valueType =
						rawType (
							providerType.getActualTypeArguments () [0]);

					List <ComponentData> componentDatas =
						componentDatasForClass (
							taskLogger,
							valueType,
							qualifierAnnotationOptional);

					if (
						classEqualSafe (
							keyType,
							String.class)
					) {

						Map <String, Provider <?>> mapToInject =
							iterableTransformToMap (
								componentDatas,
								componentData ->
									componentData.name (),
								componentData ->
									(Provider <?>)
									() -> getComponentReal (
										componentData));

						fieldSet (
							field,
							component,
							optionalOf (
								mapToInject));

					} else if (
						classEqualSafe (
							keyType,
							Class.class)
					) {

						Map <Class <?>, Provider <?>> mapToInject =
							iterableTransformToMap (
								componentDatas,
								componentData ->
									componentData.interfaceClass (),
								componentData ->
									(Provider <?>)
									() -> getComponentReal (
										componentData));

						fieldSet (
							field,
							component,
							optionalOf (
								mapToInject));

					} else {

						throw new RuntimeException (
							stringFormat (
								"Don't support maps with key type %s",
								classNameSimple (
									keyType)));

					}

				} else {

					throw new RuntimeException (
						stringFormat (
							"Prototype dependency %s.%s ",
							classNameFull (
								component.getClass ()),
							field.getName (),
							"is of type %s ",
							classNameSimple (
								field.getType ()),
							"(expected Provider)"));

				}

			} else {

				throw shouldNeverHappen ();

			}

		}

	}

	private
	List <ComponentData> componentDatasForClass (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <?> targetClass,
			@NonNull Optional <Annotation> qualifierAnnotation) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"componentDatasForClass");

		) {

			List <ComponentData> componentDatas =
				optionalOrElseRequired (
					mapItemForKey (
						componentsByInterface,
						targetClass),
					() -> emptyList ());

			if (
				optionalIsPresent (
					qualifierAnnotation)
			) {

				componentDatas =
					iterableFilterToList (
						componentData ->
							optionalIsPresent (
								findQualifierAnnotation (
									componentData.implementationClass ())),
						componentDatas);

			}

			return componentDatas;

		}

	}

	private
	Optional <Annotation> findQualifierAnnotation (
			@NonNull Class <?> targetClass) {

		return findQualifierAnnotation (
			Arrays.asList (
				targetClass.getAnnotations ()));

	}

	private
	Optional <Annotation> findQualifierAnnotation (
			@NonNull List <Annotation> annotations) {

		return optionalFromJava (
			annotations.stream ()

			.filter (
				annotation ->
					isNotNull (
						annotation.getClass ().getAnnotation (
							Qualifier.class)))

			.findFirst ()

		);

	}

	// component data

	@Accessors (fluent = true)
	@Data
	public static
	class ComponentData {

		String name;

		ComponentScope scope;

		Class <?> interfaceClass;
		Class <?> implementationClass;

		Optional <Object> component;

	}

	public static
	enum ComponentScope {
		singleton,
		prototype;
	}

}
