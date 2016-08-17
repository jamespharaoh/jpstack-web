package wbs.framework.application.context;

import static wbs.framework.utils.etc.CollectionUtils.iterableCount;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.isZero;
import static wbs.framework.utils.etc.Misc.moreThanOne;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.framework.utils.etc.OptionalUtils.presentInstances;
import static wbs.framework.utils.etc.StringUtils.joinWithCommaAndSpace;
import static wbs.framework.utils.etc.StringUtils.joinWithSeparator;
import static wbs.framework.utils.etc.StringUtils.nullIfEmptyString;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.framework.activitymanager.ActiveTask;
import wbs.framework.activitymanager.ActivityManager;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.application.annotations.UninitializedDependency;
import wbs.framework.application.context.EasyReadWriteLock.HeldLock;
import wbs.framework.application.context.InjectedProperty.CollectionType;
import wbs.framework.application.xml.BeansBeanSpec;
import wbs.framework.application.xml.BeansPropertiesPropertySpec;
import wbs.framework.application.xml.BeansPropertyValueSpec;
import wbs.framework.application.xml.BeansReferencePropertySpec;
import wbs.framework.application.xml.BeansSpec;
import wbs.framework.application.xml.BeansValuePropertySpec;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataToXml;
import wbs.framework.utils.etc.BeanLogic;
import wbs.framework.utils.etc.Misc;

/**
 * My not-quite-drop-in replacement for spring's ApplicationContext. This
 * provides all of the features we need, misses out what we don't need, and
 * gives much more helpful error messages.
 *
 * It also is much more deterministic, which means it can work out dependencies
 * at runtime, which is extremely helpful. It will also dump out its bean
 * definitions in XML format to help diagnostics.
 *
 * TODO output ordering, indexes in xml
 * TODO split this into registry and runtime
 * TODO handle qualifiers better
 * TODO handle request scope more elegantly, other scopes
 * TODO cache fields, methods, etc for speed, runtime code gen
 */
@Accessors (fluent = true)
@Log4j
public
class ApplicationContext {

	@Getter @Setter
	String outputPath;

	@Getter @Setter
	ActivityManager activityManager;

	List <ComponentDefinition> componentDefinitions =
		new ArrayList <> ();

	Map <String, ComponentDefinition> componentDefinitionsByName =
		new HashMap <> ();

	Map <Class <?>, Map <String, ComponentDefinition>>
	componentDefinitionsByClass =
		new HashMap <> ();

	Map <Class <?>, Map <String, ComponentDefinition>>
	singletonComponentDefinitionsByClass =
		new HashMap <> ();

	Map <Class <?>, Map <String,ComponentDefinition>>
	prototypeComponentDefinitionsByClass =
		new HashMap<> ();

	Map <Annotation, List <ComponentDefinition>>
	componentDefinitionsByQualifier =
		new HashMap<> ();

	Map <Annotation, List <ComponentDefinition>>
	singletonComponentDefinitionsByQualifier =
		new HashMap<> ();

	Map <Annotation, List <ComponentDefinition>>
	prototypeComponentDefinitionsByQualifier =
		new HashMap <Annotation, List <ComponentDefinition>> ();

	Map <String, Object> singletonComponents =
		new HashMap <> ();

	Set <String> singletonComponentsInCreation =
		new LinkedHashSet <> ();

	Set <String> singletonComponentsFailed =
		new HashSet <> ();

	Map <Object, ComponentMetaData> componentMetaDatas =
		new MapMaker ()
			.weakKeys ()
			.makeMap ();

	// TODO not pretty
	@Getter
	List <String> requestComponentNames =
		new ArrayList <String> ();

	EasyReadWriteLock lock =
		EasyReadWriteLock.instantiate ();

	public <ComponentType>
	ComponentType getComponentRequired (
			String componentName,
			Class <ComponentType> componentClass) {

		@Cleanup
		HeldLock heldLock =
			lock.read ();

		ComponentDefinition componentDefinition =
			componentDefinitionsByName.get (
				componentName);

		if (componentDefinition == null) {

			throw new NoSuchComponentException (
				stringFormat (
					"Bean definition with name %s does not exist",
					componentName));

		}

		return componentClass.cast (
			getComponent (
				componentDefinition,
				true));

	}

	public <ComponentType>
	ComponentType getComponentOrElse (
			String componentName,
			Class <ComponentType> componentClass,
			Provider <ComponentType> orElse) {

		@Cleanup
		HeldLock heldLock =
			lock.read ();

		ComponentDefinition componentDefinition =
			componentDefinitionsByName.get (
				componentName);

		if (
			isNull (
				componentDefinition)
		) {
			return orElse.get ();
		}

		return componentClass.cast (
			getComponent (
				componentDefinition,
				true));

	}

	public <ComponentType>
	Provider <ComponentType> getComponentProvider (
			String componentName,
			Class <ComponentType> componentClass) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		ComponentDefinition componentDefinition =
			componentDefinitionsByName.get (
				componentName);

		if (componentDefinition == null) {

			throw new NoSuchComponentException (
				stringFormat (
					"Bean definition with name %s does not exist",
					componentName));

		}

		if (
			! componentClass.isAssignableFrom (
				componentDefinition.beanClass ())
		) {

			throw new NoSuchComponentException (
				stringFormat (
					"Bean definition with name %s is of type %s instead of %s",
					componentName,
					componentDefinition.beanClass ().getName (),
					componentClass.getName ()));

		}

		@SuppressWarnings ("unchecked")
		Provider<ComponentType> beanProvider =
			(Provider<ComponentType>)
			getBeanProvider (
				componentDefinition);

		return beanProvider;

	}

	public
	Map<String,Object> getAllSingletonBeans () {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		Map<String,Object> map =
			new HashMap<String,Object> ();

		for (ComponentDefinition beanDefinition
				: componentDefinitions) {

			if (! equal (
					beanDefinition.scope (),
					"singleton"))
				continue;

			map.put (
				beanDefinition.name (),
				getComponent (
					beanDefinition,
					true));

		}

		return map;

	}

	public
	List<ComponentDefinition> getBeanDefinitionsWithAnnotation (
			Class<? extends Annotation> annotationClass) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		ImmutableList.Builder<ComponentDefinition>
			beanDefinitionsWithAnnotationBuilder =
				ImmutableList.<ComponentDefinition>builder ();

		for (
			ComponentDefinition beanDefinition
				: componentDefinitions
		) {

			Annotation annotation =
				beanDefinition.beanClass ().getAnnotation (annotationClass);

			if (annotation == null)
				continue;

			beanDefinitionsWithAnnotationBuilder.add (
				beanDefinition);

		}

		return beanDefinitionsWithAnnotationBuilder.build ();

	}

	private
	Object getComponent (
			ComponentDefinition beanDefinition,
			Boolean initialize) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		if (
			equal (
				beanDefinition.scope (),
				"prototype")
		) {

			return instantiateBean (
				beanDefinition,
				initialize);

		} else if (
			equal (
				beanDefinition.scope (),
				"singleton")
		) {

			if (! initialize) {
				throw new IllegalArgumentException ();
			}

			Object bean =
				singletonComponents.get (
					beanDefinition.name ());

			if (bean != null)
				return bean;

			if (
				singletonComponentsInCreation.contains (
					beanDefinition.name ())
			) {

				throw new RuntimeException (
					stringFormat (
						"Singleton bean %s already in creation (%s)",
						beanDefinition.name (),
						joinWithCommaAndSpace (
							singletonComponentsInCreation)));

			}

			if (
				singletonComponentsFailed.contains (
					beanDefinition.name ())
			) {

				throw new RuntimeException (
					stringFormat (
						"Singleton bean %s already failed",
						beanDefinition.name ()));

			}

			singletonComponentsInCreation.add (
				beanDefinition.name ());

			try {

				bean =
					instantiateBean (
						beanDefinition,
						true);

				singletonComponents.put (
					beanDefinition.name (),
					bean);

			} finally {

				singletonComponentsInCreation.remove (
					beanDefinition.name ());

				if (bean == null)
					singletonComponentsFailed.add (
						beanDefinition.name ());

			}

			return bean;

		} else {

			throw new RuntimeException (
				stringFormat (
					"Unrecognised scope %s for bean %s",
					beanDefinition.scope (),
					beanDefinition.name ()));

		}

	}

	@SneakyThrows (Exception.class)
	public
	Object instantiateBean (
			@NonNull ComponentDefinition beanDefinition,
			@NonNull Boolean initialize) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		@Cleanup
		ActiveTask activeTask =
			activityManager.start (
				"application-context",
				stringFormat (
					"instantiateBean (%s)",
					beanDefinition.name ()),
				this);

		log.debug (
			stringFormat (
				"Instantiating %s (%s)",
				beanDefinition.name (),
				beanDefinition.scope ()));

		// instantiate

		Class <?> instantiateClass =
			ifNull (
				beanDefinition.factoryClass (),
				beanDefinition.beanClass ());

		Object protoComponent =
			instantiateClass.newInstance ();

		// set properties

		setBeanValueProperties (
			beanDefinition,
			protoComponent);

		setBeanReferenceProperties (
			beanDefinition,
			protoComponent);

		setBeanInjectedProperties (
			beanDefinition,
			protoComponent);

		// call factory

		Object component;
		ComponentMetaData componentMetaData;

		if (
			isNotNull (
				beanDefinition.factoryClass ())
		) {

			ComponentFactory componentFactory =
				(ComponentFactory)
				protoComponent;

			component =
				componentFactory.makeComponent ();

			if (
				isNull (
					component)
			) {

				throw new RuntimeException (
					stringFormat (
						"Factory bean returned null for %s",
						beanDefinition.name ()));

			}

			componentMetaData =
				findOrCreateMetaDataForComponent (
					beanDefinition,
					component);

		} else {

			component =
				protoComponent;

			componentMetaData =
				findOrCreateMetaDataForComponent (
					beanDefinition,
					component);

		}

		// initialize

		if (

			initialize

			&& equal (
				componentMetaData.state,
				BeanState.uninitialized)

		) {

			initializeBean (
				beanDefinition,
				component,
				componentMetaData);

		}

		// and finish

		log.debug (
			stringFormat (
				"Bean %s instantiated successfully",
				beanDefinition.name ()));

		return component;

	}

	@SneakyThrows (Exception.class)
	private
	void initializeBean (
			@NonNull ComponentDefinition beanDefinition,
			@NonNull Object bean,
			@NonNull ComponentMetaData beanMetaData) {

		synchronized (beanMetaData) {

			if (
				notEqual (
					beanMetaData.state,
					BeanState.uninitialized)
			) {

				throw new IllegalStateException (
					stringFormat (
						"Tried to initialize component %s ",
						beanMetaData.definition.name (),
						"in %s state",
						beanMetaData.state.name ()));

			}
		
			try {

				// run post construct
		
				for (
					Method method
						: bean.getClass ().getMethods ()
				) {
	
					PostConstruct postConstructAnnotation =
						method.getAnnotation (
							PostConstruct.class);
	
					if (postConstructAnnotation == null)
						continue;
	
					log.debug (
						stringFormat (
							"Running post construct method %s.%s",
							beanDefinition.name (),
							method.getName ()));
	
					method.invoke (
						bean);
	
				}

				beanMetaData.state =
					BeanState.active;

			} finally {

				if (
					notEqual (
						beanMetaData.state,
						BeanState.active)
				) {

					beanMetaData.state =
						BeanState.error;

				}

			}

		}

	}

	private
	ComponentMetaData findOrCreateMetaDataForComponent (
			@NonNull ComponentDefinition componentDefinition,
			@NonNull Object component) {

		// create bean info

		return componentMetaDatas.computeIfAbsent (
			component,
			_component -> {

			ComponentMetaData newBeanMetaData =
				new ComponentMetaData ();

			newBeanMetaData.definition =
				componentDefinition;

			newBeanMetaData.state =
				componentDefinition.owned ()
					? BeanState.uninitialized
					: BeanState.unmanaged;

			return newBeanMetaData;

		});

	}

	private
	void setBeanValueProperties (
			ComponentDefinition beanDefinition,
			Object bean) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		for (
			Map.Entry<String,Object> entry
				: beanDefinition.valueProperties ().entrySet ()
		) {

			log.debug (
				stringFormat (
					"Setting value property %s.%s",
					beanDefinition.name (),
					entry.getKey ()));

			BeanLogic.set (
				bean,
				entry.getKey (),
				entry.getValue ());

		}

	}

	private
	void setBeanReferenceProperties (
			ComponentDefinition beanDefinition,
			Object bean) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		for (
			Map.Entry<String,String> entry
				: beanDefinition.referenceProperties ().entrySet ()
		) {

			log.debug (
				stringFormat (
					"Setting reference property %s.%s",
					beanDefinition.name (),
					entry.getKey ()));

			Object target =
				getComponentRequired (
					entry.getValue (),
					Object.class);

			BeanLogic.set (
				bean,
				entry.getKey (),
				target);

		}

	}

	private
	void setBeanInjectedProperties (
			ComponentDefinition beanDefinition,
			Object bean)
		throws Exception {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		for (
			InjectedProperty injectedProperty
				: beanDefinition.injectedProperties ()
		) {

			log.debug (
				stringFormat (
					"Setting injected property %s.%s",
					beanDefinition.name (),
					injectedProperty.fieldName ()));

			// lookup target beans

			List<Pair<ComponentDefinition,Object>> targetBeans =
				new ArrayList<Pair<ComponentDefinition,Object>> ();

			for (
				String targetBeanDefinitionName
					: injectedProperty.targetBeanNames ()
			) {

				ComponentDefinition targetBeanDefinition =
					componentDefinitionsByName.get (
						targetBeanDefinitionName);

				Object injectValue;

				if (injectedProperty.provider ()) {

					injectValue =
						getBeanProvider (
							targetBeanDefinition,
							injectedProperty.initialized ());

				} else {

					injectValue =
						getComponent (
							targetBeanDefinition,
							injectedProperty.initialized ());

				}

				targetBeans.add (
					Pair.of (
						targetBeanDefinition,
						injectValue));

			}

			// package appropriately

			Object value;

			switch (injectedProperty.collectionType ()) {

			case beanClassMap:

				Map<Class<?>,Object> beanClassMap =
					new LinkedHashMap<Class<?>,Object> ();

				for (
					Pair<ComponentDefinition,Object> pair
						: targetBeans
				) {

					beanClassMap.put (
						pair.getLeft ().beanClass (),
						pair.getRight ());

				}

				value = beanClassMap;

				break;

			case beanNameMap:

				Map<String,Object> beanNameMap =
					new LinkedHashMap<String,Object> ();

				for (
					Pair<ComponentDefinition,Object> pair
						: targetBeans
				) {

					beanNameMap.put (
						pair.getLeft ().name (),
						pair.getRight ());

				}

				value = beanNameMap;

				break;

			case list:

				List<Object> beansList =
					new ArrayList<Object> ();

				for (Pair<ComponentDefinition,Object> pair
						: targetBeans) {

					beansList.add (
						pair.getRight ());

				}

				value = beansList;

				break;

			case single:

				if (targetBeans.size () != 1) {

					throw new RuntimeException (
						stringFormat (
							"Trying to inject %s beans into a single field %s.%s",
							targetBeans.size (),
							beanDefinition.name (),
							injectedProperty.fieldName ()));

				}

				value =
					targetBeans.get (0).getRight ();

				break;

			default:

				throw new RuntimeException ();

			}

			// and inject it

			Field field =
				injectedProperty
					.fieldDeclaringClass ()
					.getDeclaredField (
						injectedProperty.fieldName ());

			field.setAccessible (
				true);

			field.set (
				bean,
				value);

		}

	}

	public
	ApplicationContext registerBeanDefinition (
			@NonNull ComponentDefinition beanDefinition) {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		// sanity check

		if (beanDefinition.name () == null) {

			throw new RuntimeException (
				stringFormat (
					"Bean definition has no name"));

		}

		if (beanDefinition.beanClass () == null) {

			throw new RuntimeException (
				stringFormat (
					"Bean definition %s has no bean class",
					beanDefinition.beanClass ()));

		}

		if (beanDefinition.scope () == null) {

			throw new RuntimeException (
				stringFormat (
					"Bean definition %s has no scope",
					beanDefinition.name ()));

		}

		if (componentDefinitionsByName.containsKey (
				beanDefinition.name ())) {

			throw new RuntimeException (
				stringFormat (
					"Duplicated bean definition name %s",
					beanDefinition.name ()));

		}

		if (! in (
				beanDefinition.scope (),
				"singleton",
				"prototype")) {

			throw new RuntimeException (
				stringFormat (
					"Bean definition %s has invalid scope %s",
					beanDefinition.name (),
					beanDefinition.scope ()));

		}

		Class<?> instantiationClass =
			ifNull (
				beanDefinition.factoryClass (),
				beanDefinition.beanClass ());

		// check the class looks ok

		if (! Modifier.isPublic (
				instantiationClass.getModifiers ())) {

			throw new RuntimeException (
				stringFormat (
					"Bean definition %s refers to non-public class %s",
					beanDefinition.name (),
					instantiationClass.getName ()));

		}

		if (Modifier.isAbstract (
				instantiationClass.getModifiers ())) {

			throw new RuntimeException (
				stringFormat (
					"Bean definition %s ref	ers to abstract class %s",
					beanDefinition.name (),
					instantiationClass.getName ()));

		}

		Constructor<?> constructor;

		try {

			constructor =
				instantiationClass.getDeclaredConstructor ();

		} catch (NoSuchMethodException exception) {

			throw new RuntimeException (
				stringFormat (
					"Bean definition %s refers class %s with no default ",
					beanDefinition.name (),
					instantiationClass.getName (),
					"constructor"));

		}

		if (
			! Modifier.isPublic (
				constructor.getModifiers ())
		) {

			throw new RuntimeException (
				stringFormat (
					"Bean definition %s refers to class %s with non-public ",
					beanDefinition.name (),
					instantiationClass.getName (),
					"default constructor"));

		}

		// store bean definition

		componentDefinitions.add (
			beanDefinition);

		componentDefinitionsByName.put (
			beanDefinition.name (),
			beanDefinition);

		// index by class

		if (! beanDefinition.hide ()) {

			Set<Class<?>> beanClasses =
				new HashSet<Class<?>> ();

			beanClasses.add (
				beanDefinition.beanClass ());

			beanClasses.addAll (
				ClassUtils.getAllSuperclasses (
					beanDefinition.beanClass ()));

			beanClasses.addAll (
				ClassUtils.getAllInterfaces (
					beanDefinition.beanClass ()));

			updateIndexByClass (
				componentDefinitionsByClass,
				beanClasses,
				beanDefinition);

			if (
				equal (
					beanDefinition.scope (),
					"singleton")
			) {

				updateIndexByClass (
					singletonComponentDefinitionsByClass,
					beanClasses,
					beanDefinition);

			}

			if (
				equal (
					beanDefinition.scope (),
					"prototype")
			) {

				updateIndexByClass (
					prototypeComponentDefinitionsByClass,
					beanClasses,
					beanDefinition);

			}

		}

		// index by qualifiers

		if (! beanDefinition.hide ()) {

			for (
				Annotation annotation
					: beanDefinition.beanClass ().getDeclaredAnnotations ()
			) {

				Qualifier qualifierAnnotation =
					annotation
						.annotationType ()
						.getAnnotation (Qualifier.class);

				if (qualifierAnnotation == null)
					continue;

				updateIndexByQualifier (
					componentDefinitionsByQualifier,
					annotation,
					beanDefinition);

				if (
					equal (
						beanDefinition.scope (),
						"singleton")
				) {

					updateIndexByQualifier (
						singletonComponentDefinitionsByQualifier,
						annotation,
						beanDefinition);

				}

				if (
					equal (
						beanDefinition.scope (),
						"prototype")
				) {

					updateIndexByQualifier (
						prototypeComponentDefinitionsByQualifier,
						annotation,
						beanDefinition);

				}

			}

		}

		return this;

	}

	private
	void updateIndexByClass (
			Map<Class<?>,Map<String,ComponentDefinition>> index,
			Set<Class<?>> beanClasses,
			ComponentDefinition beanDefinition) {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		for (
			Class<?> beanClass
				: beanClasses
		) {

			Map<String,ComponentDefinition> beanDefinitionsForClass =
				index.get (
					beanClass);

			if (beanDefinitionsForClass == null) {

				index.put (
					beanClass,
					beanDefinitionsForClass =
						new HashMap<String,ComponentDefinition> ());

			}

			beanDefinitionsForClass.put (
				beanDefinition.name (),
				beanDefinition);

		}

	}

	private
	void updateIndexByQualifier (
			Map<Annotation,List<ComponentDefinition>> index,
			Annotation annotation,
			ComponentDefinition beanDefinition) {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		List<ComponentDefinition> beanDefinitionsForQualifier =
			index.get (annotation);

		if (beanDefinitionsForQualifier == null) {

			index.put (
				annotation,
				beanDefinitionsForQualifier =
					new ArrayList<ComponentDefinition> ());

		}

		beanDefinitionsForQualifier.add (
			beanDefinition);

	}

	public
	ApplicationContext init () {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		int errors = 0;

		// automatic beans

		registerUnmanagedSingleton (
			"applicationContext",
			this);

		registerUnmanagedSingleton (
			"activityManager",
			activityManager);

		// work out dependencies

		for (
			ComponentDefinition beanDefinition
				: componentDefinitions
		) {

			errors +=
				initBeanDefinition (
					beanDefinition);

		}

		// check dependencies exist

		for (
			ComponentDefinition beanDefinition
				: componentDefinitions
		) {

			for (
				String dependency
					: beanDefinition.orderedDependencies ()
			) {

				if (
					! componentDefinitionsByName.containsKey (
						dependency)
				) {

					log.error (
						stringFormat (
							"Can't provide dependency %s for %s",
							dependency,
							beanDefinition.name ()));

					errors ++;

				}

			}

		}

		// order bean definitions

		List<ComponentDefinition> unorderedBeanDefinitions =
			new ArrayList<ComponentDefinition> (
				componentDefinitions);

		Map<String,ComponentDefinition> orderedBeanDefinitions =
			new LinkedHashMap<String,ComponentDefinition> ();

		while (! unorderedBeanDefinitions.isEmpty ()) {

			boolean madeProgress = false;

			ListIterator<ComponentDefinition> unorderedBeanDefinitionIterator =
				unorderedBeanDefinitions.listIterator ();

			OUTER: while (
				unorderedBeanDefinitionIterator.hasNext ()
			) {

				ComponentDefinition beanDefinition =
					unorderedBeanDefinitionIterator.next ();

				for (
					String targetBeanDefinitionName
						: beanDefinition.orderedDependencies ()
				) {

					if (! componentDefinitionsByName.containsKey (
							targetBeanDefinitionName))
						continue OUTER;

				}

				log.debug (
					stringFormat (
						"Ordered bean definition %s",
						beanDefinition.name ()));

				orderedBeanDefinitions.put (
					beanDefinition.name (),
					beanDefinition);

				unorderedBeanDefinitionIterator.remove ();

				madeProgress = true;

			}

			if (! madeProgress) {

				for (
					ComponentDefinition beanDefinition
						: unorderedBeanDefinitions
				) {

					List<String> unresolvedDependencyNames =
						new ArrayList<String> (
							Sets.difference (
								beanDefinition.orderedDependencies (),
								orderedBeanDefinitions.keySet ()));

					Collections.sort (
						unresolvedDependencyNames);

					log.error (
						stringFormat (
							"Unable to resolve dependencies for %s (%s)",
							beanDefinition.name (),
							joinWithSeparator (
								", ",
								unresolvedDependencyNames)));

					errors ++;

				}

				break;

			}

		}

		// output bean definitions

		if (outputPath != null)
			outputBeanDefinitions (outputPath);

		// check for errors

		if (errors > 0) {

			throw new RuntimeException (
				stringFormat (
					"Aborting due to %s errors",
					errors));

		}

		// instantiate singletons

		for (
			ComponentDefinition beanDefinition
				: componentDefinitions
		) {

			if (
				! equal (
					beanDefinition.scope (),
					"singleton")
			) {
				continue;
			}

			getComponentRequired (
				beanDefinition.name (),
				Object.class);

		}

		return this;

	}

	public
	int initBeanDefinition (
			ComponentDefinition beanDefinition) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		int errors = 0;

		Class<?> instantiateClass =
			ifNull (
				beanDefinition.factoryClass (),
				beanDefinition.beanClass ());

		beanDefinition.orderedDependencies ().addAll (
			beanDefinition.referenceProperties ().values ());

		for (
			Field field
				: FieldUtils.getAllFields (
					instantiateClass)
		) {

			Inject injectAnnotation =
				field.getAnnotation (
					Inject.class);

			PrototypeDependency prototypeDependencyAnnotation =
				field.getAnnotation (
					PrototypeDependency.class);

			SingletonDependency singletonDependencyAnnotation =
				field.getAnnotation (
					SingletonDependency.class);

			UninitializedDependency uninitializedDependencyAnnotation =
				field.getAnnotation (
					UninitializedDependency.class);

			long numAnnotations =
				iterableCount (
					presentInstances (
						optionalFromNullable (
							injectAnnotation),
						optionalFromNullable (
							prototypeDependencyAnnotation),
						optionalFromNullable (
							singletonDependencyAnnotation),
						optionalFromNullable (
							uninitializedDependencyAnnotation)));

			if (
				isZero (
					numAnnotations)
			) {
				continue;
			}

			if (
				moreThanOne (
					numAnnotations)
			) {
				throw new RuntimeException ();
			}

			Named namedAnnotation =
				field.getAnnotation (
					Named.class);

			if (namedAnnotation != null) {

				errors +=
					initInjectedFieldByName (
						beanDefinition,
						namedAnnotation,
						field,
						isNull (
							uninitializedDependencyAnnotation));

			} else {

				List <Annotation> qualifierAnnotations =
					new ArrayList <Annotation> ();

				for (
					Annotation annotation
						: field.getDeclaredAnnotations ()
				) {

					if (annotation instanceof Named)
						continue;

					Qualifier metaAnnotation =
						annotation.annotationType ().getAnnotation (
							Qualifier.class);

					if (metaAnnotation == null)
						continue;

					qualifierAnnotations.add (
						annotation);

				}

				if (qualifierAnnotations.size () > 1)
					throw new RuntimeException ();

				InjectedProperty injectedProperty =
					new InjectedProperty ()

					.beanDefinition (
						beanDefinition)

					.fieldDeclaringClass (
						field.getDeclaringClass ())

					.fieldName (
						field.getName ())

					.initialized (
						isNull (
							uninitializedDependencyAnnotation));

				errors +=
					initInjectedPropertyField (
						beanDefinition,
						field,
						injectedProperty);

				if (qualifierAnnotations.size () == 1) {

					errors +=
						initInjectedPropertyTargetByQualifier (
							beanDefinition,
							qualifierAnnotations.get (0),
							injectedProperty);

				} else {

					errors +=
						initInjectedPropertyTargetByClass (
							beanDefinition,
							field,
							injectedProperty);

				}

				beanDefinition.injectedProperties ().add (
					injectedProperty);

			}

		}

		return errors;

	}

	private
	int initInjectedFieldByName (
			ComponentDefinition beanDefinition,
			Named namedAnnotation,
			Field field,
			Boolean initialized) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		// TODO merge this

		String targetBeanDefinitionName =
			ifNull (
				nullIfEmptyString (
					namedAnnotation.value ()),
				field.getName ());

		ComponentDefinition targetBeanDefinition =
			componentDefinitionsByName.get (
				targetBeanDefinitionName);

		if (targetBeanDefinition == null) {

			log.error (
				stringFormat (
					"Named bean %s does not exist for %s.%s",
					targetBeanDefinitionName,
					beanDefinition.name (),
					field.getName ()));

			return 1;

		}

		beanDefinition.orderedDependencies.add (
			targetBeanDefinition.name ());

		beanDefinition.injectedProperties ().add (
			new InjectedProperty ()

			.beanDefinition (
				beanDefinition)

			.fieldDeclaringClass (
				field.getDeclaringClass ())

			.fieldName (
				field.getName ())

			.provider (
				field.getType () == Provider.class)

			.initialized (
				initialized)

			.targetBeanNames (
				Collections.singletonList (
					targetBeanDefinitionName)));

		return 0;

	}

	private
	int initInjectedPropertyField (
			ComponentDefinition beanDefinition,
			Field field,
			InjectedProperty injectedProperty) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		Type fieldType =
			field.getGenericType ();

		ParameterizedType parameterizedFieldType =
			field.getGenericType () instanceof ParameterizedType
				? (ParameterizedType) field.getGenericType ()
				: null;

		Class<?> fieldClass =
			parameterizedFieldType != null
				? (Class<?>) parameterizedFieldType.getRawType ()
				: (Class<?>) fieldType;

		// handle collections

		Type injectType;

		InjectedProperty.CollectionType collectionType;

		if (fieldClass == List.class
				&& parameterizedFieldType != null) {

			injectType =
				parameterizedFieldType
					.getActualTypeArguments () [0];

			collectionType = CollectionType.list;

		} else if (fieldClass == Map.class
				&& parameterizedFieldType != null) {

			Type keyType =
				parameterizedFieldType
					.getActualTypeArguments () [0];

			ParameterizedType keyParameterizedType =
				keyType instanceof ParameterizedType
					? (ParameterizedType) keyType
					: null;

			Class<?> keyClass =
				keyParameterizedType != null
					? (Class<?>) keyParameterizedType.getRawType ()
					: (Class<?>) keyType;

			if (! Misc.<Class<?>>in (
					keyClass,
					String.class,
					Class.class)) {

				log.error (
					stringFormat (
						"Don't know how to inject map with key type %s for %s.%s",
						keyType.toString (),
						beanDefinition.name (),
						field.getName ()));

				return 1;

			}

			injectType =
				parameterizedFieldType
					.getActualTypeArguments () [1];

			collectionType =
				keyClass == String.class
					? CollectionType.beanNameMap
					: CollectionType.beanClassMap;

		} else {

			injectType =
				fieldType;

			collectionType =
				CollectionType.single;

		}

		ParameterizedType parameterizedInjectType =
			injectType instanceof ParameterizedType
				? (ParameterizedType) injectType
				: null;

		Class<?> injectClass =
			parameterizedInjectType != null
				? (Class<?>) parameterizedInjectType.getRawType ()
				: (Class<?>) injectType;

		// handle providers

		Type valueType;

		boolean isProvider;

		if (injectClass == Provider.class) {

			if (parameterizedInjectType == null) {

				log.error (
					stringFormat (
						"No type information for provider %s at %s.%s",
						injectType,
						beanDefinition.name (),
						field.getName ()));

				return 1;

			}

			valueType =
				parameterizedInjectType
					.getActualTypeArguments () [0];

			isProvider = true;

		} else {

			valueType =
				injectType;

			isProvider = false;

		}

		// return

		injectedProperty
			.collectionType (collectionType)
			.provider (isProvider)
			.finalType (fieldType)
			.injectType (injectType)
			.targetType (valueType);

		return 0;

	}

	private
	int initInjectedPropertyTargetByClass (
			ComponentDefinition beanDefinition,
			Field field,
			InjectedProperty injectedProperty) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		// lookup target beans

		ParameterizedType parameterizedTargetType =
			injectedProperty.targetType () instanceof ParameterizedType
				? (ParameterizedType) injectedProperty.targetType ()
				: null;

		Class<?> targetClass =
			parameterizedTargetType != null
				? (Class<?>) parameterizedTargetType.getRawType ()
				: (Class<?>) injectedProperty.targetType ();

		Map<String,ComponentDefinition> targetBeanDefinitions =
			ifNull (
				injectedProperty.provider ()
					? ifNull (
						prototypeComponentDefinitionsByClass.get (targetClass),
						singletonComponentDefinitionsByClass.get (targetClass))
					: singletonComponentDefinitionsByClass.get (targetClass),
				Collections.<String,ComponentDefinition>emptyMap ());

		if (injectedProperty.collectionType () == CollectionType.single) {

			if (targetBeanDefinitions.isEmpty ()) {

				log.error (
					stringFormat (
						"Unable to find bean of type %s for %s.%s",
						injectedProperty.targetType (),
						beanDefinition.name (),
						field.getName ()));

				return 1;

			}

			if (targetBeanDefinitions.size () > 1) {

				log.error (
					stringFormat (
						"Found %s ",
						targetBeanDefinitions.size (),
						"candidate beans of type %s ",
						injectedProperty.targetType (),
						"for %s.%s: ",
						beanDefinition.name (),
						field.getName (),
						"%s",
						joinWithSeparator (
							", ",
							targetBeanDefinitions.keySet ())));

				return 1;

			}

		}

		// register dependencies

		if (! injectedProperty.provider ()) {

			for (ComponentDefinition targetBeanDefinition
					: targetBeanDefinitions.values ()) {

				beanDefinition.orderedDependencies ().add (
					targetBeanDefinition.name ());

			}

		}

		// store injected target beans

		injectedProperty.targetBeanNames (
			new ArrayList<String> (
				targetBeanDefinitions.keySet ()));

		return 0;

	}

	private
	int initInjectedPropertyTargetByQualifier (
			ComponentDefinition beanDefinition,
			Annotation qualifier,
			InjectedProperty injectedProperty) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		// lookup target beans

		List<ComponentDefinition> targetBeanDefinitions =
			ifNull (
				injectedProperty.provider ()
					? ifNull (
						prototypeComponentDefinitionsByQualifier.get (qualifier),
						singletonComponentDefinitionsByQualifier.get (qualifier))
					: singletonComponentDefinitionsByQualifier.get (qualifier),
				Collections.<ComponentDefinition>emptyList ());

		if (injectedProperty.collectionType () == CollectionType.single) {

			if (targetBeanDefinitions.isEmpty ()) {

				log.error (
					stringFormat (
						"Unable to find bean of type %s for %s.%s",
						injectedProperty.targetType (),
						beanDefinition.name (),
						injectedProperty.fieldName ()));

				return 1;

			}

			if (targetBeanDefinitions.size () > 1) {

				log.error (
					stringFormat (
						"Found %s candidate beans of type %s for %s.%s",
						targetBeanDefinitions.size (),
						injectedProperty.targetType (),
						beanDefinition.name (),
						injectedProperty.fieldName ()));

				return 1;

			}

		}

		// register dependencies

		if (! injectedProperty.provider ()) {

			for (ComponentDefinition targetBeanDefinition
					: targetBeanDefinitions) {

				beanDefinition.orderedDependencies ().add (
					targetBeanDefinition.name ());

			}

		}

		// store injected target beans

		List<String> targetBeanDefinitionNames =
			new ArrayList<String> ();

		for (ComponentDefinition targetBeanDefinition
				: targetBeanDefinitions) {

			targetBeanDefinitionNames.add (
				targetBeanDefinition.name ());

		}

		injectedProperty.targetBeanNames (
			new ArrayList<String> (
				targetBeanDefinitionNames));

		return 0;

	}

	public
	void outputBeanDefinitions (
			String outputPath) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		try {

			FileUtils.deleteDirectory (
				new File (outputPath));

			FileUtils.forceMkdir (
				new File (outputPath));

		} catch (IOException exception) {

			log.warn (
				stringFormat (
					"Error deleting contents of %s",
					outputPath),
				exception);

		}

		for (
			ComponentDefinition beanDefinition
				: componentDefinitions
		) {

			String outputFile =
				stringFormat (
					"%s/%s.xml",
					outputPath,
					beanDefinition.name ());

			try {

				new DataToXml ().writeToFile (
					outputFile,
					beanDefinition);

			} catch (IOException exception) {

				log.warn (
					stringFormat (
						"Error writing %s",
						outputFile),
					exception);

			}

		}

	}

	public
	ApplicationContext registerUnmanagedSingleton (
			@NonNull String beanName,
			@NonNull Object object) {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		ComponentDefinition beanDefinition =
			new ComponentDefinition ()

			.name (
				beanName)

			.beanClass (
				object.getClass ())

			.scope (
				"singleton")

			.factoryClass (
				SingletonBeanFactory.class)

			.addValueProperty (
				"object",
				object)

			.owned (
				false);

		registerBeanDefinition (
			beanDefinition);

		if (
			componentMetaDatas.containsKey (
				object)
		) {
			throw new IllegalStateException ();
		}

		ComponentMetaData beanData =
			new ComponentMetaData ();

		beanData.definition =
			beanDefinition;

		beanData.state =
			BeanState.unmanaged;		

		componentMetaDatas.put (
			object,
			beanData);

		return this;

	}

	public
	ApplicationContext registerXmlClasspath (
			String classpath) {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		DataFromXml dataFromXml =
			new DataFromXml ()

			.registerBuilderClass (
				BeansSpec.class)

			.registerBuilderClass (
				BeansBeanSpec.class)

			.registerBuilderClass (
				BeansValuePropertySpec.class)

			.registerBuilderClass (
				BeansReferencePropertySpec.class)

			.registerBuilderClass (
				BeansPropertiesPropertySpec.class)

			.registerBuilderClass (
				BeansPropertyValueSpec.class);

		BeansSpec beans =
			(BeansSpec)
			dataFromXml.readClasspath (
				Collections.emptyList (),
				classpath);

		beans.register (
			this);

		return this;

	}

	public
	ApplicationContext registerXmlFilename (
			String filename) {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		DataFromXml dataFromXml =
			new DataFromXml ()

			.registerBuilderClass (
				BeansSpec.class)

			.registerBuilderClass (
				BeansBeanSpec.class)

			.registerBuilderClass (
				BeansValuePropertySpec.class)

			.registerBuilderClass (
				BeansReferencePropertySpec.class)

			.registerBuilderClass (
				BeansPropertiesPropertySpec.class)

			.registerBuilderClass (
				BeansPropertyValueSpec.class);

		BeansSpec beans =
			(BeansSpec)
			dataFromXml.readFilename (
				filename);

		beans.register (this);

		return this;

	}

	public
	ApplicationContext close () {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		// TODO

		return this;

	}

	public
	Provider<?> getBeanProvider (
			@NonNull ComponentDefinition beanDefinition) {

		return getBeanProvider (
			beanDefinition,
			true);

	}

	public
	Provider<?> getBeanProvider (
			final ComponentDefinition beanDefinition,
			final Boolean initialized) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		return new Provider <Object> () {

			@Override
			public
			Object get () {

				return getComponent (
					beanDefinition,
					initialized);

			}

		};

	}

	@SneakyThrows (Exception.class)
	public
	<BeanType>
	BeanType injectDependencies (
			BeanType bean) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		ComponentDefinition beanDefinition =
			new ComponentDefinition ()

			.name (
				bean.getClass ().getSimpleName ())

			.beanClass (
				bean.getClass ());

		int errors = 0;

		errors =
			initBeanDefinition (
				beanDefinition);

		if (errors > 0)
			throw new RuntimeException ();

		setBeanValueProperties (
			beanDefinition,
			bean);

		setBeanReferenceProperties (
			beanDefinition,
			bean);

		setBeanInjectedProperties (
			beanDefinition,
			bean);

		return bean;

	}

	public static
	class ComponentMetaData {
		ComponentDefinition definition;
		BeanState state;
	}

	public static
	enum BeanState {
		uninitialized,
		active,
		tornDown,
		error,
		unmanaged;
	}

}
