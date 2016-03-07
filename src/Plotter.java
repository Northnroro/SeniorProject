import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Plotter {
	private float startTime, finishTime, duration;

	private static File prevDirectory = new File("C:/cygwin/home/SeniorProject/data/output/qtdb");

	private JFrame frame;
	private BufferedImage img;
	
	private Signal signal;

	public Plotter(String title, int width, int height, float startTime, float finishTime) {
		// Initialize Variables
		{
			this.startTime = startTime;
			this.finishTime = finishTime;
		}
		// Initialize JFrame
		{
			this.frame = new JFrame(title);
			this.img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Container container = this.frame.getContentPane();
			container.setLayout(new BorderLayout());
			container.add(new JLabel(new ImageIcon(this.img)), BorderLayout.CENTER);
			this.frame.setResizable(false);
			this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.frame.pack();
			this.frame.setVisible(true);
		}
		// Choose Signal
		{
			JFileChooser fc = new JFileChooser(prevDirectory);
			JOptionPane.showMessageDialog(this.frame, "Please choose a signal file.", "Choose Signal",
					JOptionPane.INFORMATION_MESSAGE);
			if (fc.showOpenDialog(this.frame) != JFileChooser.APPROVE_OPTION) {
				JOptionPane.showMessageDialog(this.frame, "No file is selected. The program is closing.",
						"No file selected", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			signal = fc.getSelectedFile();
			prevDirectory = fc.getSelectedFile();
		}
		// Read Signal
		{
			try {
				Scanner sc = new Scanner(this.signalFile);
				this.signalName = sc.nextLine().trim().split("\\s\\s+");
				String signalName2[] = sc.nextLine().trim().split("\\s\\s+");
				this.signalMin = new float[this.signalName.length];
				this.signalMax = new float[this.signalName.length];
				for (int i = 0; i < this.signalName.length; i++) {
					this.signalName[i] = this.signalName[i].trim() + " " + signalName2[i].trim();
					this.signalMax[i] = Float.MIN_VALUE;
					this.signalMin[i] = Float.MAX_VALUE;
				}
				boolean start = false, stop = false;
				for (int i = 0; sc.hasNextFloat() && !stop; i++) {
					Float row[] = new Float[this.signalName.length];
					for (int j = 0; j < this.signalName.length; j++) {
						row[j] = sc.nextFloat();
						if (j == 0) {
							if (row[j] >= this.startTime && !start) {
								start = true;
								this.startTime = row[j];
							} else if (row[j] > this.finishTime && !stop) {
								stop = true;
								this.finishTime = row[j];
								this.duration = this.finishTime - this.startTime;
								break;
							}
						} else {
							this.signalMin[j] = Math.min(this.signalMin[j], row[j]);
							this.signalMax[j] = Math.max(this.signalMax[j], row[j]);
						}
					}
					if (start && !stop) {
						this.signal.add(row);
					}
				}
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(frame, "No such a file. The program is closing.", "File Not Found",
						JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
		this.plot();
	}

	public void smooth() {
		for (int i = 1; i < this.signal.get(0).length; i++) {
			Float temp[] = new Float[this.signal.size()];
			temp[0] = this.signal.get(0)[i];
			temp[this.signal.size() - 1] = this.signal.get(this.signal.size() - 1)[i];
			for (int j = 1; j < this.signal.size() - 1; j++) {
				temp[j] = (this.signal.get(j)[i] + this.signal.get(j - 1)[i] + this.signal.get(j + 1)[i]) / 3;
			}
			Float temps[][] = new Float[this.signal.size()][this.signal.get(0).length];
			for (int j = 0; j < this.signal.size(); j++) {
				for (int k = 0; k < this.signal.get(0).length; k++) {
					if (k == i) {
						temps[j][k] = temp[j];
					} else {
						temps[j][k] = this.signal.get(j)[k];
					}
				}
			}
			for (int j = 0; j < this.signal.size(); j++) {
				this.signal.set(j, temps[j]);
			}
		}
	}

	public void plot() {
		while (true)
			try {
				this.clearPlot();
				// this.paperPlot();
				this.signalPlot();
				this.frame.repaint();
				Thread.sleep(1000);
				// this.smooth();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	public void paperPlot() {
		Graphics2D g = this.img.createGraphics();
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1.5f));
		// g.drawLine(50, this.img.getHeight(), 50, 0);
		if (!square) {
			int w1 = (int) (this.img.getWidth() / 0.04);
			for (int i = 0; i <= w1; i++) {
				int x = (int) ((i * 0.04 - this.startTime) / (this.finishTime - this.startTime) * this.img.getWidth()
						+ 0.5);

				g.drawLine(x, this.img.getHeight(), x, 0);
			}
			w1 = (int) (this.img.getHeight() / 0.04);
			for (int i = 0; i <= w1; i++) {
				int y = (int) ((i * 0.04 - this.startTime) / (this.finishTime - this.startTime) * this.img.getHeight()
						+ 0.5);

				g.drawLine(0, y, this.img.getWidth(), y);
			}
		} else {
			int w1 = (int) (this.img.getWidth() / 0.04);
			for (int i = 0; i <= w1; i++) {
				int x = (int) ((i * 0.04 - this.startTime) / (this.finishTime - this.startTime) * this.img.getWidth()
						+ 0.5);

				g.drawLine(x, this.img.getHeight(), x, 0);
			}
			w1 = (int) (this.img.getHeight() / 0.04);
			for (int i = 0; i <= w1; i++) {
				int y = (int) ((i * 0.04 - this.startTime) / (this.finishTime - this.startTime) * this.img.getWidth()
						+ 0.5);

				g.drawLine(0, y, this.img.getWidth(), y);
			}
		}
	}

	public void clearPlot() {
		Graphics2D g = this.img.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.img.getWidth(), this.img.getHeight());
	}

	public void signalPlot() {
		Graphics2D g = this.img.createGraphics();
		g.setStroke(new BasicStroke(2.0f));
		if (!square) {
			for (int i = 1; i < signalName.length; i++) {
				int prevX = 0, prevY = 0;
				for (int j = 0; j < signal.size(); j++) {
					g.setColor(i == 1 ? Color.RED : Color.BLUE);
					int x = (int) ((signal.get(j)[0] - this.startTime) / (this.finishTime - this.startTime)
							* this.img.getWidth() * 0.8 + this.img.getWidth() * 0.1 + 0.5);
					int y = (int) ((1.0f - (signal.get(j)[i] - signalMin[i]) / (signalMax[i] - signalMin[i]))
							* this.img.getHeight() * 0.8 + this.img.getHeight() * 0.1 + 0.5);
					if (j > 0) {
						g.drawLine(prevX, prevY, x, y);
					}
					g.fillOval(x - 2, y - 2, 4, 4);
					prevX = x;
					prevY = y;
				}
			}
		} else {
			for (int i = 1; i < signalName.length; i++) {
				int prevX = 0, prevY = 0;
				for (int j = 0; j < signal.size(); j++) {
					g.setColor(i == 1 ? Color.RED : Color.BLUE);
					int x = (int) ((signal.get(j)[0] - this.startTime) / (this.finishTime - this.startTime)
							* this.img.getWidth() + 0.5);
					int y = (int) ((1.0f - (signal.get(j)[i] - signalMin[i]) / (signalMax[i] - signalMin[i]))
							* this.img.getHeight() * 0.8 + this.img.getHeight() * 0.1 + 0.5);
					if (j > 0) {
						g.drawLine(prevX, prevY, x, y);
					}
					g.fillOval(x - 2, y - 2, 4, 4);
					prevX = x;
					prevY = y;
				}
			}
		}
	}

	public static void main(String[] args) {
		Plotter p1 = new Plotter("TestECG", 1200, 400, 3.6f, 5.3f);
	}
}
