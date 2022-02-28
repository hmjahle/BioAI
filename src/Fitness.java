import java.util.ArrayList;
import java.util.ListIterator;

public class Fitness {
    public int nbrNurses;
    public int capacityNurse;
    public Depot depot;
    public double[][] travelTimes;
    public Patient[] patients;
    public double prevMaxFitness;
    public double prevMinFitness;
    public double prevMinPenalty;
    public ArrayList<ArrayList<Double>> fitnessMatrix; // Keeps record of the fitness for each path

    public Fitness(int nbrNurses, int capacityNurse, Depot depot, Patient[] patients, double[][] travelTimes) {
        this.nbrNurses = nbrNurses;
        this.capacityNurse = capacityNurse;
        this.depot = depot;
        this.travelTimes = travelTimes;
        this.patients = patients;
    }

    // Naively checks route duration without any feasibility check
    public ArrayList<Double> getRegularFitness(ArrayList<Individual> population) {
        ArrayList<Double> fitness = new ArrayList<Double>();
        double maxVal = 0;
        double minVal = Math.pow(10, 10);
        for (int i = 0; i < population.size(); i++) {
            Individual individual = population.get(i);
            ArrayList<Integer> routes = individual.routes;
            double indFitness = 0;
            for (int j = 0; j < routes.size() - 1; j++) {
                indFitness += travelTimes[routes.get(j)][routes.get(j + 1)];

            }
            if(indFitness > maxVal){
                maxVal = indFitness;
            }
            if(indFitness < minVal){
                minVal = indFitness;
            }
            fitness.add(indFitness);
        }
        this.prevMaxFitness = maxVal;
        this.prevMinFitness = minVal;

        return fitness;
    }

    public ArrayList<Double> getPenaltyFitness(ArrayList<Individual> population) {
        ArrayList<Double> fitness = new ArrayList<Double>();
        double maxVal = 0;
        double minVal = Math.pow(10, 10);
        double minPenVal = Math.pow(10, 10);
        double penaltyScale = 100;
        for (int i = 0; i < population.size(); i++) {

            Individual individual = population.get(i);
            ArrayList<Integer> routes = individual.routes;

            double travelDuration = 0;  
            double penaltyCapacity = 0;
            double penaltyMissedCareTime = 0; 

            double nurseClock = 0;            
            double nurseUsage = 0;

            for (int j = 0; j < routes.size() - 1; j++) {
                Patient patient = this.patients[routes.get(j + 1)];
                double travel = travelTimes[routes.get(j)][routes.get(j + 1)];
                nurseClock += travel;
                travelDuration += travel;

                if(nurseClock < patient.start_time){ // Wait
                    nurseClock = patient.start_time;
                }

                if(nurseClock > patient.end_time){ // Missed patient entirely
                    penaltyMissedCareTime = patient.care_time + (nurseClock - patient.end_time);
                } 

                else { // Arrived in time window
                    double availableCareTime =  patient.end_time - nurseClock;
                    double restCareTime = availableCareTime - patient.care_time;
                    if(restCareTime < 0){ // Penalty! Arrived too late
                        penaltyMissedCareTime += -restCareTime; // Care time missing
                        nurseClock = patient.end_time;
                        nurseUsage += availableCareTime;
                    }
                    else{ // Nailed it!
                        nurseUsage += patient.care_time;
                        nurseClock += patient.care_time;
                    }
                }

                if (routes.get(j + 1) == 0){ // Arrival at Depot
                    if(nurseUsage > capacityNurse){ // To much work for one nurse
                        penaltyCapacity += nurseUsage - capacityNurse;
                    }
                    nurseUsage = 0; // Reset for new nurse
                    nurseClock = 0;
                }
            }
            double penalty = (penaltyMissedCareTime + penaltyCapacity) * penaltyScale;
            double penaltyFitness = travelDuration + penalty;
            if( (penaltyMissedCareTime + penaltyCapacity) == 0){
                System.out.println("LGTM!");
            }

            fitness.add(penaltyFitness);

            if(penaltyFitness > maxVal){
                maxVal = penaltyFitness;
            }
            if(penaltyFitness < minVal){
                minVal = penaltyFitness;
            }
            if(penalty < minPenVal){
                this.prevMinPenalty = penalty;
            }
        }

        this.prevMaxFitness = maxVal;
        this.prevMinFitness = minVal;

        return fitness;
    }

    public ArrayList<Double> transformFitnessArray(ArrayList<Double> fitness, double maxValue) {
        ArrayList<Double> transformedFitness = new ArrayList<Double>();
        
        ListIterator<Double> iter = fitness.listIterator();
        while( iter.hasNext() ){
            Double value = iter.next();
            Double newValue = maxValue - value;
            transformedFitness.add(newValue);
        }
        return transformedFitness;
    }   
}
