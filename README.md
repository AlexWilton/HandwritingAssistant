#DEAL Assistant (Java)
###Digitally Enhanced hAndwriting Learning (DEAL) Assistant


DEAL Assistant is a proof of concept system has been developed in order to explore how a futurist pen on paper could 
aid handwriting learning through providing real-time visual feedback. Currently the system can analyse strokes from a 
learner recognising handwritten words. The system can be used to create exercises. Examples of possible exercises are
provided (Alphabet, Headline Sentence and Recap Exercises). Mistakes made by the learner can be highlighted.


`Minimum Requirements: jdk 1.7 and maven2`

###Getting Started Instructions

1) Clone or download repository.

2) Open a terminal and change directory to the project directory.

3) To compile into a jar, run "mvn assembly:assembly -DdescriptorId=jar-with-dependencies". A jar will be generated within the target directory.

4) To execute the jar, run "java -jar target/DEALAssistant-1.0-jar-with-dependencies.jar"