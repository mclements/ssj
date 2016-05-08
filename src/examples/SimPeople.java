import umontreal.ssj.stat.*;
import java.util.concurrent.Callable;
class SimPeople implements Callable<Tally>, Runnable {
	public Tally call() throws Exception {
		SimPerson person = new SimPerson();
		synchronized(person.s) {
			for (int id=0; id<1000000; ++id) {
				person.run();
			}
		}
		return person.lifetime;
	}
	public void run() {
		SimPerson person = new SimPerson();
		synchronized(person.s) {
			for (int id=0; id<1000000; ++id) {
				person.run();
			}
		}
	}
}
