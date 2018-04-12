package group15;

import de.learnlib.algorithms.lstar.dfa.ClassicLStarDFA;
import de.learnlib.algorithms.lstar.dfa.ClassicLStarDFABuilder;
import de.learnlib.algorithms.ttt.dfa.TTTLearnerDFA;
import de.learnlib.algorithms.ttt.dfa.TTTLearnerDFABuilder;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.filter.cache.sul.SULCaches;
import de.learnlib.filter.statistic.oracle.CounterOracle.DFACounterOracle;
import de.learnlib.oracle.equivalence.WMethodEQOracle.DFAWMethodEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle.DFASimulatorOracle;
import de.learnlib.util.Experiment.DFAExperiment;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;


public final class Experiment {

    // Experiment Parameters

    private static final Boolean randomDFA = true; // use this to manually put in DFA or just get random ones

    private static final int DFASize = 60; // size of the random DFA

    private static final int alphabetSize = 6; // alphabet size of the random DFA

    private static final  int EXPLORATION_DEPTH = 4; // not sure what this is just keep it as 4

    private Experiment() {}

    public static void main(String[] args) {


        System.out.println("Started experiment with values:");
        System.out.println("random DFA: " + randomDFA + " | DFA size: " + DFASize + " | DFA alphabet size: " + alphabetSize);


        // load DFA and alphabet
        CompactDFA<Character> target = constructSUL(randomDFA, alphabetSize, DFASize);
        Alphabet<Character> inputs = target.getInputAlphabet();


        // construct simulator membership query oracles
        // input  - Character (determined by example)
        DFAMembershipOracle<Character> sulLStarMem = new DFASimulatorOracle<>(target);
        DFAMembershipOracle<Character> sulTTTMem = new DFASimulatorOracle<>(target);

        DFAMembershipOracle<Character> sulLStarEq = new DFASimulatorOracle<>(target);
        DFAMembershipOracle<Character> sulTTTEq = new DFASimulatorOracle<>(target);

        // oracle for counting queries wraps SUL
        DFACounterOracle<Character> mqOracleLStarMem = new DFACounterOracle<>(sulLStarMem, "membership queries");
        DFACounterOracle<Character> mqOracleTTTMem = new DFACounterOracle<>(sulTTTMem,"membership queries");

        DFACounterOracle<Character> mqOracleLStarEq = new DFACounterOracle<>(sulLStarEq, "equivalence queries");
        DFACounterOracle<Character> mqOracleTTTEq = new DFACounterOracle<>(sulTTTEq,"equivalence queries");

        // Learners
        ClassicLStarDFA<Character> LStar;
        TTTLearnerDFA<Character> tttLearner;

        // Teachers (Oracles)
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

        long TTTTimerStart = System.nanoTime();
        experimentTTT.run();

        long TTTTimerEnd = System.nanoTime();

        long TTTDuration = ((TTTTimerEnd - TTTTimerStart) / 1000000 );  // milliseconds

        DFA<?, Character> resultT = experimentTTT.getFinalHypothesis();

        // Print out the statistics
        System.out.println("---------------TTT----------------");
        System.out.println("Time taken:  " + TTTDuration);
        System.out.println(experimentTTT.getRounds().getSummary());
        System.out.println(mqOracleTTTMem.getStatisticalData().getDetails());
        System.out.println(mqOracleTTTEq.getStatisticalData());

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
        experimentLStar.run();

        long LStarTimerEnd = System.nanoTime();

        long LStarDuration = ((LStarTimerEnd - LStarTimerStart) / 1000000 );  // milliseconds

        DFA<?, Character> resultL = experimentLStar.getFinalHypothesis();

        // Print out the statistics
        System.out.println("--------------L STAR--------------");
        System.out.println("Time taken:  " + LStarDuration);
        System.out.println(experimentLStar.getRounds().getSummary());
        System.out.println(mqOracleLStarMem.getStatisticalData());
        System.out.println(mqOracleLStarEq.getStatisticalData());


        System.out.println("-------------------------------------------------------");
        System.out.println(resultT.size() + "           " + resultL.size());
    }

    private static CompactDFA<Character> constructSUL(Boolean isRandom, int alphabetSize, int DFASize) {

        if (isRandom) {
            GetRandomDFA DFA = new GetRandomDFA();
            return DFA.randomDFA(DFASize, alphabetSize);

        } else {

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