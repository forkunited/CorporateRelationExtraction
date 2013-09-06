package corp.util;

public class StringUtil {
	public interface StringPairMeasure {
		double compute(String str1, String str2);
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
	
	public static boolean isInitialism(String initialism, String str) {
		String[] strTokens = str.trim().toLowerCase().split("\\s+");
		String cleanInitialism = initialism.trim().toLowerCase();
		if (strTokens.length != cleanInitialism.length())
			return false;
		
		for (int i = 0; i < strTokens.length; i++) {
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
	
	public static String clean(String str) {
		return str.trim().toLowerCase().replaceAll("[\\W&&[^\\s]]+", "").replaceAll("\\s+", " ");
	}
}
