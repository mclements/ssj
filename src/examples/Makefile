CP = ~/src/java/ssj/build/libs/ssj-3.1.1.jar:.:classes

testSimPerson:
	javac -cp $(CP) SimPerson.java
	javac -cp $(CP) TestSimPerson.java
	time java -cp $(CP) TestSimPerson

testSimPerson2:
	javac -cp $(CP) SimPerson.java
	omp4j -d classes -cp $(CP) TestSimPerson2.java
	time java -cp $(CP) TestSimPerson2


test:
	javac -cp $(CP) TestSimeventsSim.java
	time java -cp $(CP) TestSimeventsSim

threaded:
	./omp4j -d sources -cp $(CP) -v -n TestSimeventsSim.java
	./omp4j -d classes -cp $(CP) -v TestSimeventsSim.java
	cd classes
	time java -cp $(CP) TestSimeventsSim

short:
	./omp4j -d classes -cp $(CP) -v Testomp4j.java
	cd classes
	time java Testomp4j
