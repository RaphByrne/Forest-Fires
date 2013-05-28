import java.lang.*;

/**
 * Starter class for running the forest fires experiment
 */
public class ForestFiresRatio {

	private static void printStats(CAModel model) {
		System.out.println(model.numEmpty + " " + model.numTrees + " " + model.numFires);
	}



	/**
 	*
 	* @param model the model
 	*/
	private static void run(CAModel model, double ratio) {
		for(double g = model.growthrate; g < 1; g += 0.01) {
			model.lighteningChance = g*ratio;
			int minSteps = 20;
			int[] lastEmpties = new int[minSteps];
			int[] lastTrees = new int[minSteps];
			int[] lastFires = new int[minSteps];
			int i = 0;
			for(i = 1; i < minSteps; i++) {
				model.step();
				lastEmpties[i] = model.numEmpty;
				lastTrees[i] = model.numTrees;
				lastFires[i] = model.numFires;
			}
			while(!convergenceCheck(lastEmpties,lastTrees,lastFires, (i-1)%minSteps)) {
				model.step();
				int index = (i% minSteps);
				lastEmpties[index] = model.numEmpty;
				lastTrees[index] = model.numTrees;
				lastFires[index] = model.numFires;
				i++;
			}
			System.out.print(g + " ");
			printStats(model);
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
		if(args.length < 4) {
			System.out.println("Arguments are: width height init_tree_chance fpRatio");
			System.exit(0);
		}
		

		int width = Integer.parseInt(args[0]);
		int height = Integer.parseInt(args[1]);
		double q = Double.parseDouble(args[2]);
		double ratio = Double.parseDouble(args[3]);
		double growth = 0.01;
		double firechance = growth*ratio;

		CAModel model = new CAModel(width,height,q,growth,firechance);
		run(model,ratio);
	}

}
