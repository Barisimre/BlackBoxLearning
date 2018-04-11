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


public final class Experiment {

    // Experiment Parameters

    private static Boolean randomDFA = true; // use this to manually put in DFA or just get random ones

    private static int DFASize = 5; // size of the random DFA

    private static int alphabetSize = 6; // alphabet size of the random DFA

    private static final int EXPLORATION_DEPTH = 4;

    private Experiment() {
    }

    public static void main(String[] args) {

        // load DFA and alphabet
        CompactDFA<Character> target = constructSUL(randomDFA, DFASize, alphabetSize);
        Alphabet<Character> inputs = target.getInputAlphabet();


        // construct simulator membership query oracles
        // input  - Character (determined by example)
        DFAMembershipOracle<Character> sulLStar = new DFASimulatorOracle<>(target);
        DFAMembershipOracle<Character> sulTTT = new DFASimulatorOracle<>(target);

        // oracle for counting queries wraps SUL
        DFACounterOracle<Character> mqOracleLStar = new DFACounterOracle<>(sulLStar, "membership queries");
        DFACounterOracle<Character> mqOracleTTT = new DFACounterOracle<>(sulTTT, "membership queries");

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
                        .withOracle(mqOracleTTT)
                        .create();

        wMethodTTT = new DFAWMethodEQOracle<>(mqOracleTTT, EXPLORATION_DEPTH);

        experimentTTT = new DFAExperiment<>(tttLearner, wMethodTTT, inputs);

        experimentTTT.setProfile(true);

        experimentTTT.setLogModels(true);

        experimentTTT.run();

        DFA<?, Character> resultT = experimentTTT.getFinalHypothesis();

        // Print out the statistics
        System.out.println("---------------TTT----------------");
        System.out.println(experimentTTT.getRounds().getSummary());
        System.out.println(mqOracleTTT.getStatisticalData().getSummary());


        //==================================L STAR============================
        // Set up and run the experiment
        LStar =
                new ClassicLStarDFABuilder<Character>().withAlphabet(inputs) // input alphabet
                        .withOracle(mqOracleLStar) // membership oracle
                        .create();

        wMethodLStar = new DFAWMethodEQOracle<>(mqOracleLStar, EXPLORATION_DEPTH);

        experimentLStar = new DFAExperiment<>(LStar, wMethodLStar, inputs);

        experimentLStar.setProfile(true);

        experimentLStar.setLogModels(true);

        experimentLStar.run();

        DFA<?, Character> resultL = experimentLStar.getFinalHypothesis();

        // Print out the statistics
        System.out.println("--------------L STAR---------------");
        System.out.println(experimentLStar.getRounds().getSummary());
        System.out.println(mqOracleLStar.getStatisticalData().getSummary());


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