CP = ../../build/libs/ssj-3.1.1.jar:.

test:
	javac -cp $(CP) TestSimeventsSim.java
	time java -cp $(CP) TestSimeventsSim

threaded:
	./omp4j -d sources -cp $(CP) -v -n TestSimeventsSim.java
	./omp4j -d classes -cp $(CP) -v TestSimeventsSim.java
	cd classes
	time java -cp $(CP) TestSimeventsSim
