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
	private static void run(CAModel model, int numIter) {
		printStats(model);
		for(int i = 1; i <= numIter; i++) {
			model.step();
			System.out.print(i + " ");
			printStats(model);
		}
	}	

	public static void main(String[] args) {
		if(args.length < 6) {
			System.out.println("Arguments are: width height init_tree_chance growthrate firechance num_iterations");
			System.exit(0);
		}
		

		int width = Integer.parseInt(args[0]);
		int height = Integer.parseInt(args[1]);
		double q = Double.parseDouble(args[2]);
		double growth = Double.parseDouble(args[3]);
		double firechance = Double.parseDouble(args[4]);
		int numIter = Integer.parseInt(args[5]);

		CAModel model = new CAModel(width,height,q,growth,firechance);
		run(model, numIter);
	}

}
