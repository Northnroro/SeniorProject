import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

public class Main {

	protected static File prevDirectory = System.getProperty("user.name").equals("DELL WIN7")
			? new File("C:/cygwin/home/SeniorProject/data/output") : new File("C:/cygwin64/home/GS60/data");

	private JFrame frame;

	public Main() {

		// Initialize JFrame
		{
			System.out.println("Username : " + System.getProperty("user.name"));
			this.frame = new JFrame("ECG Label");
			Container container = this.frame.getContentPane();
			GridLayout grid = new GridLayout(2, 0, 0, 0);
			container.setLayout(grid);
			JButton bSave = new JButton("Stretch and Save");
			JButton bPlot = new JButton("Plot a big ECG");
			JButton bPlotSm = new JButton("Plot a small ECG");
			JButton bSplit = new JButton("Split and Add to DB");
			JButton bAnnotate = new JButton("Annotate signal");
			JButton bBestMatch = new JButton("Find best match");
			JButton bGenReport = new JButton("Make a report");
			bSave.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					stretchAndSave();
				}
			});
			bSplit.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("Add more database is clicked: callSplit");
					callSplit();
				}
			});
			bPlot.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("Plot an ECG is clicked: callPlot");
					callPlot();
				}
			});
			bPlotSm.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("Plot an ECG is clicked: callPlotSmall");
					callPlotSmall();
				}
			});
			bAnnotate.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("Annotate Signal is clicked: labeling");
					labeling();
				}
			});
			bBestMatch.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					findBestMatch();
				}
			});
			bGenReport.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					genReport();
				}
			});
			container.add(bSave);
			container.add(bPlot);
			container.add(bPlotSm);
			container.add(bSplit);
			container.add(bAnnotate);
			container.add(bBestMatch);
			container.add(bGenReport);
			this.frame.setResizable(false);
			this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.frame.pack();
			this.frame.setLocation(0,
					(int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() - this.frame.getHeight() - 50));
			this.frame.setVisible(true);
		}
	}

	public void stretchAndSave() {
		File f = chooseSignal();
		if (f == null)
			return;
		Signal signal = new Signal(f, 15f, 16f, true);
		signal.stretch(Signal.BIN_SIZE);
		signal.signalFile = new File(Signal.path
				+ signal.signalFile.getName().substring(0, signal.signalFile.getName().length() - 4) + "+.txt");
		signal.save();
		System.out.println("DONE");
	}

	public void findBestMatch() {
		File f = chooseSignal();
		if (f == null)
			return;
		Signal signal = new Signal(f, 0.0f, Float.MAX_VALUE, true);
		System.out.println("Best Match >> " + signal.findNearestSignal().signalFile.getName());

	}

	public void callPlot() {
		File f = chooseSignal();
		if (f == null)
			return;
		// Plotter p = new Plotter("PlottingECG -- " + f.getName(), f, 1600,
		// 600, 1.39f, 6f);
		Plotter p = new Plotter("PlottingECG -- " + f.getName(), f, 1600, 600, 93f, 98f);
		// Plotter p = new Plotter("PlottingECG -- " + f.getName(), f, 1600,
		// 600, 0f, Float.MAX_VALUE);
		p.plot();
	}

	public void callPlotSmall() {
		File[] fs = chooseManySignals(new FileFilter() {
			@Override
			public String getDescription() {
				return "Signal files (*.txt)";
			}

			@Override
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().length() >= 5 && f.getName().indexOf(".txt") == f.getName().length() - 4
								&& Character.isDigit(f.getName().charAt(f.getName().length() - 5));
			}
		});
		if (fs != null && fs.length > 1) {
			for (File f : fs) {
				Plotter p = new Plotter("PlottingECG -- " + f.getName(), f, 1100, 600, 0f, Float.MAX_VALUE);
				p.plot();
				p.writePrepare = true;
				p.markerPlot();
				p.frame.dispose();
				System.out.println(f.getName() + "\'s Result Done");
			}
			System.out.println("All Results Done");
		} else {
			if (fs == null)
				return;
			System.out.println("Prepare for plotting...");
			Plotter p = new Plotter("PlottingECG -- " + fs[0].getName(), fs[0], 1100, 600, 0f, Float.MAX_VALUE);
			p.plot();
		}
	}

	public static void callPlotSmall(File f) {
		if (f == null)
			return;
		// Plotter p = new Plotter("PlottingECG -- " + f.getName(), f, 1600,
		// 600, 1.39f, 6f);
		// Plotter p = new Plotter("PlottingECG -- " + f.getName(), f, 1600,
		// 600, 93f, 98f);
		Plotter p = new Plotter("PlottingECG -- " + f.getName(), f, 1100, 600, 0f, Float.MAX_VALUE);
		p.plot();
	}

	public void callSplit() {
		File[] fs = chooseManySignals();
		if (fs == null)
			return;
		System.out.println("Preparing to split...");
		for (File f : fs) {
			Signal s = new Signal(f, 0.0f, Float.MAX_VALUE, true);
			s.split();
		}
	}

	public void labeling() {
		File[] fs = chooseManySignals(new FileFilter() {
			@Override
			public String getDescription() {
				return "Signal files (*.txt)";
			}

			@Override
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().length() >= 5 && f.getName().indexOf(".txt") == f.getName().length() - 4
								&& Character.isDigit(f.getName().charAt(f.getName().length() - 5));
			}
		});
		if (fs == null)
			return;
		int count = 0;
		String errMsg = "";
		for (File f : fs) {
			System.out.println("Labeling " + f.getName() + " ... (" + count + "/" + fs.length + ")");
			try {
				Signal s = new Signal(f, 0, Float.MAX_VALUE, true);
				s.labelAll();
			} catch (Exception e) {
				System.err.println("ERROR - " + f.getPath());
				errMsg += "ERROR - " + f.getPath() + "\n";
			}
			count++;
			System.out.println(f.getName() + " Labeled! (" + count + "/" + fs.length + ")");
		}
		System.out.println("DONE - All signals have been labeled!");
		if (errMsg.length() > 0) {
			System.err.println(errMsg);
		}
	}

	public File[] chooseManySignals() {
		JFileChooser fc = new JFileChooser(prevDirectory);
		fc.setMultiSelectionEnabled(true);
		if (fc.showOpenDialog(this.frame) != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		prevDirectory = fc.getSelectedFiles()[0];
		return fc.getSelectedFiles();
	}

	public File[] chooseManySignals(FileFilter ff) {
		JFileChooser fc = new JFileChooser(prevDirectory);
		fc.setMultiSelectionEnabled(true);
		fc.setFileFilter(ff);
		if (fc.showOpenDialog(this.frame) != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		prevDirectory = fc.getSelectedFiles()[0];
		return fc.getSelectedFiles();
	}

	public File chooseSignal() {
		JFileChooser fc = new JFileChooser(prevDirectory);
		// JOptionPane.showMessageDialog(this.frame,
		// "Please choose a Signal file.", "Choose Signal",
		// JOptionPane.INFORMATION_MESSAGE);
		// System.out.println();
		if (fc.showOpenDialog(this.frame) != JFileChooser.APPROVE_OPTION) {
			// JOptionPane.showMessageDialog(this.frame,
			// "No file is selected. The program is closing.",
			// "No file selected", JOptionPane.ERROR_MESSAGE);
			// System.exit(0);
			return null;
		}
		// signal = new Signal(fc.getSelectedFile(), 0, 0, true);
		// annotation = signal.annotation;
		prevDirectory = fc.getSelectedFile();
		return prevDirectory;
	}

	public void genReport() {
		File[] fs = chooseManySignals(new FileFilter() {
			@Override
			public String getDescription() {
				return "Result files (*result.txt)";
			}

			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().indexOf("result.txt") >= 0
						&& f.getName().indexOf("result.txt") == f.getName().length() - 10;
			}
		});
		if (fs == null)
			return;
		String errMsg = "";
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<ArrayList<Float>> overlaps = new ArrayList<ArrayList<Float>>();
		ArrayList<ArrayList<ArrayList<Integer>>> upPercents = new ArrayList<ArrayList<ArrayList<Integer>>>();
		for (File f : fs) {
			System.out.println("Using " + f.getName() + " ...");
			try {
				Scanner s = new Scanner(f);
				names.add(f.getName());
				ArrayList<Float> overlap = new ArrayList<Float>();
				overlaps.add(overlap);
				for (int i = 0; i < Signal.SIGNALS.length; i++) {
					float val = s.nextFloat();
					if (Float.isNaN(val))
						val = 0.0f;
					overlap.add(val);
				}
				ArrayList<ArrayList<Integer>> upPercent = new ArrayList<ArrayList<Integer>>();
				upPercents.add(upPercent);
				while (s.hasNextInt()) {
					s.nextInt();
					ArrayList<Integer> each = new ArrayList<Integer>();
					upPercent.add(each);
					for (int i = 0; i < Signal.SIGNALS.length; i++) {
						each.add(s.nextInt());
					}
				}
				s.close();
			} catch (Exception e) {
				System.err.println("ERROR - " + f.getPath());
				errMsg += "ERROR - " + f.getPath() + "\n";
			}
		}
		try {
			PrintWriter p = new PrintWriter(
					new File(fs[0].getPath().substring(0, fs[0].getPath().lastIndexOf('\\')) + "\\" + "result.csv"));
			p.print("File,");
			for (String n : names)
				p.print(n + ",,,");
			p.println("Total,,,,Average,");
			p.print("Signal,");
			for (@SuppressWarnings("unused")
			String n : names)
				for (String s : Signal.SIGNALS)
					p.print(s + ",");
			for (String s : Signal.SIGNALS)
				p.print(s + ",");
			p.print("All,");
			for (String s : Signal.SIGNALS)
				p.print(s + ",");
			p.println("All");
			p.print("Average " + ",");
			float SUM = 0.0f;
			float sum[] = new float[Signal.SIGNALS.length];
			int COUNTPERCENT[][] = new int[upPercents.get(0).size()][Signal.SIGNALS.length + 1];
			for (int i = 0; i < overlaps.size(); i++) {
				for (int j = 0; j < Signal.SIGNALS.length; j++) {
					p.print(overlaps.get(i).get(j) + ",");
					sum[j] += overlaps.get(i).get(j);
					SUM += overlaps.get(i).get(j);
					for (int k = 0; k < upPercents.get(0).size(); k++) {
						if (overlaps.get(i).get(j) >=  k * 5) {
							COUNTPERCENT[k][j]++;
							COUNTPERCENT[k][Signal.SIGNALS.length]++;
						}
					}
				}
			}
			for (int j = 0; j < Signal.SIGNALS.length; j++)
				p.print(String.format("%.2f,", sum[j] / overlaps.size()));
			p.println(String.format("%.2f", SUM / overlaps.size() / Signal.SIGNALS.length));
			int count0[] = new int[Signal.SIGNALS.length];
			int COUNT0 = 0;
			for (int i = 0; i < upPercents.get(0).size(); i++) {
				int percent = i * 5;
				p.print(">=" + percent + "%,");
				int count[] = new int[Signal.SIGNALS.length];
				int COUNT = 0;
				for (ArrayList<ArrayList<Integer>> up : upPercents) {
					for (int j = 0; j < Signal.SIGNALS.length; j++) {
						count[j] += up.get(i).get(j);
						COUNT += up.get(i).get(j);
						p.print(String.format("%.2f", up.get(i).get(j) * 100f / up.get(0).get(j)) + ",");
					}
				}
				for (int j = 0; j < Signal.SIGNALS.length; j++) {
					if (i == 0)
						count0[j] = count[j];
					p.print(String.format("%.2f", count[j] * 100f / count0[j]) + ",");
				}
				if (i == 0)
					COUNT0 = COUNT;
				p.print(String.format("%.2f", COUNT * 100f / COUNT0) + ",");
				for (int j = 0; j < COUNTPERCENT[i].length; j++) {
					p.print(String.format("%.2f", COUNTPERCENT[i][j] * 100f / COUNTPERCENT[0][j]) + ",");
				}
				p.println();
			}
			p.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("DONE - All results have been used to make a report!");
		if (errMsg.length() > 0) {
			System.err.println(errMsg);
		}
	}

	public static void main(String[] args) {
		new Main();
	}

}
