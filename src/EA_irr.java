import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class EA_irr {

    public static void main(String args []){
        // Hyper-parameters
        int trainInstanceIndex = 7;
        int popSize = 5000;
        double pC = 0.7;
        double pM = 0.0003;
        int lambda = 7; // 3 is more populare now a days

        // Load JSON
        TrainData data = new TrainData();
        JSONArray trainArray = data.getTrainArray();
        JSONObject trainInstance = (JSONObject) trainArray.get(trainInstanceIndex);

        // Get datastructures
        int nbrNurses = Integer.parseInt(Long.toString( (Long) trainInstance.get("nbr_nurses")));
        int capacityNurse = Integer.parseInt(Long.toString( (Long) trainInstance.get("capacity_nurse")));
        double benchmark =  (double) trainInstance.get("benchmark");
        Depot depot = data.getDepot(trainInstance);
        Patient[] patients = data.getPatients(trainInstance);
        double[][] travelTimes = data.getTravelTime(trainInstance);
         
        // Set up SGA
        Population populationClass = new Population(nbrNurses, capacityNurse, depot, patients, travelTimes);
        ArrayList<ArrayList<Integer>> population = populationClass.generatePopArray(trainInstance, nbrNurses, popSize);

        Fitness fitnessClass = new Fitness(nbrNurses, capacityNurse, depot, patients, travelTimes);
        ArrayList<Double> fitness = fitnessClass.getRegularFitnessArray(population);

        // Consider to normalize fitness for greater selection pressure 
        Parent parentClass = new Parent(nbrNurses, capacityNurse, depot, patients, travelTimes);
        ArrayList<ArrayList<Integer>> parents = parentClass.selectParentsProbabilistic(fitness, population, popSize);
        ArrayList<Double> parentFitness = parentClass.parentFitness;

        Offspring offspringClass = new Offspring(nbrNurses, capacityNurse, depot, patients, travelTimes);
        ArrayList<ArrayList<Integer>> offspring = offspringClass.createOffspring(parents, parentFitness, pC, pM, lambda);
        ArrayList<Double> offspringfitness = fitnessClass.getRegularFitnessArray(offspring); // This can definitly be optimized

        // (lambda, mu)-selection, based on offspring only (lambda > mu) 
        Survivor survivorClass = new Survivor(nbrNurses, capacityNurse, depot, patients, travelTimes);
        ArrayList<ArrayList<Integer>> survivors = survivorClass.deterministicOffspringSelection(offspring, offspringfitness, popSize);
    }
    
}