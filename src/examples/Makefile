CP = ~/src/java/ssj/build/libs/ssj-3.1.1.jar:.
CP2 = ~/src/java/ssj/build/libs/ssj-3.1.1.jar:classes:.
JAVAC = javac

%.class : %.java
	$(JAVAC) -cp $(CP) $<

testSimPerson: SimPerson.class  SimPeople.class TestSimPerson.class
	time java -cp $(CP) TestSimPerson

testSimPerson2: SimPerson.class SimPeople.class TestSimPerson.class
	./omp4j -d classes -cp $(CP) -v TestSimPerson2.java
	time java -cp $(CP2) TestSimPerson

sources: TestSimPerson2.java
	./omp4j -d sources -cp $(CP) -n TestSimPerson2.java

test: TestSimeventsSim.class
	time java -cp $(CP) TestSimeventsSim

threaded:
	./omp4j -d classes -cp $(CP) -v TestSimeventsSim.java
	time java -cp $(CP) TestSimeventsSim

short:
	./omp4j -d classes -cp $(CP) -v Testomp4j.java
	time java  -cp $(CP) Testomp4j

