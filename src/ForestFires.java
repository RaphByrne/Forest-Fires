import java.lang.*;

/**
 * Starter class for running the forest fires experiment
 */
public class ForestFires {

	private static void printStats(CAModel model) {
		System.out.println(model.numEmpty + " " + model.numTrees + " " + model.numFires);
	}



	/**
 	* Runs the model for a number of iterations, prints data to standard out in form:
 	* iteration_num modelStats
 	*
 	* @param model the model
 	* @param numIter the number of iterations to run
 	*/
	private static void run(CAModel model) {
		int minSteps = 20;
		int[] lastEmpties = new int[minSteps];
		int[] lastTrees = new int[minSteps];
		int[] lastFires = new int[minSteps];
		int i = 0;
		System.out.print(i + " ");	
		printStats(model);
		for(i = 1; i < minSteps; i++) {
			model.step();
			System.out.print(i + " ");
			printStats(model);
			lastEmpties[i] = model.numEmpty;
			lastTrees[i] = model.numTrees;
			lastFires[i] = model.numFires;
		}
		while(!convergenceCheck(lastEmpties,lastTrees,lastFires, (i-1)%minSteps)) {
			model.step();
			System.out.print(i + " ");
			printStats(model);
			int index = (i% minSteps);
			lastEmpties[index] = model.numEmpty;
			lastTrees[index] = model.numTrees;
			lastFires[index] = model.numFires;
			i++;
		}	
	}

	private static double average(int[] a) {
		int sum = 0;	
		for(Integer i : a) {
			sum += i;
		}
		return sum/a.length;
	}

	private static double tolerance = 0.01;

	private static boolean convergenceCheck(int[] lastEmpties, int[] lastTrees, int[] lastFires, int i) {
		int len = Math.max(lastEmpties.length, lastTrees.length);
		len = Math.max(len, lastFires.length);
		int lastE = lastEmpties[i];
		int lastT = lastTrees[i]; 
		int lastF = lastFires[i];
		double aveE = average(lastEmpties);
		double aveT = average(lastTrees);
		double aveF = average(lastFires);
		double varE = tolerance*aveE;
		double varT = tolerance*aveT;
		double varF = tolerance*aveF;
		boolean result = true;
		if(lastE > (aveE + varE) || lastE < (aveE - varE))
			result = false;
		if(lastT > (aveT + varT) || lastT < (aveT - varT))
			result = false;
		if(lastF > (aveF + varF) || lastF < (aveF - varF))
			result = false;
		return result;
	}

	public static void main(String[] args) {
		if(args.length < 5) {
			System.out.println("Arguments are: width height init_tree_chance growthrate firechance");
			System.exit(0);
		}
		

		int width = Integer.parseInt(args[0]);
		int height = Integer.parseInt(args[1]);
		double q = Double.parseDouble(args[2]);
		double growth = Double.parseDouble(args[3]);
		double firechance = Double.parseDouble(args[4]);

		CAModel model = new CAModel(width,height,q,growth,firechance);
		run(model);
	}

}
