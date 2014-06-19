package corp.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import corp.data.Gazetteer;

public class StringUtil {
	/* String cleaning and measure helpers... maybe move these to separate classes later. */

	public interface StringTransform {
		String transform(String str);
		String toString(); // Return constant name for this transformation (used in feature names)
	}

	public interface StringPairMeasure {
		double compute(String str1, String str2);
	}

	public interface StringCollectionTransform {
		Collection<String> transform(String str);
		String toString();
	}

	public static StringUtil.StringTransform getDefaultCleanFn() {
		return new StringUtil.StringTransform() {
			public String toString() {
				return "DefaultCleanFn";
			}

			public String transform(String str) {
				return StringUtil.clean(str);
			}
		};	
	}

	public static StringUtil.StringTransform getStopWordsCleanFn(final Gazetteer stopWords) {
		final String stopWordsName = stopWords.getName();

		return new StringUtil.StringTransform() {
			public String toString() {
				return "StopWordsCleanFn_" + stopWordsName;
			}

			public String transform(String str) {
				str = StringUtil.clean(str);
				String stoppedStr = stopWords.removeTerms(str);
				if (stoppedStr.length() > 0)
					return stoppedStr;
				else 
					return str;
			}
		};
	}


	public static StringUtil.StringCollectionTransform getPrefixesFn() {
		return new StringUtil.StringCollectionTransform() {
			public String toString() {
				return "Prefixes";
			}

			public Collection<String> transform(String str) {
				return StringUtil.prefixes(str);
			}
		};	
	}

	/**
	 * @param str1
	 * @param str2
	 * @return The number of case-insensitive tokens (split on white-space) shared at
	 *  the start of str1 and str2 if str1 is a prefix of str2 or str2 is a prefix of
	 *  str1.  If neither is a prefix, then return 0.
	 */
	public static int prefixTokenOverlap(String str1, String str2) {
		String[] str1Tokens = str1.trim().split("\\s+");
		String[] str2Tokens = str2.trim().split("\\s+");

		for (int i = 0; i < str1Tokens.length && i < str2Tokens.length; i++) {
			if (!str1Tokens[i].equalsIgnoreCase(str2Tokens[i]))
				return 0;
		}
		return Math.min(str1Tokens.length, str2Tokens.length);
	}

	public static boolean isInitialism(String initialism, String str, boolean allowPrefix) {
		String[] strTokens = str.trim().toLowerCase().split("\\s+");
		String cleanInitialism = initialism.trim().toLowerCase();

		if (!allowPrefix && strTokens.length != cleanInitialism.length())
			return false;

		if (cleanInitialism.length() > strTokens.length)
			return false;

		for (int i = 0; i < cleanInitialism.length(); i++) {
			if (cleanInitialism.charAt(i) != strTokens[i].charAt(0))
				return false;
		}

		return true;
	}

	public static int levenshteinDistance(String str1, String str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++)
			distance[i][0] = i;
		for (int j = 1; j <= str2.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= str1.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = 
					Math.min(
						Math.min(distance[i - 1][j] + 1, distance[i][j - 1] + 1),
						distance[i - 1][j - 1]+((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1)
					);

		return distance[str1.length()][str2.length()];
	}

	// FIXME: This function is messy and inefficient.
	public static String clean(String str) {
		StringBuilder cleanStrBuilder = new StringBuilder();

		str = str.trim();
		if (str.equals("$") || str.equals("&") || str.equals("+") || str.equals("@"))
			return str;

		// Remove words with slashes
		String[] tokens = str.split("\\s+");
		for (int i = 0; i < tokens.length; i++) {
			if (!tokens[i].startsWith("/") && !tokens[i].startsWith("\\") && !tokens[i].startsWith("-"))
				cleanStrBuilder.append(tokens[i]).append(" ");
		}

		// Remove non alpha-numeric characters
		String cleanStr = cleanStrBuilder.toString();
		cleanStr = cleanStr.toLowerCase()
						   .replaceAll("[\\W&&[^\\s]]+", "")
						   .replaceAll("\\s+", " ");

		// Remove single character words
		/*cleanStrBuilder = new StringBuilder();
		tokens = cleanStr.split(" ");
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].length() > 1)
				cleanStrBuilder.append(tokens[i]).append(" ");
		}
		return cleanStrBuilder.toString().trim();*/
		return cleanStr.trim();
	}

	public static Collection<String> prefixes(String str) {
		List<String> prefixes = new ArrayList<String>();
		for (int i = 1; i <= str.length(); i++) {
			prefixes.add(str.substring(0, i));
		}
		return prefixes;
	}
}