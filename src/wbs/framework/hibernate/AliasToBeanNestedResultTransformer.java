package wbs.framework.hibernate;

import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import org.hibernate.HibernateException;
import org.hibernate.property.access.internal.PropertyAccessStrategyBasicImpl;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.transform.AliasedTupleSubsetResultTransformer;
import org.hibernate.transform.ResultTransformer;

public
class AliasToBeanNestedResultTransformer
	extends AliasedTupleSubsetResultTransformer {

	// adapted from
	// https://github.com/samiandoni/AliasToBeanNestedResultTransformer

	private final
	Class<?> resultClass;

	@Override
	public
	boolean isTransformedValueATupleElement (
			String[] aliases,
			int tupleLength) {

		return false;

	}

	public
	AliasToBeanNestedResultTransformer (
			Class<?> resultClass) {

		this.resultClass =
			resultClass;

	}

	@SuppressWarnings ("unchecked")
	@Override
	public
	Object transformTuple (
			Object[] tuple,
			String[] aliases) {

		Map<Class<?>,List<?>> subclassToAlias =
			new HashMap<Class<?>,List<?>> ();

		List<String> nestedAliases =
			new ArrayList<String> ();

		try {

			for (
				int index = 0;
				index < aliases.length;
				index ++
			) {

				String alias =
					aliases [index];

				if (alias.contains (".")) {

					nestedAliases.add (alias);

					String[] sp = alias.split ("\\.");

					String fieldName = sp [0];
					String aliasName = sp [1];

					Class<?> subclass =
						resultClass.getDeclaredField (fieldName).getType ();

					if (! subclassToAlias.containsKey (subclass)) {

						subclassToAlias.put (
							subclass,
							ImmutableList.of (
								new ArrayList <Object> (),
								new ArrayList <String> (),
								fieldName));

					}

					(
						(List<Object>)
						subclassToAlias.get (subclass).get (0)
					).add (
						tuple [index]);

					(
						(List<String>)
						subclassToAlias.get (subclass).get (1)
					).add (
						aliasName);

				}

			}

		} catch (NoSuchFieldException exception) {

			throw new HibernateException (
				stringFormat (
					"Could not instantiate result class: %s, ",
					resultClass.getName (),
					"due to inexistant field: %s",
					exception.getMessage ()),
				exception);

		}

		Object[] newTuple =
			new Object [
				+ aliases.length
				- nestedAliases.size ()];

		String[] newAliases =
			new String [
				+ aliases.length
				- nestedAliases.size ()];

		int outerIndex = 0;

		for (
			int innerIndex = 0;
			innerIndex < aliases.length;
			innerIndex ++
		) {

			if (
				doesNotContain (
					nestedAliases,
					aliases [innerIndex])
			) {

				newTuple [outerIndex] =
					tuple [innerIndex];

				newAliases [outerIndex] =
					aliases [innerIndex];

				outerIndex ++;

			}

		}

		ResultTransformer rootTransformer =
			new AliasToBeanResultTransformer (
				resultClass);

		Object root =
			rootTransformer.transformTuple (
				newTuple,
				newAliases);

		for (
			Class<?> subclass
				: subclassToAlias.keySet ()
		) {

			ResultTransformer subclassTransformer =
				new AliasToBeanResultTransformer (
					subclass);

			Object subObject =
				subclassTransformer.transformTuple (
					(
						(List<Object>)
						subclassToAlias.get (subclass).get (0)
					).toArray (),
					(
						(List<Object>)
						subclassToAlias.get (subclass).get (1)
					).toArray (new String [0]));

			PropertyAccess accessStrategy =
				PropertyAccessStrategyBasicImpl.INSTANCE.buildPropertyAccess (
					resultClass,
					(String) subclassToAlias.get (subclass).get (2));

			accessStrategy.getSetter ().set (
				root,
				subObject,
				null);

		}

		return root;

	}

}
