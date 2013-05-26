/**
 * runs the experiment for the percolation question
 */
public class Percolation {

	private static void printStats(CAModel model) {
		System.out.println(model.numEmpty + " " + model.numTrees + " " + model.numFires);
	}

	/**
 	* Runs the percolation experiment, model is run to completion and final stats given in form	
 	* q-value modelStats
 	*
 	* @param model the model
 	* @param numIter the number of iterations to run
 	*/
	private static void run(CAModel model, int numIter) {
		System.out.print(model.q + " ");
		model.setFireCentre();
		for(int i = 1; i <= numIter; i++) {
			model.step();
		}
		printStats(model);
	}	

	public static void main(String[] args) {
		if(args.length < 3) {
			System.out.println("Arguments are: width height num_iterations");
			System.exit(0);
		}
		

		int width = Integer.parseInt(args[0]);
		int height = Integer.parseInt(args[1]);
		double growth = 0;
		double firechance = 0;
		int numIter = Integer.parseInt(args[2]);

		for(double q = 0; q < 1; q+=0.01) {
			CAModel model = new CAModel(width,height,q,growth,firechance);
			run(model, numIter);
		}
	}

}
