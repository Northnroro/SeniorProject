import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Plotter {

	protected JFrame frame;
	private BufferedImage img;

	private Signal signal;
	private Annotation annotation;

	private BufferedImage cacheImg;
	private Float cursorTime;
	private boolean cursorChoose;
	private boolean cursorChose;
	private boolean write = false;
	protected boolean writePrepare = false;

	private int signalType = 0;

	private float originalStartTime, originalFinishTime;

	public Plotter(String title, final Signal s, int width, int height, float startTime, float finishTime) {
		signal = s;
		annotation = s.annotation;
		originalStartTime = s.startTime;
		originalFinishTime = s.finishTime;
		s.finishTime = Math.min(s.startTime + 2f, s.finishTime);
		this.frame = new JFrame(title);
		this.img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		this.cacheImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Container container = this.frame.getContentPane();
		container.setLayout(new BorderLayout());
		JLabel jlb = new JLabel(new ImageIcon(this.img));
		container.add(jlb, BorderLayout.CENTER);
		JPanel p = new JPanel(new GridLayout(0, 15));
		container.add(p, BorderLayout.NORTH);
		JButton back10 = new JButton("<<");
		JButton back = new JButton("<");
		JButton save = new JButton("SAVE");
		JButton next = new JButton(">");
		JButton next10 = new JButton(">>");
		JButton mark = new JButton("Mark");
		p.add(back10);
		p.add(back);
		p.add(save);
		p.add(next);
		p.add(next10);
		for (int i = 0; i < 9; i++) {
			p.add(new JLabel(""));
		}
		p.add(mark);
		next.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String path = s.signalFile.getPath();
				path = path.substring(0, path.lastIndexOf('.'));
				String num = "";
				for (int i = path.length() - 1; i >= 0 && path.charAt(i) >= '0' && path.charAt(i) <= '9'; i--) {
					num = path.charAt(i) + num;
					path = path.substring(0, path.length() - 1);
				}
				path += Integer.parseInt(num) + 1 + ".txt";
				Main.callPlotSmall(new File(path));
				frame.dispose();
			}
		});
		next10.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String path = s.signalFile.getPath();
				path = path.substring(0, path.lastIndexOf('.'));
				String num = "";
				for (int i = path.length() - 1; i >= 0 && path.charAt(i) >= '0' && path.charAt(i) <= '9'; i--) {
					num = path.charAt(i) + num;
					path = path.substring(0, path.length() - 1);
				}
				path += Integer.parseInt(num) + 10 + ".txt";
				Main.callPlotSmall(new File(path));
				frame.dispose();
			}
		});
		back.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String path = s.signalFile.getPath();
				path = path.substring(0, path.lastIndexOf('.'));
				String num = "";
				for (int i = path.length() - 1; i >= 0 && path.charAt(i) >= '0' && path.charAt(i) <= '9'; i--) {
					num = path.charAt(i) + num;
					path = path.substring(0, path.length() - 1);
				}
				path += Integer.parseInt(num) - 1 + ".txt";
				System.out.println(path);
				Main.callPlotSmall(new File(path));
				frame.dispose();
			}
		});
		back10.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String path = s.signalFile.getPath();
				path = path.substring(0, path.lastIndexOf('.'));
				String num = "";
				for (int i = path.length() - 1; i >= 0 && path.charAt(i) >= '0' && path.charAt(i) <= '9'; i--) {
					num = path.charAt(i) + num;
					path = path.substring(0, path.length() - 1);
				}
				path += Integer.parseInt(num) - 10 + ".txt";
				System.out.println(path);
				Main.callPlotSmall(new File(path));
				frame.dispose();
			}
		});
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File original = s.signalFile;
				s.signalFile = new File(Signal.selectPath + "/" + s.signalFile.getName());
				s.save();
				s.signalFile = original;
			}
		});
		mark.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = signal.signalFile.getPath();
				signal.marker.save(new File(name.substring(0, name.length() - 4) + "answer.txt"));
				System.out.println("Marker Saved! (" + signal.marker.times.size() + ")");
				writePrepare = true;
				partialPlot();
			}
		});
		JPanel p2 = new JPanel(new GridLayout(2, 1));
		container.add(p2, BorderLayout.SOUTH);
		final JScrollBar sb = new JScrollBar(JScrollBar.HORIZONTAL, (int) (s.startTime * 10),
				(int) ((s.finishTime - s.startTime) * 10), (int) (originalStartTime * 10),
				(int) (originalFinishTime * 10));
		JSlider slider = new JSlider(0 * 10, (int) ((originalFinishTime - originalStartTime) * 10),
				(int) ((originalFinishTime - originalStartTime - s.finishTime + s.startTime) * 10));
		slider.setMajorTickSpacing(100);
		slider.setMinorTickSpacing(10);
		slider.setPaintTicks(true);
		p2.add(sb, BorderLayout.CENTER);
		p2.add(slider, BorderLayout.SOUTH);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				float val = ((JSlider) e.getSource()).getValue() / 10f;
				s.finishTime = -val + s.startTime + originalFinishTime - originalStartTime;
				if (s.finishTime > originalFinishTime) {
					s.startTime -= s.finishTime - originalFinishTime;
					s.finishTime = originalFinishTime;
				}
				sb.setValue((int) (s.startTime * 10));
				sb.setVisibleAmount((int) ((s.finishTime - s.startTime) * 10));
				plot();
			}
		});
		sb.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				float val = ((JScrollBar) e.getSource()).getValue() / 10f;
				s.finishTime = val + s.finishTime - s.startTime;
				s.startTime = val;
				plot();
			}
		});
		jlb.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				float val = (e.getX() - img.getWidth() / 10) * (signal.finishTime - signal.startTime)
						/ (img.getWidth() * 8 / 10) + signal.startTime;
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (e.getY() > img.getHeight() / 10 && e.getY() < img.getHeight() * 9 / 10) {
						signal.marker.times.add(val);
					} else {
						cursorChose = true;
						for (int i = 0; i < signal.marker.signals.size(); i++) {
							TreeSet<Float> ts = signal.marker.signals.get(i);
							if (signal.marker.times.floor(val) != null && signal.marker.times.ceiling(val) != null
									&& ts.ceiling(signal.marker.times.floor(val)) != null
									&& ts.ceiling(signal.marker.times.floor(val))
											.equals(ts.floor(signal.marker.times.ceiling(val)))) {
								ts.remove(ts.ceiling(signal.marker.times.floor(val)));
								cursorChose = false;
							}
						}
						if (cursorChose && signal.marker.times.floor(val) != null
								&& signal.marker.times.ceiling(val) != null) {
							signal.marker.signals.get(signalType).add(val);
							signalType = (signalType + 1) % Signal.SIGNALS.length;
							System.out.println(
									findPercent(signal.marker.times.floor(val), signal.marker.times.ceiling(val)));
						}
					}
				} else {
					if (e.getY() > img.getHeight() / 10 && e.getY() < img.getHeight() * 9 / 10) {
						Float min = signal.marker.times.floor(val);
						Float max = signal.marker.times.ceiling(val);
						if (min != null && max != null) {
							if (val - min < max - val) {
								val = min;
							} else {
								val = max;
							}
						} else if (min != null) {
							val = min;
						} else if (max != null) {
							val = max;
						}
						signal.marker.times.remove(val);
						min = signal.marker.times.floor(val);
						max = signal.marker.times.ceiling(val);
						if (min != null && max != null) {
							for (int i = 0; i < signal.marker.signals.size(); i++) {
								TreeSet<Float> ts = signal.marker.signals.get(i);
								if (ts.ceiling(min) != null && ts.floor(max) != null
										&& ts.ceiling(min).equals(ts.floor(max))) {
									ts.remove(ts.floor(max));
								}
							}
						} else {
							if (min == null && max != null) {
								for (int i = 0; i < signal.marker.signals.size(); i++) {
									TreeSet<Float> ts = signal.marker.signals.get(i);
									if (ts.floor(max) != null)
										ts.remove(ts.floor(max));
								}
							}
							if (max == null && min != null) {
								for (int i = 0; i < signal.marker.signals.size(); i++) {
									TreeSet<Float> ts = signal.marker.signals.get(i);
									if (ts.ceiling(min) != null)
										ts.remove(ts.ceiling(min));
								}
							}
						}
					} else {
						signalType = (signalType + 1) % Signal.SIGNALS.length;
					}
				}
				partialPlot();
			}
		});
		jlb.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				cursorChoose = !(e.getY() > img.getHeight() / 10 && e.getY() < img.getHeight() * 9 / 10
						&& e.getX() > img.getWidth() / 10 && e.getX() < img.getWidth() * 9 / 10);
				cursorTime = (e.getX() - img.getWidth() / 10) * (signal.finishTime - signal.startTime)
						/ (img.getWidth() * 8 / 10) + signal.startTime;
				cursorChose = false;
				partialPlot();
			}
		});
		this.frame.setResizable(false);
		this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.frame.pack();
		this.frame.setVisible(true);
	}

	public Plotter(String title, File f, int width, int height, float startTime, float finishTime) {
		// initialize frame
		this(title, new Signal(f, startTime, finishTime, true), width, height, startTime, finishTime);
		// this.frame = new JFrame(title);
		// this.img = new BufferedImage(width, height,
		// BufferedImage.TYPE_INT_ARGB);
		// Container container = this.frame.getContentPane();
		// container.setLayout(new BorderLayout());
		// container.add(new JLabel(new ImageIcon(this.img)),
		// BorderLayout.CENTER);
		// this.frame.setResizable(false);
		// this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// this.frame.pack();
		// this.frame.setVisible(true);
		//
		// signal = new Signal(f, startTime, finishTime, true);
		// annotation = signal.annotation;
		// originalStartTime = signal.startTime;
		// originalFinishTime = signal.finishTime;
		// plot(); // NEVER COMMENT THIS
		// annotation.insertR(signal, signal.findR()); // NEVER UNCOMMENT THIS
		// signal.split(); // UNCOMMENT THIS IF WANT TO SPLIT
		// System.out.println("Best Match >> "
		// + signal.findNearestSignal().signalFile.getName());
	}

	public void plot() {
		while (true)
			try {
				this.clearPlot();
				this.annotationPlot();
				// System.out.println(1);
				this.paperPlot();
				// System.out.println(2);
				this.signalPlot();
				// System.out.println(3);
				// this.envPlot();
				// System.out.println(4);
				this.rPlot();
				// System.out.println(5);
				this.intervalPlot();
				// this.markerPlot();
				cacheImg.createGraphics().drawImage(img, 0, 0, null);
				this.partialPlot();
				this.frame.repaint();
				// System.out.println(6);
				Thread.sleep(20);
				break;
				// signal.smooth();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	public void partialPlot() {
		try {
			img.createGraphics().drawImage(cacheImg, 0, 0, null);
			markerPlot();
			frame.repaint();
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void clearPlot() {
		Graphics2D g = this.img.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.img.getWidth(), this.img.getHeight());
	}

	public void annotationPlot() {
		Graphics2D g = this.img.createGraphics();
		boolean foundOpenParentheses = false, foundCloseParentheses = true;
		float lft = signal.startTime;
		float rght = 0.0f;
		float prevBeat = -1f;
		for (int i = 0; i < annotation.annotation.size(); i++) {
			if (signal.annotation.annotation.get(i)[0] - signal.startTime < 0
					|| signal.annotation.annotation.get(i)[0] > signal.finishTime) {
				if (annotation.annotation.get(i)[4] == 0) {
					if (annotation.annotation.get(i)[2] == 0) {
						lft = annotation.annotation.get(i)[0];
					}
				}
				continue;
			}
			if (annotation.annotation.get(i)[4] == 0) { // p=1, t=3, N=2, (=0,
														// )=4, |=9, ^=8
				if (annotation.annotation.get(i)[2] == 0) {
					lft = annotation.annotation.get(i)[0];
					foundOpenParentheses = true;
					foundCloseParentheses = false;
				} else if (annotation.annotation.get(i)[2] == 1)
					g.setColor(new Color(255, 0, 0, 50));
				else if (annotation.annotation.get(i)[2] == 2)
					g.setColor(new Color(0, 255, 0, 50));
				else if (annotation.annotation.get(i)[2] == 3)
					g.setColor(new Color(0, 0, 255, 50));
				else if (annotation.annotation.get(i)[2] == 4) {
					foundCloseParentheses = true;
					if (!foundOpenParentheses)
						g.setColor(new Color(0, 255, 0, 50));
					rght = annotation.annotation.get(i)[0];
					rght = translate(rght, 0).x;
					lft = translate(lft, 0).x;
					g.fillRect((int) lft, (int) (0.1 * img.getHeight()), (int) (rght - lft),
							(int) (img.getHeight() * 0.8));
				}
				if (annotation.annotation.get(i)[2] == 9) {
					if (prevBeat >= 0) {
						lft = translate(prevBeat, 0).x;
						rght = translate(annotation.annotation.get(i)[0], 0).x;
						g.setColor(new Color(255, 200, 0, 50));
						g.fillRect((int) lft, (int) (0.1 * img.getHeight()), (int) (rght - lft),
								(int) (img.getHeight() * 0.8));
						g.setStroke(new BasicStroke(2.0f));
						g.drawLine((int) lft, 0, (int) lft, img.getHeight());
						g.drawLine((int) rght, 0, (int) rght, img.getHeight());
						g.setStroke(new BasicStroke(1.0f));
						// for (int j = 0; j <= (int) (img.getHeight() * 0.8 /
						// 4); j++) {
						// int y = (int) (j * 4 + img.getHeight() * 0.1);
						// g.drawLine((int) lft, y, (int) rght, y);
						// }
						// for (int j = 0; j < (int) ((rght - lft) / 4); j++) {
						// int x = (int) (lft + j * 4);
						// g.drawLine(x, img.getHeight() / 10, x,
						// img.getHeight() * 9 / 10);
						// }
						prevBeat = -1f;
					} else {
						prevBeat = annotation.annotation.get(i)[0];
					}
				}
			}
		}
		if (!foundCloseParentheses) {
			g.setColor(new Color(0, 255, 0, 50));
			rght = translate(signal.finishTime, 0).x;
			lft = translate(lft, 0).x;
			g.fillRect((int) lft, (int) (0.1 * img.getHeight()), (int) (rght - lft), (int) (img.getHeight() * 0.8));
		}
		if (prevBeat >= 0) {
			lft = translate(prevBeat, 0).x;
			rght = translate(signal.finishTime, 0).x;
			g.setColor(new Color(255, 200, 0, 20));
			g.fillRect((int) lft, (int) (0.1 * img.getHeight()), (int) (rght - lft), (int) (img.getHeight() * 0.8));
			g.setStroke(new BasicStroke(2.0f));
			g.drawLine((int) lft, 0, (int) lft, img.getHeight());
			g.setStroke(new BasicStroke(1.0f));
			for (int j = 0; j <= (int) (img.getHeight() * 0.8 / 4); j++) {
				int y = (int) (j * 4 + img.getHeight() * 0.1);
				g.drawLine((int) lft, y, (int) rght, y);
			}
			for (int j = 0; j < (int) ((rght - lft) / 4); j++) {
				int x = (int) (lft + j * 4);
				g.drawLine(x, img.getHeight() / 10, x, img.getHeight() * 9 / 10);
			}
		}
	}

	public void paperPlot() {
		Graphics2D g = this.img.createGraphics();
		int numVerticle = (int) ((signal.finishTime - signal.startTime) / 0.04);
		for (int i = 0; i < numVerticle; i++) {
			int x = (int) (i * img.getWidth() * 0.8 / numVerticle + 0.5 + img.getWidth() * 0.1);
			if (i % 4 == 0) {
				g.setColor(Color.GRAY);
				g.setFont(new Font("Tahoma", Font.BOLD, 12));
				g.drawString((int) ((signal.startTime + 0.04 * i) * 100) / 100f + "s", x - 10,
						img.getHeight() * 9 / 10 + 14);
				g.setStroke(new BasicStroke(2.0f));
			} else {
				g.setColor(Color.LIGHT_GRAY);
				g.setStroke(new BasicStroke(1.0f));
			}
			g.drawLine(x, img.getHeight() / 10, x, img.getHeight() * 9 / 10);
		}
		int numHorizontal = (int) ((signal.signalMax[0] - signal.signalMin[0]) / 0.1);
		for (int i = 0; i < numHorizontal; i++) {
			int y = (int) (i * img.getHeight() * 0.8 / numHorizontal + 0.5 + img.getHeight() * 0.1);
			if (i % 4 == 0) {
				g.setColor(Color.GRAY);
				g.drawString((int) ((signal.signalMax[0] - 0.1 * i) * 100) / 100f + "mV", img.getWidth() / 10 - 50,
						y + 4);
				g.setStroke(new BasicStroke(2.0f));
			} else {
				g.setColor(Color.LIGHT_GRAY);
				g.setStroke(new BasicStroke(1.0f));
			}
			g.drawLine(img.getWidth() / 10, y, img.getWidth() * 9 / 10, y);
		}
	}

	public void signalPlot() {
		Graphics2D g = this.img.createGraphics();
		g.setStroke(new BasicStroke(2.0f));
		signal.findSignalBaseLine(signal);
		int MM = 0;
		for (int i = 0; i < signal.baseLineFreq.length; i++) {
			MM = Math.max(MM, signal.baseLineFreq[i]);
		}
		signal.findSignalBaseLine2();
		for (int i = 1; i < signal.signalName.length; i++) {
			int prevX = 0, prevY = 0;
			boolean first = false;
			for (int j = 0; j < signal.signal.size(); j++) {
				if (signal.signal.get(j)[0] - signal.startTime < 0 || signal.signal.get(j)[0] > signal.finishTime)
					continue;
				g.setColor(i == 1 ? Color.RED : i == 2 ? Color.BLUE : new Color(0, 200, 0));
				int x = (int) ((signal.signal.get(j)[0] - signal.startTime) / (signal.finishTime - signal.startTime)
						* this.img.getWidth() * 0.8 + this.img.getWidth() * 0.1 + 0.5);
				int y = (int) ((1.0f
						- (signal.signal.get(j)[i] - signal.signalMin[0]) / (signal.signalMax[0] - signal.signalMin[0]))
						* this.img.getHeight() * 0.8 + this.img.getHeight() * 0.1 + 0.5);
				if (j > 0 && first) {
					g.drawLine(prevX, prevY, x, y);
				}
				first = true;
				g.setColor(new Color(0,
						(int) (signal.baseSignal[j] * 255 / 15
								* signal.baseLineFreq[signal.valueToBaseLine(signal, signal.signal.get(j)[1])] / MM),
						0));
				g.fillOval(x - 2, y - 2, 4, 4);
				g.setColor(i == 1 ? Color.RED : i == 2 ? Color.BLUE : new Color(0, 200, 0));
				prevX = x;
				prevY = y;
			}
			g.setFont(new Font("Tahoma", Font.BOLD, 15));
			g.drawString(signal.signalName[i], prevX + 4, prevY + 4);
		}
	}

	public void rPlot() {
		if (signal.upperEnvForR == null) {
			System.out.println("no envforR");
			return;
		}
		// TODO omit env
		// this.envForRPlot();
		Graphics2D g = this.img.createGraphics();
		g.setColor(new Color(0, 255, 0));
		ArrayList<Point> a = signal.findR();
		if (signal.annotation.annotationName != null)
			annotation.insertR(signal, a);
		for (int i = 0; i < a.size(); i++) {
			if (signal.signal.get(i)[0] - originalStartTime < 0 || signal.signal.get(i)[0] > originalFinishTime)
				continue;
			Point p = translate(signal.signal.get(a.get(i).y)[0], signal.signal.get(a.get(i).y)[a.get(i).x]);
			g.fillOval(p.x - 5, p.y - 5, 10, 10);
		}
	}

	public void envPlot() {
		if (signal.upperEnv != null) {
			Graphics2D g = this.img.createGraphics();
			g.setStroke(new BasicStroke(2.0f));
			g.setColor(new Color(255, 125, 255));
			int prevX = 0, prevY = 0, prevX2 = 0, prevY2 = 0;
			boolean first = false;
			for (int k = 1; k < signal.upperEnv.length; k++) {
				for (int i = 0; i < signal.signal.size(); i++) {
					if (signal.signal.get(i)[0] - signal.startTime < 0 || signal.signal.get(i)[0] > signal.finishTime)
						continue;
					Point a = translate(signal.signal.get(i)[0], signal.upperEnv[k][i]);
					Point a2 = translate(signal.signal.get(i)[0], signal.lowerEnv[k][i]);
					if (i > 0 && first) {
						g.drawLine(prevX, prevY, a.x, a.y);
						g.drawLine(prevX2, prevY2, a2.x, a2.y);
					}
					first = true;
					prevX = a.x;
					prevY = a.y;
					prevX2 = a2.x;
					prevY2 = a2.y;
				}
			}
		}
	}

	public void envForRPlot() {
		if (signal.upperEnvForR != null) {
			Graphics2D g = this.img.createGraphics();
			g.setStroke(new BasicStroke(2.0f));
			g.setColor(new Color(125, 255, 255));
			int prevX = 0, prevY = 0, prevX2 = 0, prevY2 = 0;
			boolean first = false;
			for (int k = 1; k < signal.upperEnvForR.length; k++) {
				for (int i = 0; i < signal.signal.size(); i++) {
					if (signal.signal.get(i)[0] - signal.startTime < 0 || signal.signal.get(i)[0] > signal.finishTime)
						continue;
					Point a = translate(signal.signal.get(i)[0], signal.upperEnvForR[k][i]);
					Point a2 = translate(signal.signal.get(i)[0], signal.lowerEnvForR[k][i]);
					if (i > 0 && first) {
						g.drawLine(prevX, prevY, a.x, a.y);
						g.drawLine(prevX2, prevY2, a2.x, a2.y);
					}
					first = true;
					prevX = a.x;
					prevY = a.y;
					prevX2 = a2.x;
					prevY2 = a2.y;
				}
			}
			g.setStroke(new BasicStroke(5f));
			Point tmp = translate(1, signal.findSignalBaseLine(signal));
			g.setColor(Color.ORANGE);
			System.out.println(tmp.y);
			g.drawLine(0, tmp.y, img.getWidth(), tmp.y);
		}
	}

	public void intervalPlot() {
		Graphics2D g = this.img.createGraphics();
		g.setStroke(new BasicStroke(3f));
		g.setColor(new Color(125, 255, 255));
		g.setFont(new Font("Tahoma", Font.PLAIN, 10));
		for (int i = 1; i < annotation.interval.size(); i++) {
			Point pFront = translate(annotation.interval.get(i)[1], 0);
			Point pEnd = translate(annotation.interval.get(i)[2], 0);
			Point qrsFront = translate(annotation.interval.get(i)[3], 0);
			Point qrsEnd = translate(annotation.interval.get(i)[4], 0);
			Point tFront = translate(annotation.interval.get(i)[5], 0);
			// System.out.println(tFront.x+" "+qrsEnd.x);
			Point tEnd = translate(annotation.interval.get(i)[6], 0);
			g.setColor(Color.GREEN);
			g.fillRect(pFront.x, img.getHeight() - 40, qrsFront.x - pFront.x, 10);
			g.drawString("PRinterval", qrsFront.x + 5, img.getHeight() - 30);
			g.setColor(new Color(204, 153, 255));
			g.fillRect(pEnd.x, img.getHeight() - 30, qrsFront.x - pEnd.x, 10);
			g.drawString("PRsegment", qrsFront.x + 5, img.getHeight() - 20);
			g.setColor(Color.DARK_GRAY);
			g.fillRect(qrsFront.x, img.getHeight() - 20, qrsEnd.x - qrsFront.x, 10);
			g.setColor(Color.cyan);
			g.fillRect(qrsEnd.x, img.getHeight() - 20, tFront.x - qrsEnd.x, 10);
			g.setColor(Color.pink);
			g.fillRect(qrsFront.x, img.getHeight() - 10, tEnd.x - qrsFront.x, 10);
			g.setColor(Color.white);
			g.drawString("QT", qrsFront.x + 1, img.getHeight());
			g.drawString("QRS", qrsFront.x + 1, img.getHeight() - 11);
			g.setColor(Color.black);
			g.drawString("ST", qrsEnd.x + 1, img.getHeight() - 11);
			g.setColor(Color.red);
			if (annotation.interval.get(i)[4] - annotation.interval.get(i)[3] < 0.07
					|| annotation.interval.get(i)[4] - annotation.interval.get(i)[3] > 0.1)
				g.drawRect(qrsFront.x, img.getHeight() - 20, qrsEnd.x - qrsFront.x, 10);
			if (annotation.interval.get(i)[1] - annotation.interval.get(i)[3] < 0.12
					&& annotation.interval.get(i)[1] - annotation.interval.get(i)[3] > 0.2)
				g.drawRect(pFront.x, img.getHeight() - 40, qrsFront.x - pFront.x, 10);
			if (annotation.interval.get(i)[6] - annotation.interval.get(i)[3] < 0.35
					|| annotation.interval.get(i)[6] - annotation.interval.get(i)[3] > 0.45)
				g.drawRect(qrsFront.x, img.getHeight() - 10, tEnd.x - qrsFront.x, 10);
			if (!signal.isSTabnormal(annotation.interval.get(i)[2], annotation.interval.get(i)[3],
					annotation.interval.get(i)[4], annotation.interval.get(i)[5])) {
				g.drawRect(qrsEnd.x, img.getHeight() - 20, tFront.x - qrsEnd.x, 10);
			}
		}
	}

	public float findPercent(Float left, Float right) {
		if (left == null || right == null)
			return 0.0f;
		float min = Float.MAX_VALUE;
		float annoRight = 0;
		for (int i = 0; i < signal.annotation.annotation.size(); i++) {
			Float[] row = signal.annotation.annotation.get(i);
			if (row[4] == 0.0f && row[0] > left) {
				if (row[2] == 4) {
					if (Math.abs(row[0] - right) < min) {
						min = Math.abs(row[0] - right);
						annoRight = row[0];
					}
				}
			}
		}
		min = Float.MAX_VALUE;
		float annoLeft = 0;
		for (int i = 0; i < signal.annotation.annotation.size(); i++) {
			Float[] row = signal.annotation.annotation.get(i);
			if (row[4] == 0.0f && row[0] < right) {
				if (row[2] == 0) {
					if (Math.abs(row[0] - left) < min) {
						min = Math.abs(row[0] - left);
						annoLeft = row[0];
					}
				}
			}
		}
		float outerLeft = Math.min(annoLeft, left);
		float outerRight = Math.max(annoRight, right);
		float innerLeft = Math.max(annoLeft, left);
		float innerRight = Math.min(annoRight, right);
		return (innerRight - innerLeft) / (outerRight - outerLeft) * 100.0f;
	}

	protected void markerPlot() {
		Graphics2D g = this.img.createGraphics();
		g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 9f, 3f },
				0.0f));
		g.setColor(Color.BLACK);
		g.setFont(new Font("Tahoma", Font.BOLD, 13));
		float prev = signal.startTime;
		int count = 0;
		int percentCounts[][] = new int[Signal.SIGNALS.length][20];
		float percents[] = new float[Signal.SIGNALS.length];
		int counts[] = new int[Signal.SIGNALS.length];
		boolean repeat = true;
		for (Float f : signal.marker.times) {
			float temp = f;
			boolean repeated = false;
			while (true) {
				boolean repeating = !cursorChoose && cursorTime != null && f >= cursorTime && repeat;
				if (repeating) {
					repeated = true;
					f = cursorTime;
					g.setColor(new Color(100, 100, 255));
				} else {
					f = temp;
					g.setColor(new Color(0, 0, 0));
				}
				count++;
				int x = translate(f, 0).x;
				g.drawLine(x, 0, x, this.img.getHeight());
				float percent = findPercent(prev, f);
				int xx = translate((prev + f) / 2, 0).x - 20;
				int yy = this.img.getHeight() / 20 + 13 * (count % 2);
				int yy2 = this.img.getHeight() / 10;
				for (int i = 0; i < signal.marker.signals.size(); i++) {
					TreeSet<Float> ts = signal.marker.signals.get(i);
					if (ts.ceiling(prev) != null && ts.ceiling(prev).equals(ts.floor(f))) {
						g.setColor(new Color[] { Color.RED, Color.GREEN, Color.BLUE }[i]);
						g.drawString(Signal.SIGNALS[i], xx - 1 + 15, yy2 - 1 + 20);
						g.drawString(Signal.SIGNALS[i], xx - 1 + 15, yy2 + 1 + 20);
						g.drawString(Signal.SIGNALS[i], xx + 1 + 15, yy2 - 1 + 20);
						g.drawString(Signal.SIGNALS[i], xx + 1 + 15, yy2 + 1 + 20);
						g.setColor(Color.WHITE);
						g.drawString(Signal.SIGNALS[i], xx + 15, yy2 + 20);
						percents[i] += percent;
						counts[i]++;
						for (int j = 0; j < percentCounts[i].length; j++) {
							if (percent >= j * 5) {
								percentCounts[i][j]++;
							}
						}
					}
				}
				prev = f;
				if (percent <= 0) {
					if (repeating)
						repeat = false;
					else
						break;
					continue;
				}
				g.setColor(cursorChoose && f >= cursorTime && repeat ? Color.BLACK
						: repeating || repeated ? new Color(255, 240, 0) : Color.WHITE);
				g.drawString(String.format("%.2f", percent) + "%", xx - 1, yy - 1);
				g.drawString(String.format("%.2f", percent) + "%", xx - 1, yy + 1);
				g.drawString(String.format("%.2f", percent) + "%", xx + 1, yy - 1);
				g.drawString(String.format("%.2f", percent) + "%", xx + 1, yy + 1);
				g.setColor(cursorChoose && f >= cursorTime && repeat ? Color.YELLOW
						: repeating || repeated ? new Color(100, 100, 255) : Color.BLACK);
				g.drawString(String.format("%.2f", percent) + "%", xx, yy);
				if (repeating) {
					repeat = false;
				} else if (cursorTime != null && f >= cursorTime) {
					repeat = false;
					count--;
				} else {
					break;
				}
			}
		}
		for (int i = 0; i < percents.length; i++) {
			percents[i] /= counts[i];
		}
		try {
			File f = null;
			if (writePrepare) {
				writePrepare = false;
				write = true;
			}
			if (write)
				f = new File(signal.signalFile.getPath().substring(0, signal.signalFile.getPath().length() - 4)
						+ "result.txt");
			PrintWriter p = null;
			if (write)
				p = new PrintWriter(f);
			for (int i = 0; i < percents.length; i++) {
				g.setColor(Color.BLACK);
				g.drawString(String.format(Signal.SIGNALS[i] + " = %.2f%%", percents[i]),
						this.img.getWidth() * 9 / 10 + 10 - 2, 20 + 15 * i - 2);
				g.drawString(String.format(Signal.SIGNALS[i] + " = %.2f%%", percents[i]),
						this.img.getWidth() * 9 / 10 + 10 - 2, 20 + 15 * i + 2);
				g.drawString(String.format(Signal.SIGNALS[i] + " = %.2f%%", percents[i]),
						this.img.getWidth() * 9 / 10 + 10 + 2, 20 + 15 * i - 2);
				g.drawString(String.format(Signal.SIGNALS[i] + " = %.2f%%", percents[i]),
						this.img.getWidth() * 9 / 10 + 10 + 2, 20 + 15 * i + 2);
				g.drawString(String.format(Signal.SIGNALS[i] + " = %.2f%%", percents[i]),
						this.img.getWidth() * 9 / 10 + 10 - 1, 20 + 15 * i - 1);
				g.drawString(String.format(Signal.SIGNALS[i] + " = %.2f%%", percents[i]),
						this.img.getWidth() * 9 / 10 + 10 - 1, 20 + 15 * i + 1);
				g.drawString(String.format(Signal.SIGNALS[i] + " = %.2f%%", percents[i]),
						this.img.getWidth() * 9 / 10 + 10 + 1, 20 + 15 * i - 1);
				g.drawString(String.format(Signal.SIGNALS[i] + " = %.2f%%", percents[i]),
						this.img.getWidth() * 9 / 10 + 10 + 1, 20 + 15 * i + 1);
				g.setColor(Color.YELLOW);
				g.drawString(String.format(Signal.SIGNALS[i] + " = %.2f%%", percents[i]),
						this.img.getWidth() * 9 / 10 + 10, 20 + 15 * i);
				if (write)
					p.println(String.format("%.2f", percents[i]));
			}
			if (write)
				p.println();
			g.setColor(Color.BLACK);
			for (int i = 0; i < percentCounts[0].length; i++) {
				if (i < percentCounts[0].length) {

					g.drawString(">=" + i * 5 + "%", 2,
							this.img.getHeight() / 10 + 50 * (percentCounts[0].length - i - 1));
					if (write)
						p.println(String.format("%d ", i * 5));
				}
				for (int j = 0; j < percentCounts.length; j++) {
					g.drawString(Signal.SIGNALS[j], 2 + 20 * j,
							this.img.getHeight() / 10 + 50 * (percentCounts[0].length - i - 1) + 13);
					g.drawString(percentCounts[j][i] + "", 2 + 20 * j,
							this.img.getHeight() / 10 + 50 * (percentCounts[0].length - i - 1) + 26);
					if (write)
						p.print(String.format("%d ", percentCounts[j][i]));
				}
				if (write)
					p.println();
			}
			if (write) {
				p.close();
				write = false;
				System.out.println("Result Saved!");
			}
		} catch (

		FileNotFoundException e)

		{
			e.printStackTrace();
		}

	}

	public Point translate(float t, float mv) {
		int x = (int) ((t - signal.startTime) / (signal.finishTime - signal.startTime) * img.getWidth() * 0.8
				+ (0.1 * img.getWidth()) + 0.5);
		int y = (int) ((1.0f
				- (mv - signal.signalMin[0]) / (signal.signalMax[0] - signal.signalMin[0]) * img.getHeight()) * 0.8
				+ 0.9 * img.getHeight() + 0.5);
		Point p = new Point(x, y);
		return p;
	}
}
