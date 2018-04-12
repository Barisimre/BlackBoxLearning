package group15;

import de.learnlib.algorithms.lstar.dfa.ClassicLStarDFA;
import de.learnlib.algorithms.lstar.dfa.ClassicLStarDFABuilder;
import de.learnlib.algorithms.ttt.dfa.TTTLearnerDFA;
import de.learnlib.algorithms.ttt.dfa.TTTLearnerDFABuilder;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.filter.statistic.oracle.CounterOracle.DFACounterOracle;
import de.learnlib.oracle.equivalence.WMethodEQOracle.DFAWMethodEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle.DFASimulatorOracle;
import de.learnlib.util.Experiment.DFAExperiment;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
@author Baris Imre
b.imre@student.utwente.nl
 **/

public final class Experiment {

    // Experiment Parameters

    private static final Boolean randomDFA = true; // use this to manually put in DFA or just get random ones

    private static final int DFASize = 50; // size of the random DFA

    private static final int alphabetSize = 15; // alphabet size of the random DFA

    private static final  int EXPLORATION_DEPTH = 4;

    private static final int randomNumber = 1337; // this directly effects the seed for the random DFA

    private Experiment() {}

    public static void main(String[] args) {
        StringBuilder TTTTime = new StringBuilder();
        StringBuilder TTTMem = new StringBuilder();
        StringBuilder TTTEq = new StringBuilder();

        StringBuilder LStarTime = new StringBuilder();
        StringBuilder LStarMem = new StringBuilder();
        StringBuilder LStarEq = new StringBuilder();

        // Run the experiment with some number of different random DFAs (i)
        for (int i = 1; i <= 5; i++) {


            // load DFA and alphabet
            CompactDFA<Character> target = constructSUL(randomDFA, alphabetSize, DFASize, i + randomNumber);
            Alphabet<Character> inputs = target.getInputAlphabet();


            // construct simulator membership query oracles
            // input  - Character (determined by example)
            DFAMembershipOracle<Character> sulLStarMem = new DFASimulatorOracle<>(target);
            DFAMembershipOracle<Character> sulTTTMem = new DFASimulatorOracle<>(target);

            // oracles for counting equivalence queries wraps SUL
            DFAMembershipOracle<Character> sulLStarEq = new DFASimulatorOracle<>(target);
            DFAMembershipOracle<Character> sulTTTEq = new DFASimulatorOracle<>(target);

            // oracles for counting membership queries wraps SUL
            DFACounterOracle<Character> mqOracleLStarMem = new DFACounterOracle<>(sulLStarMem, "membership queries");
            DFACounterOracle<Character> mqOracleTTTMem = new DFACounterOracle<>(sulTTTMem, "membership queries");

            DFACounterOracle<Character> mqOracleLStarEq = new DFACounterOracle<>(sulLStarEq, "equivalence queries");
            DFACounterOracle<Character> mqOracleTTTEq = new DFACounterOracle<>(sulTTTEq, "equivalence queries");

            // Learners
            ClassicLStarDFA<Character> LStar;
            TTTLearnerDFA<Character> tttLearner;

            // Oracles (Teachers)
            DFAWMethodEQOracle<Character> wMethodLStar;
            DFAWMethodEQOracle<Character> wMethodTTT;

            // Experiment Instances
            DFAExperiment<Character> experimentLStar;
            DFAExperiment<Character> experimentTTT;



            //===========================TTT===========================================
            // Set up and run the experiment
            tttLearner =
                    new TTTLearnerDFABuilder<Character>().withAlphabet(inputs)
                            .withOracle(mqOracleTTTMem)
                            .withOracle(mqOracleTTTEq)
                            .create();

            wMethodTTT = new DFAWMethodEQOracle<>(mqOracleTTTMem, EXPLORATION_DEPTH);

            experimentTTT = new DFAExperiment<>(tttLearner, wMethodTTT, inputs);

            // Make a loading bar in the first run
            if(i == 1){
                System.out.println("Loading bar: #  #  #  #  #");
            }

            long TTTTimerStart = System.nanoTime();
            experimentTTT.run(); // This is the actual learning experiment
            long TTTTimerEnd = System.nanoTime();
            long TTTDuration = ((TTTTimerEnd - TTTTimerStart) / 1000000);  // milliseconds

            DFA<?, Character> resultT = experimentTTT.getFinalHypothesis();

            TTTTime.append(TTTDuration).append("\n");
            TTTMem.append(mqOracleTTTMem.getStatisticalData().getDetails()).append("\n");
            TTTEq.append(mqOracleTTTEq.getStatisticalData()).append("\n");


            //==================================L STAR============================
            // Set up and run the experiment
            LStar =
                    new ClassicLStarDFABuilder<Character>().withAlphabet(inputs) // input alphabet
                            .withOracle(mqOracleLStarMem) // membership oracle
                            .withOracle(mqOracleLStarEq)
                            .create();

            wMethodLStar = new DFAWMethodEQOracle<>(mqOracleLStarMem, EXPLORATION_DEPTH);

            experimentLStar = new DFAExperiment<>(LStar, wMethodLStar, inputs);

            long LStarTimerStart = System.nanoTime();
            experimentLStar.run(); // This is the actual learning experiment
            long LStarTimerEnd = System.nanoTime();
            long LStarDuration = ((LStarTimerEnd - LStarTimerStart) / 1000000);  // milliseconds

            DFA<?, Character> resultL = experimentLStar.getFinalHypothesis();

            LStarTime.append(LStarDuration).append("\n");
            LStarMem.append(mqOracleLStarMem.getStatisticalData().getDetails()).append("\n");
            LStarEq.append(mqOracleLStarEq.getStatisticalData()).append("\n");

            // Fill the loading bar after every run
            if (i == 1) {
                System.out.print("             #  ");
            } else {
                System.out.print("#  ");
            }
        }

        String ANSI_PURPLE = "\u001B[35m";
        String ANSI_RESET = "\u001B[0m";

        System.out.println("\n\nRandom DFA: " + randomDFA + " | DFA size: " + DFASize + " | DFA alphabet size: " + alphabetSize);
        System.out.println("---------------TTT----------------");
        System.out.println(ANSI_PURPLE + "Times Taken(ms):" + ANSI_RESET);
        System.out.println(TTTTime);
        System.out.println(ANSI_PURPLE + "Membership Queries:" + ANSI_RESET);
        System.out.println(TTTMem);
        System.out.println(ANSI_PURPLE + "Equivalence Queries" + ANSI_RESET);
        System.out.println(TTTEq);

        System.out.println("--------------L STAR--------------");
        System.out.println(ANSI_PURPLE + "Times Taken(ms):" + ANSI_RESET);
        System.out.println(LStarTime);
        System.out.println(ANSI_PURPLE + "Membership Queries:" + ANSI_RESET);
        System.out.println(LStarMem);
        System.out.println(ANSI_PURPLE + "Equivalence Queries" + ANSI_RESET);
        System.out.println(LStarEq);

    }

    private static CompactDFA<Character> constructSUL(Boolean isRandom, int alphabetSize, int DFASize, int seed) {

        if (isRandom) {
            GetRandomDFA DFA = new GetRandomDFA();
            return DFA.randomDFA(DFASize, alphabetSize, seed);

        } else {
            // This can be used to manually enter a DFA to the testing.
            Alphabet<Character> sigma = Alphabets.characters('a', 'b');
            // @formatter:off
            // create automaton manually
            return AutomatonBuilders.newDFA(sigma)
                    .withInitial("q0")
                    .from("q0")
                    .on('a').to("q1")
                    .on('b').to("q2")
                    .from("q1")
                    .on('a').to("q0")
                    .on('b').to("q1")
                    .from("q2")
                    .on('a').to("q2")
                    .on('b').to("q2")
                    .withAccepting("q0")
                    .create();
            // @formatter:on
        }
    }
}