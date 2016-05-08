import umontreal.ssj.simevents.*;
import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.stat.*;
public class TestSimeventsSim {
	static class Person {
		MRG32k3a rng = new MRG32k3a();
		Simulator s = new Simulator();
		Tally lifetime = new Tally();
		double cancerIncRate=1/60.0,
			otherDeathRate=1/70.0,
			excessRate=1/20.0,
			pSusceptible=1.0;
		void run () {
			s.init(); 
			new OtherDeath().schedule(ExponentialGen.nextDouble(rng, otherDeathRate));
			if (rng.nextDouble() < pSusceptible)
				new CancerInc().schedule(ExponentialGen.nextDouble(rng,cancerIncRate));
			s.start();
		}
		class CancerInc extends Event {
			CancerInc() { super(s); }
			public void actions() {
				new CancerDeath().schedule(ExponentialGen.nextDouble(rng,excessRate));
			}
		}
		class CancerDeath extends Event {
			CancerDeath() { super(s); }
			public void actions() {
				lifetime.add(simulator().time());
				simulator().stop();
			}
		}
		class OtherDeath extends Event {
			OtherDeath() { super(s); }
			public void actions() {
				lifetime.add(simulator().time());
				simulator().stop();
			}
		}
		double expectedLifetime() {
			return pSusceptible*(1.0/(cancerIncRate+otherDeathRate) +
					     cancerIncRate/(cancerIncRate-excessRate)*
					     (1.0/(excessRate+otherDeathRate) -
					      1.0/(cancerIncRate+otherDeathRate))) +
				(1.0-pSusceptible)/otherDeathRate;
		}
	}
	public static void main (String[] args) {
		Tally lifetime = new Tally();
		for (int strat=0; strat<10; ++strat) {
			Person person = new Person();
			for (int id=0; id<1000000; ++id) {
				person.run();
			}
			lifetime.add(person.lifetime);
		}
		System.out.println(lifetime.report());
		System.out.println(lifetime.formatCINormal(0.95));
		System.out.println("Expected life-time: "+new Person().expectedLifetime());
	}
}
