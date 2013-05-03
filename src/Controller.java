import java.awt.*;
import java.awt.event.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import com.sun.media.sound.ModelAbstractChannelMixer;


public class Controller extends JFrame implements ActionListener{

	static boolean doPlay = false;
	private JButton playButton, stopButton, resetButton, stepButton, randFireButton;
	private PlotTask plotter;
	private CAModel originalModel;
	
	public Controller(CAModel model){		
		
		originalModel = model.clone();
		plotter = new PlotTask(model);
		
		playButton = makeButton("PLAY");
		stopButton = makeButton("STOP");
		stopButton.setEnabled(false);
		resetButton = makeButton("RESET");
		stepButton = makeButton("STEP");
		randFireButton = makeButton("RANDOM FIRE");
		
		
		JPanel commandPanel = new JPanel();
		commandPanel.add(playButton);
		commandPanel.add(stopButton);
		commandPanel.add(resetButton);
		commandPanel.add(stepButton);
		commandPanel.add(randFireButton);
		
		
		getContentPane().add(commandPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		pack();
	}
	
	private JButton makeButton(String caption) {
		JButton button = new JButton();
		button.setText(caption);
		button.setSize(new Dimension(100,50));
		button.addActionListener(this);
		//getContentPane().add(button);
		return button;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
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
			plotter = new PlotTask(new CAModel(plotter.model.lattice, plotter.model.growthrate));
		} else if(e.getActionCommand() == "RESET") {
			playButton.setEnabled(true);
			stopButton.setEnabled(false);
			stepButton.setEnabled(true);
			plotter.cancel(true);
			plotter = new PlotTask(new CAModel(originalModel.lattice, originalModel.growthrate));
			printLattice(plotter.model.lattice);
		} else if(e.getActionCommand() == "STEP") {
			plotter.model.step();
			printLattice(plotter.model.lattice);
		} else if(e.getActionCommand() == "RANDOM FIRE") {
			plotter.model.setRandomFire();
			printLattice(plotter.model.lattice);
		}
	}
	
	
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
		"set tics (\"empty\" 0, \"tree\" 1, \"fire\" 2) offset 0 2"
	};
	
	private static void initGnuplot() {
		for(String s : gnuplotInitCommands)
			System.out.println(s);
	}

	public static void main(String[] args) {
		CAModel model = new CAModel(200,200,1000, 0.001);
		Controller c = new Controller(model);
		initGnuplot();
		printLattice(model.lattice);
	}

	
	
}
