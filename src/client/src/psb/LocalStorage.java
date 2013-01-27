package psb;

public final class LocalStorage {
	private static ILocalStorage storage;
	
	public static ILocalStorage getInstance(){
		if(storage == null){
			storage = PSBContext.getIsAndroid() ? new AndroidLocalStorage() :
				new JavaLocalStorage();
		}
		return storage;
	}
}
