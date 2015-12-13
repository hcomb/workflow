package hcomb.eu.workflow.engine;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AsyncExecutor implements Executor {
	private Executor executor = Executors.newSingleThreadExecutor();
	
	public void execute(Runnable task) {
		executor.execute(task);
	}
}
