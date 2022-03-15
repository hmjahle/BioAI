import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MultiThreadEA extends Thread {
    ArrayList<Individual> convPopulation;

    public static void main(String args[]) {
        int nbrThreads = 3;
        ArrayList<MultiThreadEA> threadList = new ArrayList<MultiThreadEA>(nbrThreads);
        for (int i = 0; i < nbrThreads; i++) {
            MultiThreadEA thread = new MultiThreadEA();
            thread.start();
            threadList.add(thread);
        }

        ArrayList<Individual> mergedPopulation = new ArrayList<Individual>();
        for (MultiThreadEA thread : threadList) {
            try {
                thread.join();
                mergedPopulation.addAll(thread.convPopulation);
            } catch (InterruptedException e) {
                System.out.println("join() failed!");
            }
        }

        // Hyper-parameters
        int epochs = 100;
        int trainInstanceIndex = 9;
        int popSize = 500;
        double pC = 0.5;
        double pM = 0.007;
        int lambda = 50; // 3 is more populare now a days

        // Load JSON
        TrainData data = new TrainData();
        JSONArray trainArray = data.getTrainArray();
        JSONObject trainInstance = (JSONObject) trainArray.get(trainInstanceIndex);

        // Get datastructuresgit
        int nbrNurses = Integer.parseInt(Long.toString((Long) trainInstance.get("nbr_nurses")));
        int capacityNurse = Integer.parseInt(Long.toString((Long) trainInstance.get("capacity_nurse")));
        // double benchmark = (double) trainInstance.get("benchmark");
        Depot depot = data.getDepot(trainInstance);
        HashMap<Integer, Patient> patients = data.getPatients(trainInstance);
        double[][] travelTimes = data.getTravelTime(trainInstance);

        Population populationClass = new Population(nbrNurses, capacityNurse, depot, patients, travelTimes);
        Fitness fitnessClass = new Fitness(nbrNurses, capacityNurse, depot, patients, travelTimes);
        Parent parentClass = new Parent(nbrNurses, capacityNurse, depot, patients, travelTimes);
        Offspring offspringClass = new Offspring(nbrNurses, capacityNurse, depot, patients, travelTimes);
        Survivor survivorClass = new Survivor(nbrNurses, capacityNurse, depot, patients, travelTimes);

        // Set up SGA
        ArrayList<Individual> population = mergedPopulation;

        for (int epoch = 1; epoch < epochs; epoch++) {
            // Fitness of new Popuation
            ArrayList<Double> populationgFitness = fitnessClass.getPenaltyFitness(population);
            double popMaxFitVal = fitnessClass.MaxFitness;
            System.out.println("Min Penalty " + fitnessClass.MinPenalty);
            System.out.println("Min non Feasable Fitness " + fitnessClass.MinFitness);
            System.out.println("Min Fitness " + fitnessClass.bestFeasibleFitness);
            System.out.println("Epoch " + epoch);
            ArrayList<Double> transFitness = fitnessClass.transformFitnessArray(populationgFitness,
                    popMaxFitVal);

            // Parent Selection
            ArrayList<Individual> parents = parentClass.selectParentsProbabilistic(transFitness, population,
                    popSize);
            ArrayList<Double> parentTransFitness = parentClass.parentFitness;

            // Offspring
            ArrayList<Individual> offspring = offspringClass.createOffspring(parents, parentTransFitness,
                    pC,
                    pM, lambda);
            ArrayList<Double> offspringFitness = fitnessClass.getPenaltyFitness(offspring); // This can
                                                                                            // definitly
                                                                                            // be optimized
            double offMaxFitVal = fitnessClass.MaxFitness;
            ArrayList<Double> offspringTransFitness = fitnessClass.transformFitnessArray(offspringFitness,
                    offMaxFitVal);

            // Survivor Selection
            // (lambda, mu)-selection, based on offspring only (lambda > mu)
            ArrayList<Individual> survivors = survivorClass.deterministicOffspringSelection(offspring,
                    offspringTransFitness, popSize);
            population = survivors;
        }
        ArrayList<Double> populationgFitness = fitnessClass.getPenaltyFitness(population);
        Util validationClass = new Util();
        if (fitnessClass.bestFeasibleFitness < Math.pow(10, 10)) {
            System.out.println("\n" + validationClass.getValidationFormat(fitnessClass.bestFeasibleIndividual) + "\n");
        } else {
            System.out
                    .println("\n" + validationClass.getValidationFormat(fitnessClass.bestNonFeasibleIndividual) + "\n");
        }
    }

    public void run() {
        // Hyper-parameters
        int epochs = 200;
        int trainInstanceIndex = 9;
        int popSize = 300;
        double pC = 0.5;
        double pM = 0.007;
        int lambda = 50; // 3 is more populare now a days

        // Load JSON
        TrainData data = new TrainData();
        JSONArray trainArray = data.getTrainArray();
        JSONObject trainInstance = (JSONObject) trainArray.get(trainInstanceIndex);

        // Get datastructuresgit
        int nbrNurses = Integer.parseInt(Long.toString((Long) trainInstance.get("nbr_nurses")));
        int capacityNurse = Integer.parseInt(Long.toString((Long) trainInstance.get("capacity_nurse")));
        // double benchmark = (double) trainInstance.get("benchmark");
        Depot depot = data.getDepot(trainInstance);
        HashMap<Integer, Patient> patients = data.getPatients(trainInstance);
        double[][] travelTimes = data.getTravelTime(trainInstance);

        Population populationClass = new Population(nbrNurses, capacityNurse, depot, patients, travelTimes);
        Fitness fitnessClass = new Fitness(nbrNurses, capacityNurse, depot, patients, travelTimes);
        Parent parentClass = new Parent(nbrNurses, capacityNurse, depot, patients, travelTimes);
        Offspring offspringClass = new Offspring(nbrNurses, capacityNurse, depot, patients, travelTimes);
        Survivor survivorClass = new Survivor(nbrNurses, capacityNurse, depot, patients, travelTimes);

        // Set up SGA
        ArrayList<Individual> population = populationClass.generatePopArray(trainInstance, nbrNurses, popSize);

        for (int epoch = 1; epoch < epochs; epoch++) {
            // Fitness of new Popuation
            ArrayList<Double> populationgFitness = fitnessClass.getPenaltyFitness(population);
            double popMaxFitVal = fitnessClass.MaxFitness;
            System.out.println("Min Penalty " + fitnessClass.MinPenalty);
            System.out.println("Min non Feasable Fitness " + fitnessClass.MinFitness);
            System.out.println("Min Fitness " + fitnessClass.bestFeasibleFitness);
            System.out.println("Epoch " + epoch);
            ArrayList<Double> transFitness = fitnessClass.transformFitnessArray(populationgFitness,
                    popMaxFitVal);

            // Parent Selection
            ArrayList<Individual> parents = parentClass.selectParentsProbabilistic(transFitness, population,
                    popSize);
            ArrayList<Double> parentTransFitness = parentClass.parentFitness;

            // Offspring
            ArrayList<Individual> offspring = offspringClass.createOffspring(parents, parentTransFitness,
                    pC,
                    pM, lambda);
            ArrayList<Double> offspringFitness = fitnessClass.getPenaltyFitness(offspring); // This can
                                                                                            // definitly
                                                                                            // be optimized
            double offMaxFitVal = fitnessClass.MaxFitness;
            ArrayList<Double> offspringTransFitness = fitnessClass.transformFitnessArray(offspringFitness,
                    offMaxFitVal);

            // Survivor Selection
            // (lambda, mu)-selection, based on offspring only (lambda > mu)
            ArrayList<Individual> survivors = survivorClass.deterministicOffspringSelection(offspring,
                    offspringTransFitness, popSize);
            population = survivors;
        }
        ArrayList<Double> populationgFitness = fitnessClass.getPenaltyFitness(population);
        Util validationClass = new Util();

        if (fitnessClass.bestFeasibleFitness < Math.pow(10, 10)) {
            System.out.println("\n" + validationClass.getValidationFormat(fitnessClass.bestFeasibleIndividual) + "\n");
        } else {
            System.out
                    .println("\n" + validationClass.getValidationFormat(fitnessClass.bestNonFeasibleIndividual) + "\n");
        }
        this.convPopulation = population;
    }

}
