import umontreal.ssj.stat.*;
class TestSimPerson {
	public static void main (String[] args) {
		Tally lifetime = new Tally();
		// omp parallel for public(lifetime)
		for (int strat=0; strat<10; ++strat) {
			//System.out.println(OMP4J_THREAD_NUM);
			SimPerson person = new SimPerson();
			for (int id=0; id<10000000; ++id) {
				person.run();
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
}
