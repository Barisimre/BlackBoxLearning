package group15;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

import java.util.Random;

class GetRandomDFA {

    CompactDFA<Character> randomDFA(int DFASize, int alphabetSize) {

        Alphabet<Character> INPUT_ALPHABET = Alphabets.characters((char) 97, (char) (97 + alphabetSize));

        final Random random = new Random(42);

        return RandomAutomata.randomDFA(random, DFASize, INPUT_ALPHABET);
    }

}
