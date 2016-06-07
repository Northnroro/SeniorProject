import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Signal {

	protected static final String path = System.getProperty("user.name").equals("DELL WIN7")
			? "C:/cygwin/home/SeniorProject/data/output/database/" : "C:/cygwin64/home/GS60/data/db/";
	protected static final String outputPath = System.getProperty("user.name").equals("DELL WIN7")
			? "C:/cygwin/home/SeniorProject/data/output/output/" : "C:/cygwin64/home/GS60/data/output/";

	protected static final String selectPath = System.getProperty("user.name").equals("DELL WIN7")
			? "C:/cygwin/home/SeniorProject/data/output/databaseSelect" : "C:/cygwin64/home/GS60/data/dbSelect";
	protected static final String dbPath = selectPath;

	protected static final int WINDOW_SIZE = 30;
	protected static final int BIN_SIZE = 200;

	public static final String SIGNALS[] = new String[] { "P", "R", "T" };

	protected float startTime, finishTime, duration;

	protected File signalFile;
	protected String signalName[]; // Time Signal0..N
	protected ArrayList<Float[]> signal = new ArrayList<Float[]>();// Time(s)
																	// Signal0..N(mV)
	protected float signalMax[], signalMin[];// SignalAll(mV) Signal0..N(mV)

	protected Annotation annotation;

	protected Marker marker;

	protected int[] dtwPath;
	protected float[][] dtw;
	protected float[][] upperEnv;
	protected float[][] lowerEnv;
	protected float[][] lowerEnvForR;
	protected float[][] upperEnvForR;
	protected int channel = 1;
	protected float[] meanArray;
	protected float[] sdArray;
	protected int[] baseLineFreq;
	protected float[] baseSignal;

	private static ArrayList<Signal> database = new ArrayList<Signal>();

	private static final String[][] VALID_SIGNAL_NAMES = { { "Time", "Elapsed time" }, { "Lead I", "I" },
			{ "Lead II", "II", "MLII", "MLIII", "ECG1" }, { "V1" }, { "V2" }, { "V4" }, { "V5", "ECG2" } };

	public Signal(File signalFile, float startTime, float finishTime) {
		try {
			this.signalFile = signalFile;
			this.startTime = startTime;
			this.finishTime = finishTime;
			@SuppressWarnings("resource")
			Scanner sc = new Scanner(this.signalFile);
			this.signalName = sc.nextLine().trim().split("(\\s\\s+)|(\\s*\\t\\s*)");
			String signalName2[] = sc.nextLine().trim().split("(\\s\\s+)|(\\s*\\t\\s*)");
			boolean validSignals[] = new boolean[this.signalName.length];
			int validSignalCount = 0;
			for (int i = 0; i < validSignals.length; i++) {
				for (int j = 0; j < VALID_SIGNAL_NAMES.length && !validSignals[i]; j++) {
					for (int k = 0; k < VALID_SIGNAL_NAMES[j].length; k++) {
						if (this.signalName[i].equalsIgnoreCase(VALID_SIGNAL_NAMES[j][k])) {
							validSignals[i] = true;
							this.signalName[i] = VALID_SIGNAL_NAMES[j][0];
							validSignalCount++;
							break;
						}
					}
				}
				// Math.hypot(x, y)
			}
			for (int i = 0; i < this.signalName.length; i++) {
				this.signalName[i] = this.signalName[i].trim() + " " + signalName2[i].trim();
			}
			String newSignalName[] = new String[validSignalCount];
			validSignalCount = 0;
			for (int i = 0; i < this.signalName.length; i++) {
				if (validSignals[i]) {
					newSignalName[validSignalCount++] = this.signalName[i];
				}
			}
			this.signalName = newSignalName;
			this.signalMin = new float[this.signalName.length];
			this.signalMax = new float[this.signalName.length];
			for (int i = 0; i < this.signalName.length; i++) {
				this.signalMax[i] = -Float.MAX_VALUE;
				this.signalMin[i] = Float.MAX_VALUE;
			}
			boolean start = false, stop = false;
			while (sc.hasNextFloat() && !stop) {
				Float row[] = new Float[this.signalName.length];
				validSignalCount = 0;
				for (int j = 0; j < validSignals.length; j++) {
					if (!validSignals[j]) {
						sc.nextFloat();
						continue;
					}
					row[validSignalCount] = sc.nextFloat();
					if (validSignalCount == 0) {
						if (row[validSignalCount] >= this.startTime && !start) {
							start = true;
							this.startTime = row[validSignalCount];
						} else if (row[validSignalCount] > this.finishTime && !stop) {
							stop = true;
							this.finishTime = row[validSignalCount];
							this.duration = this.finishTime - this.startTime;
							break;
						}
					} else {
						this.signalMin[validSignalCount] = Math.min(this.signalMin[validSignalCount],
								row[validSignalCount]);
						this.signalMax[validSignalCount] = Math.max(this.signalMax[validSignalCount],
								row[validSignalCount]);
						this.signalMin[0] = Math.min(this.signalMin[0], this.signalMin[validSignalCount]);
						this.signalMax[0] = Math.max(this.signalMax[0], this.signalMax[validSignalCount]);
					}
					validSignalCount++;
				}
				if (start && !stop) {
					this.signal.add(row);
				}
			}
			if (this.signal.size() > 0)
				this.finishTime = Math.min(this.finishTime, this.signal.get(this.signal.size() - 1)[0]);
			this.duration = this.finishTime - this.startTime;
			meanArray = new float[signalName.length];
			sdArray = new float[signalName.length];
			for (int i = 1; i < signalName.length; i++) {
				meanArray[i] = findMean(i);
				// TODO TEST
				if (i == 1)
					meanArray[i] = findSignalBaseLine(this);
				sdArray[i] = findSD(i);
			}
			String ann = (signalFile.getAbsolutePath());
			File anno = new File((ann.substring(0, ann.length() - 4)) + "pu.txt");
			if (annotation == null)
				annotation = new Annotation(anno, startTime, finishTime);
			String name = this.signalFile.getPath();
			this.marker = new Marker(new File(name.substring(0, name.length() - 4) + "answer.txt"));
			// this.smooth();
			// this.smooth();
			// this.smooth();
			// this.smooth();
			// this.smooth();
			// this.smooth();
			// this.smooth();
			// this.smooth();
			// this.smooth();
			// this.smooth();
		} catch (FileNotFoundException e) {
			System.out.println("ERROR : File Not Found (Signal)");
			// e.printStackTrace();
		}
	}

	public Signal(File signalFile, float startTime, float finishTime, boolean toBeSplited) {
		this(signalFile, startTime, finishTime);
		// this.smooth();
		if (toBeSplited) {
			this.lowerBoundKeoghEnvelope(Signal.WINDOW_SIZE);
			this.lowerBoundKeoghEnvelopeForR(calculateWForR());
			ArrayList<Point> a = this.findR();
			if (this.annotation.annotationName == null)
				annotation = new Annotation("temp.txt");
			annotation.insertR(this, a);
		}
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

	public int dtwDistance() {
		int n = signal.size();
		int m = signal.size();
		int[][] dtw = new int[n][m];
		for (int i = 0; i < n; i++) {
			dtw[i][0] = Integer.MAX_VALUE;
		}
		for (int i = 0; i < m; i++) {
			dtw[0][m] = Integer.MAX_VALUE;
		}
		dtw[0][0] = 0;

		float cost;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				cost = signal.get(i)[1] - signal.get(j)[1];
				int x = 1;
				int y = 1;
				if (i - 1 < 0)
					x = 0;
				if (j - 1 < 0)
					y = 0;
				dtw[i][j] = (int) (cost + Math.min(Math.min(dtw[i - x][j], dtw[i][j - y]), dtw[i - x][j - y]));
			}
		}
		return dtw[signal.size() - 1][signal.size() - 1];
	}

	public float findMean(int x) {
		float mean = 0;
		for (int i = 0; i < signal.size(); i++) {
			mean += signal.get(i)[x];
		}
		mean /= signal.size();
		return mean;
	}

	public float findSD(int x) {
		float sd = 0;
		for (int i = 0; i < signal.size(); i++) {
			sd += (signal.get(i)[x] - meanArray[x]) * (signal.get(i)[x] - meanArray[x]);
		}
		sd /= signal.size();
		sd = (float) Math.sqrt(sd);
		return sd;
	}

	public float zScore(float val, int chan) {
		return (val - meanArray[chan]) / sdArray[chan];
	}

	public boolean findRDone = false;
	private ArrayList<Point> aFindR;

	public ArrayList<Point> findR() {
		if (!findRDone) {
			ArrayList<Point> a = new ArrayList<Point>();
			float w = calculateWForR() * 5;
			float w2 = w / 2.5f;
			for (int k = 1; k < upperEnvForR.length; k++) {
				boolean prev = false;
				float prev2 = -Float.MAX_VALUE;
				float boundDiff = 0f;
				for (int i = 0; i < lowerEnvForR[k].length; i++) {
					float maxOfLower = -Float.MAX_VALUE;
					float minOfUpper = Float.MAX_VALUE;
					for (int j = 0; j < w2; j++) {
						if (i - j >= 0 && maxOfLower < lowerEnvForR[k][i - j])
							maxOfLower = lowerEnvForR[k][i - j];
						if (i + j < lowerEnvForR[k].length && maxOfLower < lowerEnvForR[k][i + j])
							maxOfLower = lowerEnvForR[k][i + j];
						if (i - j >= 0 && minOfUpper > upperEnvForR[k][i - j])
							minOfUpper = upperEnvForR[k][i - j];
						if (i + j < upperEnvForR[k].length && minOfUpper > upperEnvForR[k][i + j])
							minOfUpper = upperEnvForR[k][i + j];
					}
					boundDiff = Math.max(upperEnvForR[k][i] - maxOfLower + minOfUpper - lowerEnvForR[k][i], boundDiff);
				}
				for (int i = 0; i < signal.size(); i++) {
					float maxOfLower = -Float.MAX_VALUE;
					float minOfUpper = Float.MAX_VALUE;
					for (int j = 0; j < w2; j++) {
						if (i - j >= 0 && maxOfLower < lowerEnvForR[k][i - j])
							maxOfLower = lowerEnvForR[k][i - j];
						if (i + j < lowerEnvForR[k].length && maxOfLower < lowerEnvForR[k][i + j])
							maxOfLower = lowerEnvForR[k][i + j];
						if (i - j >= 0 && minOfUpper > upperEnvForR[k][i - j])
							minOfUpper = upperEnvForR[k][i - j];
						if (i + j < upperEnvForR[k].length && minOfUpper > upperEnvForR[k][i + j])
							minOfUpper = upperEnvForR[k][i + j];
					}
					if (0.4f * boundDiff < (upperEnvForR[k][i] - maxOfLower + minOfUpper - lowerEnvForR[k][i])) {
						// if (signal.get(i)[1] > 0.7f * (upperEnvForR[i] -
						// lowerEnvForR[i]) + lowerEnvForR[i]) {
						if (!prev) {
							if (i - prev2 < w && a.get(a.size() - 1).x == k)
								continue;
							if (k == channel) {
								a.add(new Point(k, i));
								prev = true;
								prev2 = i;
							}
						} else {
							if (signal.get(a.get(a.size() - 1).y)[k] < signal.get(i)[k] && a.get(a.size() - 1).x == k) {
								if (k == channel) {
									a.set(a.size() - 1, (new Point(k, i)));
									prev2 = i;
								}
							}
						}
					} else if (i - prev2 > w) {
						prev = false;
					}
				}
			}
			aFindR = a;
			findRDone = true;
		}
		return aFindR;
		// /////////////////////////////////////////////////new
	}

	public float dtwShibaSakoe(int w, Signal target) {
		int matchFound = 0;
		float sum = 0;
		w = Math.max(w, Math.abs(this.signal.size() - target.signal.size()));
		for (int k = this.signalName.length - 1; k >= 1; k--) {
			int matchChannel = -1;
			for (int i = 0; i < target.signalName.length; i++) {
				if (signalName[k].equalsIgnoreCase(target.signalName[i])) {
					matchFound++;
					matchChannel = i;
				}
			}
			if (matchChannel == -1)
				continue;
			dtw = new float[this.signal.size()][target.signal.size()];
			for (int i = 0; i < this.signal.size(); i++) {
				for (int j = 0; j < target.signal.size(); j++) {
					dtw[i][j] = Integer.MAX_VALUE;
				}
			}
			dtw[0][0] = 0;
			float cost;
			for (int i = 0; i < this.signal.size(); i++) {
				for (int j = Math.max(0, i - w); j < Math.min(target.signal.size(), i + w); j++) {
					cost = Math.abs(zScore(this.signal.get(i)[k], k)
							- target.zScore(target.signal.get(j)[matchChannel], matchChannel));
					int x = 1;
					int y = 1;
					if (i - 1 < 0)
						x = 0;
					if (j - 1 < 0)
						y = 0;
					dtw[i][j] = cost + Math.min(Math.min(dtw[i - x][j], dtw[i][j - y]), dtw[i - x][j - y]);
				}
			}
			sum += dtw[this.signal.size() - 1][target.signal.size() - 1];
		}
		if (matchFound != 0) {
			return sum / matchFound;
		} else
			return Float.MAX_VALUE;
	}

	public float dtwShibaSakoeDiff(int w, Signal target) {
		int matchFound = 0;
		float sum = 0;
		w = Math.max(w, Math.abs(this.signal.size() - target.signal.size()));
		for (int k = 1; k < this.signalName.length; k++) {
			dtw = new float[this.signal.size()][target.signal.size()];
			int matchChannel = -1;
			for (int i = 0; i < target.signalName.length; i++) {
				if (signalName[k].equalsIgnoreCase(target.signalName[i])) {
					matchFound++;
					matchChannel = i;
				}
			}
			if (matchChannel == -1)
				continue;
			for (int i = 0; i < this.signal.size(); i++) {
				for (int j = 0; j < target.signal.size(); j++) {
					dtw[i][j] = Integer.MAX_VALUE;
				}
			}
			dtw[0][0] = 0;
			float cost;
			for (int i = 1; i < this.signal.size(); i++) {
				for (int j = Math.max(1, i - w); j < Math.min(target.signal.size(), i + w); j++) {
					float A = (zScore(this.signal.get(i)[k], k) - zScore(this.signal.get(i - 1)[k], k))
							/ (this.signal.get(i)[0] - this.signal.get(i - 1)[0]);
					float B = (target.zScore(target.signal.get(j)[matchChannel], matchChannel)
							- target.zScore(target.signal.get(j - 1)[matchChannel], matchChannel)
									/ (target.signal.get(i)[0] - target.signal.get(i - 1)[0]));
					if (A > 0 && A > 2f) {
						A += 2;
					}
					if (B > 0 && B > 2f) {
						B += 2;
					}
					if (A < 0 && A < -2f) {
						A -= 2;
					}
					if (B < 0 && B < -2f) {
						B -= 2;
					}
					cost = Math.abs(A - B);
					int x = 1;
					int y = 1;
					if (i - 1 < 0)
						x = 0;
					if (j - 1 < 0)
						y = 0;
					dtw[i][j] = cost + Math.min(Math.min(dtw[i - x][j] + cost * 0f, dtw[i][j - y] + cost * 0f),
							dtw[i - x][j - y]);
				}
			}
			sum += dtw[this.signal.size() - 1][target.signal.size() - 1];
		}
		if (matchFound != 0) {
			return sum / matchFound;
		} else
			return Float.MAX_VALUE;
	}

	public int[] dtwShibaPath(int w, Signal target) {// each target map to which
														// this
		this.dtwShibaSakoe(w, target);
		int i = this.signal.size() - 1;
		int j = target.signal.size() - 1;
		dtwPath = new int[Math.min(this.signal.size(), target.signal.size())];
		dtwPath[j] = i;
		while (!(i == 0 || j == 0)) {
			float min = Math.min(Math.min(dtw[i - 1][j], dtw[i][j - 1]), dtw[i - 1][j - 1]);
			if (j - 1 >= 0 && dtw[i][j - 1] == min) {
				j -= 1;

			} else if (i - 1 >= 0 && dtw[i - 1][j] == min) {
				i -= 1;
			} else if (i - 1 >= 0 && j - 1 >= 0 && dtw[i - 1][j - 1] == min) {
				i -= 1;
				j -= 1;
			}
			dtwPath[j] = Math.max(dtwPath[j], i);
		}
		return dtwPath;
	}

	public void lowerBoundKeoghEnvelope(int w) {
		upperEnv = new float[signalName.length][signal.size()];
		lowerEnv = new float[signalName.length][signal.size()];
		for (int i = 0; i < signal.size(); i++) {
			float[] max = new float[upperEnv.length];
			float[] min = new float[upperEnv.length];
			for (int j = 0; j < min.length; j++) {
				max[j] = -Float.MAX_VALUE;
				min[j] = Float.MAX_VALUE;
			}
			for (int j = 0; j < w; j++) {
				for (int k = 1; k < upperEnv.length; k++) {
					if (i - j >= 0 && signal.get(i - j)[k] > max[k]) {
						upperEnv[k][i] = signal.get(i - j)[k];
						max[k] = upperEnv[k][i];
					}
					if (i + j < signal.size() && signal.get(i + j)[k] > max[k]) {
						upperEnv[k][i] = signal.get(i + j)[k];
						max[k] = upperEnv[k][i];
					}
					if (i + j < signal.size() && signal.get(i + j)[k] < min[k]) {
						lowerEnv[k][i] = signal.get(i + j)[k];
						min[k] = lowerEnv[k][i];
					}
					if (i - j >= 0 && signal.get(i - j)[k] < min[k]) {
						lowerEnv[k][i] = signal.get(i - j)[k];
						min[k] = lowerEnv[k][i];
					}
				}
			}
		}
	}

	public void lowerBoundKeoghEnvelopeForR(int w) {
		upperEnvForR = new float[signalName.length][signal.size()];
		lowerEnvForR = new float[signalName.length][signal.size()];
		for (int i = 0; i < signal.size(); i++) {
			float[] max = new float[upperEnvForR.length];
			float[] min = new float[upperEnvForR.length];
			for (int j = 0; j < min.length; j++) {
				max[j] = -Float.MAX_VALUE;
				min[j] = Float.MAX_VALUE;
			}
			for (int j = 0; j < w; j++) {
				for (int k = 1; k < upperEnvForR.length; k++) {
					if (i - j >= 0 && signal.get(i - j)[k] > max[k]) {
						upperEnvForR[k][i] = signal.get(i - j)[k];
						max[k] = upperEnvForR[k][i];
					}
					if (i + j < signal.size() && signal.get(i + j)[k] > max[k]) {
						upperEnvForR[k][i] = signal.get(i + j)[k];
						max[k] = upperEnvForR[k][i];
					}
					if (i + j < signal.size() && signal.get(i + j)[k] < min[k]) {
						lowerEnvForR[k][i] = signal.get(i + j)[k];
						min[k] = lowerEnvForR[k][i];
					}
					if (i - j >= 0 && signal.get(i - j)[k] < min[k]) {
						lowerEnvForR[k][i] = signal.get(i - j)[k];
						min[k] = lowerEnvForR[k][i];
					}
				}
			}
		}
	}

	public int calculateWForR() {
		return (int) (this.signal.size() / (this.finishTime - this.startTime) * 0.04);
	}

	public ArrayList<Float> split(boolean forLabel) {
		ArrayList<Float> splitTimes = new ArrayList<Float>();
		int n = 0;
		int pos = 0;
		int posann = 0;
		float prev = startTime;
		String currentPath = forLabel ? outputPath : path;
		File originalFile = this.signalFile;
		if (forLabel) {
			this.signalFile = new File(outputPath + "temp.txt");
		}
		while (posann < annotation.annotation.size()) {
			try {
				splitTimes.add(signal.get(pos)[0]);
				if (signal.size() - pos < 3)
					break;
				File file = new File(currentPath + signalFile.getName().substring(0, signalFile.getName().length() - 4)
						+ "__" + n + ".txt");
				PrintStream p = new PrintStream(file);
				PrintStream p2 = new PrintStream(new File(currentPath
						+ signalFile.getName().substring(0, signalFile.getName().length() - 4) + "__" + n + "pu.txt"));
				for (int i = 0; i < signalName.length; i++) {
					p.print(signalName[i].substring(0, signalName[i].lastIndexOf('(')) + "  ");
				}
				p.println();
				for (int i = 0; i < annotation.annotationName.length; i++) {
					if (annotation.annotationName[i] != null)
						p2.print(annotation.annotationName[i] + "  ");
				}
				p2.println();
				for (int i = 0; i < signalName.length; i++) {
					p.print(signalName[i].substring(signalName[i].lastIndexOf('(')) + "  ");
				}
				p.println();
				for (; posann < annotation.annotation.size(); posann++) {
					for (; pos < signal.size() && signal.get(pos)[0] <= annotation.annotation.get(posann)[0]; pos++) {
						p.print(signal.get(pos)[0] - prev);
						for (int i = 1; i < signal.get(pos).length; i++) {
							p.print("  " + signal.get(pos)[i]);
						}
						p.println();
					}
					// if (annotation.annotation.get(posann)[2] ==
					// 2&&annotation.annotation.get(posann)[4] == 0) {
					if (annotation.annotation.get(posann)[2] == 8 && annotation.annotation.get(posann)[4] == 0) {
						posann++;
						break;
					}
					// if (annotation.annotation.get(posann)[2] != 9) {
					if (annotation.annotation.get(posann)[2] < 8) {
						for (int i = 0; i < annotation.annotation.get(posann).length; i++) {
							Float f = annotation.annotation.get(posann)[i];
							if (f != null)
								p2.print(f - (i == 0 ? prev : 0) + "  ");
						}
						p2.println();
					}
				}
				if (pos < signal.size())
					prev = signal.get(pos)[0];
				p.close();
				p2.close();
				Signal s = new Signal(file, 0, Float.MAX_VALUE);
				s.stretch(BIN_SIZE);
				s.save();
				n++;
				// TODO Delete
				if (n > 22)
					break;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			// System.out.println(n);
		}
		this.signalFile = originalFile;
		System.out.println("DONE : FINISH SPLITING");
		return splitTimes;
	}

	public void split() {
		split(false);
	}

	public void save() {
		save(true);
	}

	public void save(boolean saveOwn) {
		int pos = 0;
		int posann = 0;
		try {
			File file = new File(signalFile.getPath());
			PrintStream p = null;
			if (saveOwn)
				p = new PrintStream(file);
			PrintStream p2 = new PrintStream(
					new File(signalFile.getPath().substring(0, signalFile.getPath().length() - 4) + "pu.txt"));
			if (saveOwn)
				for (int i = 0; i < signalName.length; i++) {
					p.print(signalName[i].substring(0, signalName[i].lastIndexOf('(')) + "  ");
				}
			if (saveOwn)
				p.println();
			for (int i = 0; i < annotation.annotationName.length; i++) {
				if (annotation.annotationName[i] != null)
					p2.print(annotation.annotationName[i] + "  ");
			}
			p2.println();
			if (saveOwn)
				for (int i = 0; i < signalName.length; i++) {
					p.print(signalName[i].substring(signalName[i].lastIndexOf('(')) + "  ");
				}
			if (saveOwn)
				p.println();
			for (; posann <= annotation.annotation.size(); posann++) {
				for (; pos < signal.size() && (posann == annotation.annotation.size()
						|| signal.get(pos)[0] <= annotation.annotation.get(posann)[0]); pos++) {
					if (saveOwn)
						p.print(signal.get(pos)[0] - startTime);
					if (saveOwn)
						for (int i = 1; i < signal.get(pos).length; i++) {
							p.print("  " + signal.get(pos)[i]);
						}
					if (saveOwn)
						p.println();
				}
				if (posann == annotation.annotation.size())
					break;
				if (annotation.annotation.get(posann)[2] < 8) {
					for (int i = 0; i < annotation.annotation.get(posann).length; i++) {
						Float f = annotation.annotation.get(posann)[i];
						if (f != null)
							p2.print(f - (i == 0 ? startTime : 0) + "  ");
					}
					p2.println();
				}
			}
			if (saveOwn)
				p.close();
			p2.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("Saving \"" + signalFile.getName() + "\" ...");
	}

	public void stretch(int numPoint) {
		double ratio = (double) numPoint / signal.size();
		this.duration *= ratio;
		this.finishTime = (float) (this.startTime + (this.finishTime - this.startTime) * ratio);
		ArrayList<Float[]> newSignal = new ArrayList<Float[]>();
		for (int i = 0; i < numPoint; i++) {
			Float[] row = new Float[this.signal.get(0).length];
			float upperRatio = (float) ((i / ratio) % 1f);
			if (i == numPoint - 1)
				upperRatio = 0f;
			float lowerRatio = 1f - upperRatio;
			for (int j = 0; j < row.length; j++) {
				row[j] = this.signal.get((int) (i / ratio))[j] * lowerRatio;
				if ((int) (i / ratio) + 1 < this.signal.size()) {
					row[j] += this.signal.get((int) (i / ratio) + 1)[j] * upperRatio;
				}
			}
			newSignal.add(row);
		}
		this.signal = newSignal;
	}

	public float lowerBoundDistance(Signal target) {
		float sumD = 0.0f;
		float sum = 0.0f;
		int matchFound = 0;
		target.lowerBoundKeoghEnvelope(WINDOW_SIZE);
		for (int k = 1; k < signalName.length; k++) {
			int matchChannel = -1;
			for (int i = 0; i < target.signalName.length; i++) {
				if (signalName[k].equalsIgnoreCase(target.signalName[i])) {
					matchFound++;
					matchChannel = i;
				}
			}
			if (matchChannel == -1)
				continue;
			for (int i = 0; i < Math.min(this.signal.size(), target.signal.size()); i++) {
				if (target.zScore(target.upperEnv[matchChannel][i], matchChannel) < zScore(this.signal.get(i)[k], k)) {
					sumD += Math.abs(zScore(this.signal.get(i)[k], k)
							- target.zScore(target.upperEnv[matchChannel][i], matchChannel));
				} else if (target.zScore(target.lowerEnv[matchChannel][i], matchChannel) > zScore(this.signal.get(i)[k],
						k)) {
					sumD += Math.abs(target.zScore(target.lowerEnv[matchChannel][i], matchChannel)
							- zScore(this.signal.get(i)[k], k));
				}
			}
			sum += sumD;
		}
		if (matchFound != 0)
			return sum / matchFound;
		else
			return Float.MAX_VALUE;
	}

	public float lowerBoundDistanceDiff(Signal target) {
		return 0.0f;
	}

	public static void loadDatabase() {
		System.out.println("Loading database from disk...");
		for (final File f : new File(dbPath).listFiles()) {
			if (f.isFile() && f.getName().substring(f.getName().length() - 4).equals(".txt")
					&& !f.getName().substring(f.getName().length() - 6).equals("pu.txt")) {
				System.out.println("Loading \"" + f.getName() + "\" ...");
				database.add(new Signal(f, 0f, Float.MAX_VALUE));
			}
		}
		System.out.println("DONE : database loaded");
	}

	public Signal findNearestSignal() {
		if (database.size() == 0) {
			loadDatabase();
		}
		float minDist = Float.MAX_VALUE;
		Signal minSignal = null;
		for (int i = 0; i < database.size(); i++) {
			String name = database.get(i).signalFile.getName();
			if (name.indexOf("__") >= 0 && name.substring(0, name.indexOf("__"))
					.equals(this.signalFile.getName().substring(0, this.signalFile.getName().length() - 4))) {
				continue;
			}
			if (this.lowerBoundDistance(database.get(i)) < minDist) {
				float realDist = this.dtwShibaSakoe(WINDOW_SIZE, database.get(i));
				if (realDist < minDist) {
					minDist = realDist;
					minSignal = database.get(i);
					// System.out.println(database.get(i).signalFile.getName() +
					// "(LB) = "
					// + this.lowerBoundDistance(database.get(i)));
					// System.out.println(database.get(i).signalFile.getName() +
					// "(Real) = " + realDist + " / " + minDist);

				}
			}
		}
		System.out.println(minSignal.signalFile.getName() + "(LB) = " + this.lowerBoundDistance(minSignal));
		System.out.println(minSignal.signalFile.getName() + "(Real) = " + minDist);
		return minSignal;
	}

	public Signal findNearestSignalDiff() {
		if (database.size() == 0) {
			loadDatabase();
		}
		float minDist = Float.MAX_VALUE;
		Signal minSignal = null;
		for (int i = 0; i < database.size(); i++) {
			if (this.lowerBoundDistanceDiff(database.get(i)) < minDist) {
				this.smooth();
				float realDist = this.dtwShibaSakoeDiff(WINDOW_SIZE, database.get(i));
				System.out.println(database.get(i).signalFile.getName() + "(LB) = "
						+ this.lowerBoundDistanceDiff(database.get(i)));
				System.out.println(database.get(i).signalFile.getName() + "(Real) = " + realDist + " / " + minDist);
				if (realDist < minDist) {
					minDist = realDist;
					minSignal = database.get(i);
				}
			}
		}
		return minSignal;
	}

	public void label(Signal target) {
		dtwShibaPath(Signal.WINDOW_SIZE, target);
		this.annotation = new Annotation(this.signalFile.getName());
		this.annotation.annotationName = new String[target.annotation.annotationName.length];
		for (int i = 0; i < this.annotation.annotationName.length; i++)
			this.annotation.annotationName[i] = target.annotation.annotationName[i];
		int j = 0;
		for (int i = 0; i < target.signal.size(); i++) {
			if (j < target.annotation.annotation.size()
					&& target.signal.get(i)[0] >= target.annotation.annotation.get(j)[0]) {
				Float[] row = new Float[target.annotation.annotation.get(0).length];
				row[0] = this.signal.get(dtwPath[i])[0];
				for (int k = 1; k < row.length; k++) {
					row[k] = target.annotation.annotation.get(j)[k];
				}
				this.annotation.annotation.add(row);
				j++;
			}
		}
	}

	public void labelAll() {
		int num = 0;
		while (true) {
			File f = new File(outputPath + "temp__" + num + ".txt");
			File f2 = new File(outputPath + "temp__" + num++ + ".txt");
			if (f.exists()) {
				f.delete();
				if (f2.exists()) {
					f2.delete();
				}
			} else {
				break;
			}
		}
		ArrayList<Float> splitTimes = this.split(true);
		Annotation annotation = new Annotation(this.signalFile.getName());
		this.annotation = annotation;
		float offset = 0f;
		ArrayList<File> allFiles = new ArrayList<File>();
		num = 0;
		while (true) {
			File f = new File(outputPath + "temp__" + num++ + ".txt");
			if (f.exists()) {
				allFiles.add(f);
			} else {
				break;
			}
		}
		System.out.println(allFiles.size() + " Splited Files Found");
		int fileCount = 0;
		for (final File f : allFiles) {
			System.out.println("------------------------------------------------------------ " + fileCount + " / "
					+ allFiles.size() + " ----------");
			if (f.isFile() && f.getName().substring(f.getName().length() - 4).equals(".txt")
					&& !f.getName().substring(f.getName().length() - 6).equals("pu.txt")) {
				offset = splitTimes.get(fileCount++);
				System.out.println("Loading \"" + f.getName() + "\" ...");
				Signal beat = new Signal(f, 0f, Float.MAX_VALUE, true);
				Signal match = beat.findNearestSignal();// Diff of NOT Diff
				beat.label(match);
				findSignalBaseLine(beat);
				beat.adjustLabel();
				beat.adjustLabel();
				beat.adjustLabel();
				for (int i = 0; i <= beat.annotation.annotation.size(); i++) {
					if (i == 0) {
						Float row[] = new Float[7];
						row[0] = beat.startTime + offset;
						row[1] = 0f;
						row[2] = 2f;
						row[3] = 0f;
						row[4] = 0f;
						row[5] = 0f;
						this.annotation.annotation.add(row);
					}
					if (i < beat.annotation.annotation.size()) {
						Float row[] = beat.annotation.annotation.get(i);
						row[0] += offset;
						this.annotation.annotation.add(row);
					}
				}
//				System.out.println(match.signalFile.getName());
			}
		}
		System.out.println(splitTimes.toString());
		this.save(false);
		System.out.println("DONE : Labeling Completed");
	}

	public float baseLineToValue(Signal beat, int x) {
		return (float) x * (beat.signalMax[1] - beat.signalMin[1]) / (baseLineFreq.length - 1) + beat.signalMin[1];
	}

	public int valueToBaseLine(Signal beat, float x) {
		return (int) ((x - beat.signalMin[1]) / (beat.signalMax[1] - beat.signalMin[1]) * (baseLineFreq.length - 1));
	}

	public float findSignalBaseLine(Signal beat) {
		baseLineFreq = new int[2001];
		for (int i = 0; i < beat.signal.size(); i++) {
			int ind = (int) ((beat.signal.get(i)[1] - beat.signalMin[1]) / (beat.signalMax[1] - beat.signalMin[1])
					* (baseLineFreq.length - 1));
			for (int j = 0; j < 10; j++) {
				if (ind + j < baseLineFreq.length) {
					baseLineFreq[ind + j] += (10 - j);
				}
				if (ind - j >= 0) {
					baseLineFreq[ind - j] += (10 - j);
				}
			}
		}
		int maxi = -1;
		int max = -1;
		for (int i = 0; i < baseLineFreq.length; i++) {
			if (baseLineFreq[i] > max) {
				max = baseLineFreq[i];
				maxi = i;
			}
		}
		return baseLineToValue(beat, maxi);
	}

	public void adjustLabel() {
		int MAX = baseLineFreq[this.valueToBaseLine(this, this.findSignalBaseLine(this))];
		// System.out.println("MAX = " + MAX);
		// float MAX2 = this.findSignalBaseLine2();
		// System.out.println("MAX2 = " + MAX2);
		if (MAX < 155)
			return;
		int pos = 0;
		for (int i = 0; i < this.annotation.annotation.size(); i++) {
			if (this.annotation.annotation.get(i)[4] == 0.0f) {
				if (this.annotation.annotation.get(i)[2] == 0 || this.annotation.annotation.get(i)[2] == 4) {
					for (; this.signal.get(pos)[0] < this.annotation.annotation.get(i)[0]; pos++)
						;
					int idx = -1;
					int max = -1;
					int BIG = 15, SMALL = 7;
					float factor = 0.7f;
					int a = 2, b = 1;
					int left = Math.max(0, i - 1), right = Math.min(i + 1, annotation.annotation.size() - 1);
					for (; left > 0
							&& (annotation.annotation.get(left)[4] != 0 || (annotation.annotation.get(left)[2] != 0
									&& annotation.annotation.get(left)[2] != 4)); left--)
						;
					for (; right < annotation.annotation.size() - 1
							&& (annotation.annotation.get(right)[4] != 0 || (annotation.annotation.get(right)[2] != 0
									&& annotation.annotation.get(right)[2] != 4)); right++)
						;
					if (annotation.annotation.get(i)[2] == 0) {
						int type = 2;
						for (int j = i + 1; j < right; j++) {
							if (annotation.annotation.get(j)[4] == 0) {
								type = annotation.annotation.get(j)[2].intValue();
							}
						}
						if (type == 1) {
							BIG = 35;
							SMALL = 30;
							factor = 0.7f;
							a = 2;
							b = 3;
							// System.out.println("(P " + pos);
						} else if (type == 2) {
							BIG = 16;
							SMALL = 25;
							factor = 0.1f;
							a = 10;
							b = 1;
							// System.out.println("(N " + pos);
						} else if (type == 3) {
							BIG = 40;
							SMALL = 30;
							factor = 0.7f;
							// System.out.println("(T " + pos);
						}
					} else if (annotation.annotation.get(i)[2] == 4) {
						int type = 2;
						for (int j = i - 1; j > left; j--) {
							if (annotation.annotation.get(j)[4] == 0) {
								type = annotation.annotation.get(j)[2].intValue();
							}
						}
						if (type == 1) {
							BIG = 12;
							SMALL = 7;
							factor = 0.6f;
							// System.out.println("P) " + pos);
						} else if (type == 2) {
							BIG = 20;
							SMALL = 25;
							factor = 0.1f;
							// System.out.println("N) " + pos);
						} else if (type == 3) {
							BIG = 15;
							SMALL = 30;
							factor = 0.7f;
							// System.out.println("T) " + pos);
						}
					}
					int leftPos = pos, rightPos = pos;
					for (; leftPos > 0
							&& this.signal.get(leftPos)[0] > this.annotation.annotation.get(left)[0]; leftPos--)
						;
					if (left == 0)
						leftPos = 0;
					for (; rightPos < this.signal.size() - 1
							&& this.signal.get(rightPos)[0] < this.annotation.annotation.get(right)[0]; rightPos++)
						;
					if (right == this.annotation.annotation.size() - 1)
						rightPos = this.signal.size() - 1;
					leftPos = (a * leftPos + b * pos) / (a + b);
					rightPos = (a * rightPos + b * pos) / (a + b);
					for (int j = Math.min(rightPos, Math.min(pos + BIG, this.signal.size() - 1)); j >= Math.max(leftPos,
							Math.max(pos - BIG, 0)); j--) {
						int freq = baseLineFreq[valueToBaseLine(this, this.signal.get(j)[1])];
						if (freq > max) {
							max = freq;
						}
					}
					// int currFreq = baseLineFreq[valueToBaseLine(this,
					// this.signal.get(i)[1])];
					// int newFreq = 0;
					if (this.annotation.annotation.get(i)[2] == 0) {
						for (int j = Math.min(rightPos, Math.min(pos + SMALL, this.signal.size() - 4)); j >= Math
								.max(leftPos, Math.max(pos - BIG, 0)); j--) {
							int freq = baseLineFreq[valueToBaseLine(this, this.signal.get(j)[1])];
							if (freq > max * factor && Math.abs(this.signal.get(j + 3)[1] - this.signal.get(j)[1])
									/ (this.signal.get(j + 3)[0] - this.signal.get(j)[0]) < 2.5f) {
								idx = j;
								// newFreq = freq;
								break;
							}
						}
					} else if (this.annotation.annotation.get(i)[2] == 4) {
						for (int j = Math.max(leftPos, Math.max(pos - SMALL, 0)); j <= Math.min(rightPos,
								Math.min(pos + BIG, this.signal.size() - 4)); j++) {
							int freq = baseLineFreq[valueToBaseLine(this, this.signal.get(j)[1])];
							if (freq > max * factor && Math.abs(this.signal.get(j + 3)[1] - this.signal.get(j)[1])
									/ (this.signal.get(j + 3)[0] - this.signal.get(j)[0]) < 2.5f) {
								idx = j;
								// newFreq = freq;
								break;
							}
						}
					}
					if (idx >= 0
					// && !(currFreq > max * factor && Math
					// .abs(zScore(this.signal.get(pos)[1], 1) -
					// zScore(this.signal.get(idx)[1], 1)) > 0.15)
					) {
						this.annotation.annotation.get(i)[0] = this.signal.get(idx)[0];
					}
				}
			}
		}
	}

	public float findSignalBaseLine2() {
		float sum = 0;
		baseSignal = new float[this.signal.size()];
		for (int i = 0; i < this.signal.size(); i++) {
			for (int j = -7; j <= 7; j++) {
				if (i + j >= 0 && i + j < this.signal.size()) {
					// baseSignal[i] += Math.pow(this.signal.get(i)[1] -
					// this.signal.get(i + j)[1], 2);
					float val = Math.abs(zScore(this.signal.get(i)[1], 1) - zScore(this.signal.get(i + j)[1], 1));
					if (val < 0.1f) {
						baseSignal[i] += (0.1f - val) * (0.1f - val) / 0.01f;
					}
				}
			}
		}
		for (int i = 0; i < this.signal.size(); i++) {
			sum += baseSignal[i];
		}
		return sum;
	}

	public void adjustLabel2() {
		this.findSignalBaseLine(this);
		int MM = 0;
		for (int i = 0; i < this.baseLineFreq.length; i++) {
			MM = Math.max(MM, this.baseLineFreq[i]);
		}
		this.findSignalBaseLine2();
		int pos = 0;
		for (int i = 0; i < this.annotation.annotation.size(); i++) {
			if (this.annotation.annotation.get(i)[4] == 0.0f) {
				if (this.annotation.annotation.get(i)[2] == 0 || this.annotation.annotation.get(i)[2] == 4) {
					for (; this.signal.get(pos)[0] < this.annotation.annotation.get(i)[0]; pos++)
						;
					int idx = -1;
					float max = -1;
					int BIG = 15, SMALL = 7;
					float factor = 0.7f;
					int left = Math.max(0, i - 1), right = Math.min(i + 1, annotation.annotation.size() - 1);
					for (; left > 0
							&& (annotation.annotation.get(left)[4] != 0 || (annotation.annotation.get(left)[2] != 0
									&& annotation.annotation.get(left)[2] != 4)); left--)
						;
					for (; right < annotation.annotation.size() - 1
							&& (annotation.annotation.get(right)[4] != 0 || (annotation.annotation.get(right)[2] != 0
									&& annotation.annotation.get(right)[2] != 4)); right++)
						;
					if (annotation.annotation.get(i)[2] == 0) {
						int type = 2;
						for (int j = i + 1; j < right; j++) {
							if (annotation.annotation.get(j)[4] == 0) {
								type = annotation.annotation.get(j)[2].intValue();
							}
						}
						if (type == 1) {
							BIG = 15;
							SMALL = 5;
							factor = 0.4f;
						} else if (type == 2) {
							BIG = 15;
							SMALL = 7;
							factor = 0.3f;
						} else if (type == 3) {
							BIG = 40;
							SMALL = 40;
							factor = 0.0f;
						}
					} else if (annotation.annotation.get(i)[2] == 4) {
						int type = 2;
						for (int j = i - 1; j > left; j--) {
							if (annotation.annotation.get(j)[4] == 0) {
								type = annotation.annotation.get(j)[2].intValue();
							}
						}
						if (type == 1) {
							BIG = 30;
							SMALL = 7;
							factor = 0.93f;
						} else if (type == 2) {
							BIG = 10;
							SMALL = 25;
							factor = 0.05f;
						} else if (type == 3) {
							BIG = 15;
							SMALL = 50;
							factor = 0.1f;
						}
					}
					int leftPos = pos, rightPos = pos;
					for (; leftPos > 0
							&& this.signal.get(leftPos)[0] > this.annotation.annotation.get(left)[0]; leftPos--)
						;
					for (; rightPos < this.signal.size() - 1
							&& this.signal.get(rightPos)[0] < this.annotation.annotation.get(right)[0]; rightPos++)
						;
					leftPos = (3 * leftPos + pos) / 4;
					rightPos = (3 * rightPos + pos) / 4;
					for (int j = Math.min(rightPos, Math.min(pos + BIG, this.signal.size() - 1)); j >= Math.max(leftPos,
							Math.max(pos - BIG, 0)); j--) {
						float freq = baseSignal[j]
								* this.baseLineFreq[this.valueToBaseLine(this, this.signal.get(j)[1])];
						if (freq > max) {
							max = freq;
						}
					}
					if (this.annotation.annotation.get(i)[2] == 0) {
						for (int j = Math.min(rightPos, Math.min(pos + SMALL, this.signal.size() - 4)); j >= Math
								.max(leftPos, Math.max(pos - BIG, 0)); j--) {
							float freq = baseSignal[j]
									* this.baseLineFreq[this.valueToBaseLine(this, this.signal.get(j)[1])];
							if (freq > max * factor && Math.abs(this.signal.get(j + 3)[1] - this.signal.get(j)[1])
									/ (this.signal.get(j + 3)[0] - this.signal.get(j)[0]) < 2.5f) {
								idx = j;
								break;
							}
						}
					} else {
						for (int j = Math.max(leftPos, Math.max(pos - SMALL, 0)); j <= Math.min(rightPos,
								Math.min(pos + BIG, this.signal.size() - 4)); j++) {
							float freq = baseSignal[j]
									* this.baseLineFreq[this.valueToBaseLine(this, this.signal.get(j)[1])];
							if (freq > max * factor && Math.abs(this.signal.get(j + 3)[1] - this.signal.get(j)[1])
									/ (this.signal.get(j + 3)[0] - this.signal.get(j)[0]) < 2.5f) {
								idx = j;
								break;
							}
						}
					}
					if (idx >= 0) {
						this.annotation.annotation.get(i)[0] = this.signal.get(idx)[0];
					}
				}
			}
		}
	}

	public boolean isSTabnormal(float stStart, float stEnd, float prStart, float prEnd) {
		float meanST = 0, meanPR = 0;
		int count = 0, count2 = 0;
		for (int i = 0; i < signal.size(); i++) {
			if (signal.get(i)[0] >= stStart && signal.get(i)[0] <= stEnd) {
				meanST += signal.get(i)[1];
				count++;
			}
			if (signal.get(i)[0] >= stStart && signal.get(i)[0] <= stEnd) {
				meanPR += signal.get(i)[1];
				count2++;
				break;
			}
		}
		meanST /= count;
		meanPR /= count2;
		return (meanST < meanPR + 0.5 && meanST > meanPR - 0.5);
	}

}
