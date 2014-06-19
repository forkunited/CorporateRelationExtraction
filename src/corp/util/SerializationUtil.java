package corp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.stanford.nlp.util.Pair;

public class SerializationUtil {
	public static Map<String, String> deserializeArguments(String argumentsStr) {
		String[] argumentStrs = argumentsStr.split(",");
		Map<String, String> arguments = new HashMap<String, String>();
		for (String argumentStr : argumentStrs) {
			Pair<String, String> assignment = SerializationUtil.deserializeAssignment(argumentStr);
			if (assignment == null)
				continue;
			arguments.put(assignment.first(), assignment.second());
		}
		return arguments;
	}

	public static List<String> deserializeList(String listStr) {
		String[] valueStrs = listStr.split(",");
		List<String> values = new ArrayList<String>();
		for (int i = 0; i < valueStrs.length; i++)
			values.add(valueStrs[i].trim());
		return values;
	}

	public static Pair<String, String> deserializeAssignment(String assignmentStr) {
		int equalsIndex = assignmentStr.indexOf("=");
		if (!(equalsIndex >= 0 && equalsIndex < assignmentStr.length()))
			return null;

		String first = assignmentStr.substring(0, equalsIndex).trim();
		String second = null;
		if (equalsIndex == assignmentStr.length() - 1)
			second = "";
		else
			second = assignmentStr.substring(equalsIndex + 1).trim();

		/*if (!first.matches("[A-Za-z0-9]*"))
			return null;*/

		return new Pair<String, String>(first,second);
	}

	public static <T> String serializeArguments(Map<String, T> arguments) {
		StringBuilder str = new StringBuilder();
		for (Entry<String, T> argument : arguments.entrySet()) {
			str.append(argument.getKey() + "=" + argument.getValue() + ",");
		}
		if (arguments.size() > 0)
			str = str.delete(str.length()-1, str.length());

		return str.toString();
	}

	public static <T> String serializeList(List<T> list) {
		StringBuilder str = new StringBuilder();

		for (T item : list) {
			str.append(item).append(",");
		}
		if (list.size() > 0)
			str.delete(str.length() - 1, str.length());

		return str.toString();
	}

	public static <T> String serializeAssignment(Pair<String, T> assignment) {
		return assignment.first() + "=" + assignment.second();
	}
}