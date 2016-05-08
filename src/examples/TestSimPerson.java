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
	public static void main (String[] args) {
		Tally lifetime = new Tally();
		SimPerson person = new SimPerson();
		// omp parallel for public(lifetime) private(person)
		for (int j=0; j<2; ++j) {
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
	public static void main2 (String[] args) throws InterruptedException {
		int NTHREADS = 2;
		ExecutorService es = Executors.newFixedThreadPool(NTHREADS);
		class OMPContext {
			public Tally local_lifetime;
			public SimPerson local_person[];
		}
		Tally lifetime = new Tally();
		final OMPContext ompContext = new OMPContext();
		ompContext.local_lifetime = lifetime;
		ompContext.local_person = new SimPerson[NTHREADS];
		for (int j=0; j<NTHREADS; ++j) {
			ompContext.local_person[j] = new SimPerson();
		}
		for (int j=0; j<NTHREADS; ++j) {
			final int jj = j;
			es.execute(new Runnable() {
					public void run() {
						synchronized(ompContext.local_person[jj]) {
							for (int id=0; id<1000000; ++id) {
								ompContext.local_person[jj].run();
							}
						}
						synchronized(ompContext) {
							ompContext.local_lifetime.add(ompContext.local_person[jj].lifetime);
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
