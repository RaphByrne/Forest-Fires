import java.awt.*;
import java.awt.event.*;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;


public class Controller extends JFrame implements ActionListener{

	static boolean doPlay = false;
	private JButton playButton, stopButton, resetButton, stepButton, randFireButton,
	centreFireButton, saveButton, circleFireButton, lineFireButton;
	private JTextField x1Text, y1Text, x2Text, y2Text, rText;
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
		saveButton = makeButton("SAVE SNAPSHOT");
		circleFireButton = makeButton("CIRCLE FIRE");
		lineFireButton = makeButton("LINE FIRE");
	
		x1Text = new JTextField("y1-coord", 10);
		y1Text = new JTextField("x1-coord", 10);
		x2Text = new JTextField("y2-coord", 10);
		y2Text = new JTextField("x2-coord", 10);
		rText = new JTextField("radius", 10);
		
		
		//put everything in a panel
		JPanel commandPanel = new JPanel();
		commandPanel.add(playButton);
		commandPanel.add(stopButton);
		commandPanel.add(resetButton);
		commandPanel.add(stepButton);
		commandPanel.add(saveButton);	


		JPanel modifierPanel = new JPanel();
		modifierPanel.add(randFireButton);
		modifierPanel.add(centreFireButton);

		JPanel textPanel = new JPanel();
		textPanel.add(y1Text);
		textPanel.add(x1Text);
		textPanel.add(y2Text);
		textPanel.add(x2Text);
		textPanel.add(rText);

		JPanel shapesPanel = new JPanel();
		shapesPanel.add(circleFireButton);
		shapesPanel.add(lineFireButton);
	
		//more swing BS
		getContentPane().add(commandPanel, BorderLayout.PAGE_START);
		getContentPane().add(modifierPanel, BorderLayout.LINE_START);
		getContentPane().add(textPanel, BorderLayout.CENTER);
		getContentPane().add(shapesPanel, BorderLayout.LINE_END);
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

	private void saveSnapshot(CAModel model) {
		System.out.println("set t png");
		System.out.println("set output '| display -write model" + model.width + "," + model.height + "," + model.growthrate + "," + model.lighteningChance + "," + model.tics + ".png'");
		printLattice(model.lattice);
		System.out.println("set t x11");
		System.out.println("set output 'STDOUT'");
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
			plotter = new PlotTask(plotter.model.clone());
		} else if(e.getActionCommand() == "RESET") {
			playButton.setEnabled(true);
			stopButton.setEnabled(false);
			stepButton.setEnabled(true);
			plotter.cancel(true);
			plotter = new PlotTask(originalModel.clone());
			printLattice(plotter.model.lattice);
		} else if(e.getActionCommand() == "STEP") {
			plotter.model.step();
			printLattice(plotter.model.lattice);
		} else if(e.getActionCommand() == "RANDOM FIRE") {
			if(!plotter.isCancelled())
				plotter.cancel(true);
			plotter.model.setRandomFire();
			printLattice(plotter.model.lattice);
			plotter = new PlotTask(plotter.model.clone());	
		} else if(e.getActionCommand() == "SET FIRE AT CENTRE") {
			if(!plotter.isCancelled())
				plotter.cancel(true);
			plotter.model.setFireCentre();
			printLattice(plotter.model.lattice);
			plotter = new PlotTask(plotter.model.clone());	
		} else if(e.getActionCommand() == "SAVE SNAPSHOT") {
			if(!plotter.isCancelled())
				plotter.cancel(true);
			saveSnapshot(plotter.model);
			plotter = new PlotTask(plotter.model.clone());	
		} else if(e.getActionCommand() == "CIRCLE FIRE") {
			if(!plotter.isCancelled())
				plotter.cancel(true);
			plotter.model.setFireCircle(Double.parseDouble(rText.getText()),Integer.parseInt(x1Text.getText()),Integer.parseInt(y1Text.getText()));
			printLattice(plotter.model.lattice);
			plotter = new PlotTask(plotter.model.clone());	
		} else if(e.getActionCommand() == "LINE FIRE") {
			if(!plotter.isCancelled())
				plotter.cancel(true);
			plotter.model.setFireLine(Integer.parseInt(x1Text.getText()),Integer.parseInt(y1Text.getText()),Integer.parseInt(x2Text.getText()),Integer.parseInt(y2Text.getText()));
			printLattice(plotter.model.lattice);
			plotter = new PlotTask(plotter.model.clone());	
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
		"set palette model RGB defined (0 'black', 0.99 'black', 1 'green', 1.80 'green', 2 '#FF0000', 2.99 '#FF0000')",
		"set cbtics ('empty' 0, 'tree' 1, 'fire' 2) offset 0 2",
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
		if(args.length < 5) {
			System.err.println("Arguments are: width height initTreeChance growthRate igniteChance");
			System.err.println("Extensions are: <default args> burnResist windX windY");
			System.exit(0);
		}
		if(args.length > 8) {
			System.err.println("Extensions are: <default args> burnResist windX windY");
			System.exit(0);
		}
		
		//could do some more argument checking here but I can't be bothered	
		int width = Integer.parseInt(args[0]);
		int height = Integer.parseInt(args[1]);
		double q = Double.parseDouble(args[2]);
		double growthRate = Double.parseDouble(args[3]);
		double igniteChance = Double.parseDouble(args[4]);
		double burnResist = -1;
		double windX = 0;
		double windY = 0;
		if(args.length >= 6)
			burnResist = Double.parseDouble(args[5]);
		if(args.length == 8) {
			windX = Double.parseDouble(args[6]);
			windY = Double.parseDouble(args[7]);
		}
			
		CAModel model = new CAModel(width,height,q, growthRate, igniteChance, burnResist, windX, windY); 
		Controller c = new Controller(model);
		initGnuplot();
		printLattice(model.lattice);
	}

	
	
}
