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
	private static void run(CAModel model) {
		System.out.print(model.q + " ");
		model.setFireCentre();
		int i = 0;
		while(model.numFires > 0) {
			model.step();
			i++;
		}
		System.out.println(i);
	}	

	public static void main(String[] args) {
		if(args.length < 2) {
			System.out.println("Arguments are: width height");
			System.exit(0);
		}
		

		int width = Integer.parseInt(args[0]);
		int height = Integer.parseInt(args[1]);
		double growth = 0;
		double firechance = 0;

		for(double q = 0; q < 1; q+=0.01) {
			CAModel model = new CAModel(width,height,q,growth,firechance);
			run(model);
		}
	}

}
