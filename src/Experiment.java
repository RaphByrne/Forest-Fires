public class Experiment {

	private static void printStats(CAModel model) {
		System.out.println(model.numEmpty + " " + model.numTrees + " " + model.numFires);
	}

	private static void run(CAModel model, int numIter) {
		printStats(model);
		for(int i = 0; i < numIter; i++) {
			model.step();
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
