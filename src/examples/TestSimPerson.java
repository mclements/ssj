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
			SimPerson person;
			// omp critical
			{
				person = new SimPerson();
			}
			synchronized(person.s) {
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
	public static void main3 (String[] args) {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		Tally lifetime = new Tally();
		List<Future<Tally>> list = new ArrayList<Future<Tally>>();
		for (int j=0; j<2; ++j) {
			Callable<Tally> worker = new SimPeople();
			Future<Tally> submit = executor.submit(worker);
			list.add(submit);
		}
		for (Future<Tally> future : list) {
			try {
				lifetime.add(future.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		System.out.println(lifetime.report());
		System.out.println(lifetime.formatCINormal(0.95));
		System.out.println("Expected life-time: "+new SimPerson().expectedLifetime());
		executor.shutdown();
	}

	public static void main (String[] args) {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		class OMPContext {
			public Tally local_lifetime;
		}
		Tally lifetime = new Tally();
		final OMPContext ompContext = new OMPContext();
		ompContext.local_lifetime = lifetime;
		for (int j=0; j<2; ++j) {
			executor.execute(new Runnable() {
					public void run() {
						SimPerson person = new SimPerson();
						for (int id=0; id<1000000; ++id) {
							person.run();
						}
						synchronized(ompContext) {
							ompContext.local_lifetime.add(person.lifetime);
						}
					}
				});
		}
		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.SECONDS);
		System.out.println(lifetime.report());
		System.out.println(lifetime.formatCINormal(0.95));
		System.out.println("Expected life-time: "+new SimPerson().expectedLifetime());
	}
}
