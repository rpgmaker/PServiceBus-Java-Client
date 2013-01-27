package psb;


public final class LocalStorage {
	private static ILocalStorage storage;
	private static Object context;
	private static ClassLoader classLoader =
			LocalStorage.class.getClassLoader();
	
	public static void setContext(Object context){
		LocalStorage.context = context;
	}
	
	public static Object getContext(){
		return LocalStorage.context;
	}
	
	public static Boolean getIsAndroid(){
		try{
			classLoader.loadClass("android.app.Activity");
			return true;
		}
		catch(Exception ex){
			return false;
		}
	}
	
	public static ILocalStorage getInstance(){
		if(storage == null){
			storage = getIsAndroid() ? new AndroidLocalStorage() :
				new JavaLocalStorage();
		}
		return storage;
	}
}
