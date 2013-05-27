import java.awt.*;
import java.awt.event.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;


public class Controller extends JFrame implements ActionListener{

	static boolean doPlay = false;
	private JButton playButton, stopButton, resetButton, stepButton, randFireButton,
	centreFireButton;
	private PlotTask plotter;
	private CAModel originalModel;
	
	/**
 	* Makes a new controller from an existing model
 	* @param model the model to control
 	*/ 
	public Controller(CAModel model){		
		
		originalModel = model.clone();
		plotter = new PlotTask(model);
		
		//SWING BULLCRAP BELOW
		//set up al the buttons
		playButton = makeButton("PLAY");
		stopButton = makeButton("STOP");
		stopButton.setEnabled(false);
		resetButton = makeButton("RESET");
		stepButton = makeButton("STEP");
		randFireButton = makeButton("RANDOM FIRE");
		centreFireButton = makeButton("SET FIRE AT CENTRE");
		
		//put everything in a panel
		JPanel commandPanel = new JPanel();
		commandPanel.add(playButton);
		commandPanel.add(stopButton);
		commandPanel.add(resetButton);
		commandPanel.add(stepButton);
		commandPanel.add(randFireButton);
		commandPanel.add(centreFireButton);
		
		//more swing BS
		getContentPane().add(commandPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		pack();
	}
	
	/**
 	* Helper function for button making
 	*/ 
	private JButton makeButton(String caption) {
		JButton button = new JButton();
		button.setText(caption);
		button.setSize(new Dimension(100,50));
		button.addActionListener(this); //makes actionPerformed work
		//getContentPane().add(button);
		return button;
	}

	/**
 	* Captures the button actions and calls model methods
 	*/ 	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand() == "PLAY") {
			playButton.setEnabled(false);
			stopButton.setEnabled(true);
			stepButton.setEnabled(false);
			plotter.execute();
		} else if(e.getActionCommand() == "STOP") {
			playButton.setEnabled(true);
			stopButton.setEnabled(false);
			stepButton.setEnabled(true);
			plotter.cancel(true);
			plotter = new PlotTask(new CAModel(plotter.model.lattice, plotter.model.growthrate, plotter.model.lighteningChance));
		} else if(e.getActionCommand() == "RESET") {
			playButton.setEnabled(true);
			stopButton.setEnabled(false);
			stepButton.setEnabled(true);
			plotter.cancel(true);
			plotter = new PlotTask(new CAModel(originalModel.lattice, originalModel.growthrate, originalModel.lighteningChance));
			printLattice(plotter.model.lattice);
		} else if(e.getActionCommand() == "STEP") {
			plotter.model.step();
			printLattice(plotter.model.lattice);
		} else if(e.getActionCommand() == "RANDOM FIRE") {
			plotter.model.setRandomFire();
			printLattice(plotter.model.lattice);
		} else if(e.getActionCommand() == "SET FIRE AT CENTRE") {
			plotter.model.setFireCentre();
			printLattice(plotter.model.lattice);
		}
	}
	
	
	/**
 	* Required to have a task run in the background and still respond to swing GUI events
 	*/ 
	private class PlotTask extends SwingWorker<Void, CAModel> {

		CAModel model;
		
		public PlotTask(CAModel model) {
			this.model = model;
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			while(!isCancelled()) {
				model.step();
				printLattice(model.lattice);
			}
			return null;
		}
		
		/*
		@Override
		protected void process(List<CAModel> )
		*/
	}
	
	public static void printLattice(int[][] lattice) {
		System.out.println("plot '-' matrix with image");
		for(int i = 0; i < lattice.length; i++) {
			for(int j = 0; j < lattice[0].length; j++) {
				System.out.print(lattice[i][j] + " ");
			}
			System.out.print("\n");
		}
		System.out.println("e");
		System.out.println("e");
	}
	
	static String[] gnuplotInitCommands = {
		"set term x11 size 1000,800",
		"set size ratio 1",
		"set cbrange [0:3]",
		"set palette model RGB defined (0 'black', 0.99 'black', 1 'green', 1.99 'green', 2 'red', 2.99 'red')",
		"set cbtics (\"empty\" 0, \"tree\" 1, \"fire\" 2) offset 0 2",
		"set key at graph 1,1 bottom Right reverse",
		"set xtics out -200,10,200",
		"set ytics out -200,10,200",
		"set x2tics -200.5,1,200.5 format \"\"",
		"set y2tics -200.5,1,200.5 format \"\"",
		"set grid noxtics noytics x2tics y2tics front linetype -1"
	};
	
	private static void initGnuplot() {
		for(String s : gnuplotInitCommands)
			System.out.println(s);
	}

	public static void main(String[] args) {
		//CAModel model = new CAModel(200,200,0.05, 0.3, 0.00006);
		//CAModel model = new CAModel(100,100,0.05, 0.003, 0.00006, 0.20); 
		//CAModel model =  new CAModel(100, 100, 0.08, 0.005, 0.000000001, 0.3); 
		CAModel model = new CAModel(100,100,0.05, 0.3, 0.00006, 0, -1, 0, 1); 
		Controller c = new Controller(model);
		initGnuplot();
		printLattice(model.lattice);
	}

	
	
}
