package psb;


public class ArrayExtension {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T[] union(T[] array1, T[] array2){
		Class type = array1.getClass();
		T[] array =
			(T[])java.lang.reflect.Array.newInstance(type.getComponentType(),
				array1.length + array2.length);
		System.arraycopy(array1, 0, array, 0, array1.length);
		System.arraycopy(array2, 0, array, array1.length, array2.length);
		return array;
	}
}
