
public class StringExtension {
	public final static String empty = "";
	public static Boolean isNullOrEmpty(String str){
		return str == null || str.equals(empty);
	}

	public static String trimEnd(String value, String... trimChars){
		return value.replaceAll("[" + join("", trimChars) + "]+$", empty);
	}

	public static String join(String delimeter, String[] array) {
		if (array == null) return null;
		else return join(delimeter, array, 0, array.length);
	}

	public static String join(String delimeter, String[] array, int startIndex, int count){
		StringBuilder sb = new StringBuilder();
		if (array == null) return null;
		for (int i = startIndex; i < array.length && i - startIndex < count; i++) {
			if (delimeter != null && i > startIndex) sb.append(delimeter);
			if (array[i] != null) sb.append(array[i]);
		}
		return sb.toString();
	}
	
	public static String format(String format, Object... args){
		for(int i = 0; i < args.length; i++)
			format = format.replace("{" + i + "}", args[i].toString());
		return format;
	}
}
