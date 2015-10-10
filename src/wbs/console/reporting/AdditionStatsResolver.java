package wbs.console.reporting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.Instant;

import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("additionStatsResolver")
public
class AdditionStatsResolver
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

			for (Instant step : period.steps ()) {

				Pair<Object,Instant> key =
					Pair.<Object,Instant>of (
						group,
						step);

				int totalValue = 0;

				for (
					int operandIndex = 0;
					operandIndex < operands.size ();
					operandIndex ++
				) {

					Operand operand =
						operands.get (operandIndex);

					int operandValue =
						operand.coefficient;

					if (operand.resolver != null) {

						ResolvedStats operandResolved =
							operandsResolved.get (operandIndex);

						Integer resolverValue =
							(Integer)
							operandResolved.steps ().get (key);

						if (resolverValue == null)
							continue;

						operandValue *=
							resolverValue;

					}

					totalValue +=
						operandValue;

				}

				ret.steps ().put (
					key,
					totalValue);

			}

			{

				int totalValue = 0;

				for (
					int operandIndex = 0;
					operandIndex < operands.size ();
					operandIndex ++
				) {

					Operand operand =
						operands.get (operandIndex);

					int operandValue =
						operand.coefficient;

					if (operand.resolver != null) {

						ResolvedStats operandResolved =
							operandsResolved.get (operandIndex);

						Integer resolverValue =
							(Integer)
							operandResolved.totals ().get (group);

						if (resolverValue == null)
							continue;

						operandValue *=
							resolverValue;

					}

					totalValue +=
						operandValue;

				}

				ret.totals ().put (
					group,
					totalValue);

			}
		}

		return ret;

	}

	@Accessors (fluent = true)
	@Data
	public static
	class Operand {
		int coefficient;
		StatsResolver resolver;
	}

}
