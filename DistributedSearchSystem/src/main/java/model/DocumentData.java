package model;

import java.util.HashMap;
import java.util.Map;

public class DocumentData {
    private Map<String, Double> termToFrequencyMap = new HashMap<>();

    public void addToTermToFrequencyMap(String term, double frequency) {
        this.termToFrequencyMap.put(term, frequency);
    }

    public double getFrequencyForTerm(String term) {
        return this.termToFrequencyMap.get(term);
    }
}
