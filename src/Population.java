import java.util.ArrayDeque;
import java.util.ArrayList;

import org.json.simple.JSONObject;

public class Population {
    public int nbrNurses;
    public int capacityNurse;
    public Depot depot;
    public double[][] travelTimes;
    public Patient[] patients;

    public Population(int nbrNurses, int capacityNurse, Depot depot, Patient[] patients, double[][] travelTimes) {
        this.nbrNurses = nbrNurses;
        this.capacityNurse = capacityNurse;
        this.depot = depot;
        this.travelTimes = travelTimes;
        this.patients = patients;
    }

    public Individual generateIndArray(JSONObject trainInstance, int activeNurses) {
        Individual individual = new Individual(nbrNurses, capacityNurse, depot, patients, travelTimes);
        individual.generateIndArray(trainInstance, activeNurses);
        return individual;
    }
  
    public ArrayList<Individual> generatePopArray(JSONObject trainInstance, int activeNurses, int nbrIndividuals) {
        ArrayList<Individual> population = new ArrayList<Individual>(nbrIndividuals);
        for (int i = 0; i < nbrIndividuals; i++) {
            population.add(generateIndArray(trainInstance, activeNurses));
        }
        return population;
    }

}

