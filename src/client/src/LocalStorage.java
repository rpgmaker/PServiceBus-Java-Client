
public final class LocalStorage {
	private static ILocalStorage storage;
	private static ClassLoader classLoader =
			LocalStorage.class.getClassLoader();
	
	private static Boolean getIsAndroid(){
		try{
			classLoader.loadClass("android.app.Activity");
			return true;
		}
		catch(Exception ex){
			ex.addSuppressed(ex);
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
