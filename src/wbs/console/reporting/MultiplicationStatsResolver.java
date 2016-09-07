package wbs.console.reporting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.Instant;

import wbs.framework.component.annotations.PrototypeComponent;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@PrototypeComponent ("multiplicationStatsResolver")
public
class MultiplicationStatsResolver
	implements StatsResolver {

	@Getter @Setter
	List<Operand> operands =
		new ArrayList<Operand> ();

	@Override
	public
	Set<Object> getGroups (
			Map<String,StatsDataSet> dataSetsByName,
			StatsGrouper grouper) {

		Set<Object> groups =
			new HashSet<Object> ();

		for (Operand operand
				: operands) {

			if (operand.resolver () == null)
				continue;

			groups.addAll (
				operand.resolver.getGroups (
					dataSetsByName,
					grouper));

		}

		return groups;

	}

	@Override
	public
	ResolvedStats resolve (
			Map<String,StatsDataSet> dataSetsByName,
			StatsPeriod period,
			Set<Object> groups) {

		List<ResolvedStats> operandsResolved =
			new ArrayList<ResolvedStats> (
				operands.size ());

		for (
			int operandIndex = 0;
			operandIndex < operands.size ();
			operandIndex ++
		) {

			Operand operand =
				operands.get (operandIndex);

			if (operand.resolver == null) {
				operandsResolved.add (null);
				continue;
			}

			operandsResolved.add (
				operand.resolver.resolve (
					dataSetsByName,
					period,
					groups));

		}

		ResolvedStats ret =
			new ResolvedStats ();

		for (Object group : groups) {

			OUTER:
			for (
				Instant step
					: period.steps ()
			) {

				Pair <Object, Instant> key =
					Pair.of (
						group,
						step);

				long numerator = 1;
				long denominator = 1;

				for (
					int operandIndex = 0;
					operandIndex < operands.size ();
					operandIndex ++
				) {

					Operand operand =
						operands.get (operandIndex);

					long value =
						operand.value;

					if (operand.resolver != null) {

						ResolvedStats operandResolved =
							operandsResolved.get (
								operandIndex);

						Long resolverValue =
							(Long)
							operandResolved.steps ().get (
								key);

						if (resolverValue == null)
							continue OUTER;

						if (resolverValue == 0)
							continue OUTER;

						value *=
							resolverValue;

					}

					for (long i = 0; i < operand.power; i++)
						numerator *= value;

					for (long i = operand.power; i < 0; i ++)
						denominator *= value;

				}

				ret.steps ().put (
					key,
					numerator / denominator);

			}

			{

				long numerator = 1;
				long denominator = 1;

				for (
					int operandIndex = 0;
					operandIndex < operands.size ();
					operandIndex ++
				) {

					Operand operand =
						operands.get (
							operandIndex);

					long value =
						operand.value;

					if (operand.resolver != null) {

						ResolvedStats operandResolved =
							operandsResolved.get (
								operandIndex);

						Long resolverValue =
							(Long)
							operandResolved.totals ().get (
								group);

						if (resolverValue == null)
							break;

						if (resolverValue == 0l)
							break;

						value *=
							resolverValue;

					}

					for (long i = 0; i < operand.power; i++)
						numerator *= value;

					for (long i = operand.power; i < 0; i ++)
						denominator *= value;

				}

				ret.totals ().put (
					group,
					numerator / denominator);

			}

		}

		return ret;

	}

	@Accessors (fluent = true)
	@Data
	public static
	class Operand {
		long power = 1l;
		long value = 1l;
		StatsResolver resolver;
	}

}
