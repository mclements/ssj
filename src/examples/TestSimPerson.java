import umontreal.ssj.stat.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
class TestSimPerson {
	public static void main2 (String[] args) {
		Tally lifetime = new Tally();
		// omp parallel for public(lifetime)
		for (int j=0; j<2; ++j) {
			SimPerson person = new SimPerson();
			synchronized(person) {
			for (int id=0; id<1000000; ++id) {
				person.run();
				}
			}
			// omp critical
			{
				lifetime.add(person.lifetime);
			}
		}
		System.out.println(lifetime.report());
		System.out.println(lifetime.formatCINormal(0.95));
		System.out.println("Expected life-time: "+new SimPerson().expectedLifetime());
	}
	public static void main (String[] args) throws InterruptedException {
		int NTHREADS = 2; // Runtime.getRuntime().availableProcessors();
		ExecutorService es = Executors.newFixedThreadPool(NTHREADS);
		class Context {
			public Tally local_lifetime;
		}
		Tally lifetime = new Tally();
		final Context context = new Context();
		context.local_lifetime = lifetime;
		for (int j=0; j<NTHREADS; ++j) {
			es.execute(new Runnable() {
					public void run() {
						SimPerson person = new SimPerson();
						synchronized(person.s) {
							for (int id=0; id<1000000; ++id) {
								person.run();
							}
						}
						synchronized(context) {
							context.local_lifetime.add(person.lifetime);
						}
					}
				});
		}
		es.shutdownNow();
		if (!es.awaitTermination(10, TimeUnit.SECONDS)) {
			System.out.println("Still waiting...");
			System.exit(0);
		}
		System.out.println(lifetime.report());
		System.out.println(lifetime.formatCINormal(0.95));
		System.out.println("Expected life-time: "+new SimPerson().expectedLifetime());
	}


}
