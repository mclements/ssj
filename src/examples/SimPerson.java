import umontreal.ssj.simevents.*;
import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.stat.*;
// import java.util.concurrent.ThreadLocalRandom;
class SimPerson implements Runnable {
	Simulator s;
	Tally lifetime;
	ThreadLocalMRG32k3a rng;
	double cancerIncRate=1/60.0,
		otherDeathRate=1/70.0,
		excessRate=1/20.0,
		pSusceptible=1.0;
	SimPerson() {
		s = new Simulator();
		lifetime = new Tally();
		rng = ThreadLocalMRG32k3a.current();
	}
	public void run () {
		s.init(); 
		new OtherDeath().schedule(ExponentialGen.nextDouble(rng,otherDeathRate));
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
