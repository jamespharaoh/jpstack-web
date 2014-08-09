package wbs.platform.reporting.console;

import java.util.ArrayList;
import java.util.HashMap;
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
	Map<Pair<Object,Instant>,Object> resolve (
			Map<String,StatsDataSet> dataSetsByName,
			StatsPeriod period,
			Set<Object> groups) {

		List<Map<Pair<Object,Instant>,Object>> operandDatas =
			new ArrayList<Map<Pair<Object,Instant>,Object>> (
				operands.size ());

		for (
			int operandIndex = 0;
			operandIndex < operands.size ();
			operandIndex ++
		) {

			Operand operand =
				operands.get (operandIndex);

			if (operand.resolver == null) {
				operandDatas.add (null);
				continue;
			}

			Map<Pair<Object,Instant>,Object> operandData =
				operand.resolver.resolve (
					dataSetsByName,
					period,
					groups);

			operandDatas.add (
				operandData);

		}

		Map<Pair<Object,Instant>,Object> ret =
			new HashMap<Pair<Object,Instant>,Object> ();

		for (Object group : groups) {

			OUTER:
			for (Instant step : period.steps ()) {

				Pair<Object,Instant> key =
					Pair.<Object,Instant>of (
						group,
						step);

				int numerator = 1;
				int denominator = 1;

				for (
					int operandIndex = 0;
					operandIndex < operands.size ();
					operandIndex ++
				) {

					Operand operand =
						operands.get (operandIndex);

					int value =
						operand.value;

					if (operand.resolver != null) {

						Map<Pair<Object,Instant>,Object> operandData =
							operandDatas.get (operandIndex);

						Integer resolverValue =
							(Integer)
							operandData.get (key);

						if (resolverValue == null)
							continue OUTER;

						if (resolverValue == 0)
							continue OUTER;

						value *=
							resolverValue;

					}

					for (int i = 0; i < operand.power; i++)
						numerator *= value;

					for (int i = operand.power; i < 0; i ++)
						denominator *= value;

				}

				ret.put (
					key,
					numerator / denominator);

			}

		}

		return ret;

	}

	@Accessors (fluent = true)
	@Data
	public static
	class Operand {
		int power = 1;
		int value = 1;
		StatsResolver resolver;
	}

}
