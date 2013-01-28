package psb;

import java.util.concurrent.ThreadFactory;

public class StreamThreadFactory implements ThreadFactory {

	@Override
	public Thread newThread(Runnable runnable) {
		Thread t = new Thread(runnable);
		t.setDaemon(true);
		return t;
	}

}
