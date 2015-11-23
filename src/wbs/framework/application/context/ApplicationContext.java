package wbs.framework.application.context;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.nullIfEmptyString;
import static wbs.framework.utils.etc.Misc.stringFormat;

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

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.application.annotations.SingletonDependency;
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

	List<BeanDefinition> beanDefinitions =
		new ArrayList<BeanDefinition> ();

	Map<String,BeanDefinition> beanDefinitionsByName =
		new HashMap<String,BeanDefinition> ();

	Map<Class<?>,Map<String,BeanDefinition>> beanDefinitionsByClass =
		new HashMap<Class<?>,Map<String,BeanDefinition>> ();

	Map<Class<?>,Map<String,BeanDefinition>> singletonBeanDefinitionsByClass =
		new HashMap<Class<?>,Map<String,BeanDefinition>> ();

	Map<Class<?>,Map<String,BeanDefinition>> prototypeBeanDefinitionsByClass =
		new HashMap<Class<?>,Map<String,BeanDefinition>> ();

	Map<Annotation,List<BeanDefinition>> beanDefinitionsByQualifier =
		new HashMap<Annotation,List<BeanDefinition>> ();

	Map<Annotation,List<BeanDefinition>> singletonBeanDefinitionsByQualifier =
		new HashMap<Annotation,List<BeanDefinition>> ();

	Map<Annotation,List<BeanDefinition>> prototypeBeanDefinitionsByQualifier =
		new HashMap<Annotation,List<BeanDefinition>> ();

	Map<String,Object> singletonBeans =
		new HashMap<String,Object> ();

	Set<String> singletonBeansInCreation =
		new LinkedHashSet<String> ();

	Set<String> singletonBeansFailed =
		new HashSet<String> ();

	// TODO not pretty
	@Getter
	List<String> requestBeanNames =
		new ArrayList<String> ();

	EasyReadWriteLock lock =
		EasyReadWriteLock.instantiate ();

	public
	<BeanType>
	BeanType getBean (
			String beanName,
			Class<BeanType> beanClass) {

		@Cleanup
		HeldLock heldLock =
			lock.read ();

		BeanDefinition beanDefinition =
			beanDefinitionsByName.get (beanName);

		if (beanDefinition == null) {

			throw new NoSuchBeanException (
				stringFormat (
					"Bean definition with name %s does not exist",
					beanName));

		}

		return beanClass.cast (
			getBean (beanDefinition));

	}

	public
	<BeanType>
	Provider<BeanType> getBeanProvider (
			String beanName,
			Class<BeanType> beanClass) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		BeanDefinition beanDefinition =
			beanDefinitionsByName.get (beanName);

		if (beanDefinition == null) {

			throw new NoSuchBeanException (
				stringFormat (
					"Bean definition with name %s does not exist",
					beanName));

		}

		if (
			! beanClass.isAssignableFrom (
				beanDefinition.beanClass ())
		) {

			throw new NoSuchBeanException (
				stringFormat (
					"Bean definition with name %s is of type %s instead of %s",
					beanName,
					beanDefinition.beanClass ().getName (),
					beanClass.getName ()));

		}

		@SuppressWarnings ("unchecked")
		Provider<BeanType> beanProvider =
			(Provider<BeanType>)
			getBeanProvider (
				beanDefinition);

		return beanProvider;

	}

	public
	Map<String,Object> getAllSingletonBeans () {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		Map<String,Object> map =
			new HashMap<String,Object> ();

		for (BeanDefinition beanDefinition
				: beanDefinitions) {

			if (! equal (
					beanDefinition.scope (),
					"singleton"))
				continue;

			map.put (
				beanDefinition.name (),
				getBean (beanDefinition));

		}

		return map;

	}

	public
	List<BeanDefinition> getBeanDefinitionsWithAnnotation (
			Class<? extends Annotation> annotationClass) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		ImmutableList.Builder<BeanDefinition>
			beanDefinitionsWithAnnotationBuilder =
				ImmutableList.<BeanDefinition>builder ();

		for (
			BeanDefinition beanDefinition
				: beanDefinitions
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
	Object getBean (
			BeanDefinition beanDefinition) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		if (equal (
				beanDefinition.scope (),
				"prototype")) {

			return instantiateBean (
				beanDefinition);

		} else if (
			equal (
				beanDefinition.scope (),
				"singleton")
		) {

			Object bean =
				singletonBeans.get (
					beanDefinition.name ());

			if (bean != null)
				return bean;

			if (
				singletonBeansInCreation.contains (
					beanDefinition.name ())
			) {

				throw new RuntimeException (
					stringFormat (
						"Singleton bean %s already in creation (%s)",
						beanDefinition.name (),
						joinWithSeparator (
							", ",
							singletonBeansInCreation)));

			}

			if (
				singletonBeansFailed.contains (
					beanDefinition.name ())
			) {

				throw new RuntimeException (
					stringFormat (
						"Singleton bean %s already failed",
						beanDefinition.name ()));

			}

			singletonBeansInCreation.add (
				beanDefinition.name ());

			try {

				bean =
					instantiateBean (beanDefinition);

				singletonBeans.put (
					beanDefinition.name (),
					bean);

			} finally {

				singletonBeansInCreation.remove (
					beanDefinition.name ());

				if (bean == null)
					singletonBeansFailed.add (
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
			BeanDefinition beanDefinition) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		log.debug (
			stringFormat (
				"Instantiating %s (%s)",
				beanDefinition.name (),
				beanDefinition.scope ()));

		// instantiate

		Class<?> instantiateClass =
			ifNull (
				beanDefinition.factoryClass (),
				beanDefinition.beanClass ());

		Object bean =
			instantiateClass.newInstance ();

		// set properties

		setBeanValueProperties (
			beanDefinition,
			bean);

		setBeanReferenceProperties (
			beanDefinition,
			bean);

		setBeanInjectedProperties (
			beanDefinition,
			bean);

		// call factory

		if (beanDefinition.factoryClass () != null) {

			BeanFactory beanFactory =
				(BeanFactory) bean;

			bean =
				beanFactory.instantiate ();

			if (bean == null) {

				throw new RuntimeException (
					stringFormat (
						"Factory bean returned null for %s",
						beanDefinition.name ()));

			}

		}

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

		// and finish

		log.debug (
			stringFormat (
				"Bean %s instantiated successfully",
				beanDefinition.name ()));

		return bean;

	}

	private
	void setBeanValueProperties (
			BeanDefinition beanDefinition,
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
			BeanDefinition beanDefinition,
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
				getBean (
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
			BeanDefinition beanDefinition,
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

			List<Pair<BeanDefinition,Object>> targetBeans =
				new ArrayList<Pair<BeanDefinition,Object>> ();

			for (
				String targetBeanDefinitionName
					: injectedProperty.targetBeanNames ()
			) {

				BeanDefinition targetBeanDefinition =
					beanDefinitionsByName.get (
						targetBeanDefinitionName);

				Object injectValue;

				if (injectedProperty.provider ()) {

					injectValue =
						getBeanProvider (
							targetBeanDefinition);

				} else {

					injectValue =
						getBean (
							targetBeanDefinition);

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
					Pair<BeanDefinition,Object> pair
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
					Pair<BeanDefinition,Object> pair
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

				for (Pair<BeanDefinition,Object> pair
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
			BeanDefinition beanDefinition) {

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

		if (beanDefinitionsByName.containsKey (
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
					"Bean definition %s refers to abstract class %s",
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

		beanDefinitions.add (
			beanDefinition);

		beanDefinitionsByName.put (
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
				beanDefinitionsByClass,
				beanClasses,
				beanDefinition);

			if (
				equal (
					beanDefinition.scope (),
					"singleton")
			) {

				updateIndexByClass (
					singletonBeanDefinitionsByClass,
					beanClasses,
					beanDefinition);

			}

			if (
				equal (
					beanDefinition.scope (),
					"prototype")
			) {

				updateIndexByClass (
					prototypeBeanDefinitionsByClass,
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
					beanDefinitionsByQualifier,
					annotation,
					beanDefinition);

				if (
					equal (
						beanDefinition.scope (),
						"singleton")
				) {

					updateIndexByQualifier (
						singletonBeanDefinitionsByQualifier,
						annotation,
						beanDefinition);

				}

				if (
					equal (
						beanDefinition.scope (),
						"prototype")
				) {

					updateIndexByQualifier (
						prototypeBeanDefinitionsByQualifier,
						annotation,
						beanDefinition);

				}

			}

		}

		return this;

	}

	private
	void updateIndexByClass (
			Map<Class<?>,Map<String,BeanDefinition>> index,
			Set<Class<?>> beanClasses,
			BeanDefinition beanDefinition) {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		for (
			Class<?> beanClass
				: beanClasses
		) {

			Map<String,BeanDefinition> beanDefinitionsForClass =
				index.get (
					beanClass);

			if (beanDefinitionsForClass == null) {

				index.put (
					beanClass,
					beanDefinitionsForClass =
						new HashMap<String,BeanDefinition> ());

			}

			beanDefinitionsForClass.put (
				beanDefinition.name (),
				beanDefinition);

		}

	}

	private
	void updateIndexByQualifier (
			Map<Annotation,List<BeanDefinition>> index,
			Annotation annotation,
			BeanDefinition beanDefinition) {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		List<BeanDefinition> beanDefinitionsForQualifier =
			index.get (annotation);

		if (beanDefinitionsForQualifier == null) {

			index.put (
				annotation,
				beanDefinitionsForQualifier =
					new ArrayList<BeanDefinition> ());

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

		registerSingleton (
			"applicationContext",
			this);

		// work out dependencies

		for (
			BeanDefinition beanDefinition
				: beanDefinitions
		) {

			errors +=
				initBeanDefinition (
					beanDefinition);

		}

		// check dependencies exist

		for (
			BeanDefinition beanDefinition
				: beanDefinitions
		) {

			for (
				String dependency
					: beanDefinition.orderedDependencies ()
			) {

				if (
					! beanDefinitionsByName.containsKey (
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

		List<BeanDefinition> unorderedBeanDefinitions =
			new ArrayList<BeanDefinition> (
				beanDefinitions);

		Map<String,BeanDefinition> orderedBeanDefinitions =
			new LinkedHashMap<String,BeanDefinition> ();

		while (! unorderedBeanDefinitions.isEmpty ()) {

			boolean madeProgress = false;

			ListIterator<BeanDefinition> unorderedBeanDefinitionIterator =
				unorderedBeanDefinitions.listIterator ();

			OUTER: while (
				unorderedBeanDefinitionIterator.hasNext ()
			) {

				BeanDefinition beanDefinition =
					unorderedBeanDefinitionIterator.next ();

				for (
					String targetBeanDefinitionName
						: beanDefinition.orderedDependencies ()
				) {

					if (! beanDefinitionsByName.containsKey (
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
					BeanDefinition beanDefinition
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
			BeanDefinition beanDefinition
				: beanDefinitions
		) {

			if (
				! equal (
					beanDefinition.scope (),
					"singleton")
			) {
				continue;
			}

			getBean (
				beanDefinition.name (),
				Object.class);

		}

		return this;

	}

	public
	int initBeanDefinition (
			BeanDefinition beanDefinition) {

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

			if (

				isNull (
					injectAnnotation)

				&& isNull (
					prototypeDependencyAnnotation)

				&& isNull (
					singletonDependencyAnnotation)

			) {
				continue;
			}

			Named namedAnnotation =
				field.getAnnotation (
					Named.class);

			if (namedAnnotation != null) {

				errors +=
					initInjectedFieldByName (
						beanDefinition,
						namedAnnotation,
						field);

			} else {

				List<Annotation> qualifierAnnotations =
					new ArrayList<Annotation> ();

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
						.beanDefinition (beanDefinition)
						.fieldDeclaringClass (field.getDeclaringClass ())
						.fieldName (field.getName ());

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
			BeanDefinition beanDefinition,
			Named namedAnnotation,
			Field field) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		// TODO merge this

		String targetBeanDefinitionName =
			ifNull (
				nullIfEmptyString (namedAnnotation.value ()),
				field.getName ());

		BeanDefinition targetBeanDefinition =
			beanDefinitionsByName.get (
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

			.targetBeanNames (
				Collections.singletonList (
					targetBeanDefinitionName)));

		return 0;

	}

	private
	int initInjectedPropertyField (
			BeanDefinition beanDefinition,
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
			BeanDefinition beanDefinition,
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

		Map<String,BeanDefinition> targetBeanDefinitions =
			ifNull (
				injectedProperty.provider ()
					? ifNull (
						prototypeBeanDefinitionsByClass.get (targetClass),
						singletonBeanDefinitionsByClass.get (targetClass))
					: singletonBeanDefinitionsByClass.get (targetClass),
				Collections.<String,BeanDefinition>emptyMap ());

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

			for (BeanDefinition targetBeanDefinition
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
			BeanDefinition beanDefinition,
			Annotation qualifier,
			InjectedProperty injectedProperty) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		// lookup target beans

		List<BeanDefinition> targetBeanDefinitions =
			ifNull (
				injectedProperty.provider ()
					? ifNull (
						prototypeBeanDefinitionsByQualifier.get (qualifier),
						singletonBeanDefinitionsByQualifier.get (qualifier))
					: singletonBeanDefinitionsByQualifier.get (qualifier),
				Collections.<BeanDefinition>emptyList ());

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

			for (BeanDefinition targetBeanDefinition
					: targetBeanDefinitions) {

				beanDefinition.orderedDependencies ().add (
					targetBeanDefinition.name ());

			}

		}

		// store injected target beans

		List<String> targetBeanDefinitionNames =
			new ArrayList<String> ();

		for (BeanDefinition targetBeanDefinition
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

		for (BeanDefinition beanDefinition
				: beanDefinitions) {

			String outputFile =
				stringFormat (
					"%s/%s.xml",
					outputPath,
					beanDefinition.name ());

			try {

				new DataToXml ()
					.object (beanDefinition)
					.write (outputFile);

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
	ApplicationContext registerSingleton (
			String beanName,
			Object object) {

		@Cleanup
		HeldLock heldlock =
			lock.write ();

		registerBeanDefinition (
			new BeanDefinition ()

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
				object));

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
			final BeanDefinition beanDefinition) {

		@Cleanup
		HeldLock heldlock =
			lock.read ();

		return new Provider<Object> () {

			@Override
			public
			Object get () {
				return getBean (beanDefinition);
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

		BeanDefinition beanDefinition =
			new BeanDefinition ()

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

}
