import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Annotation {
	protected float startTime, finishTime, duration;

	protected File annotationFile;
	protected String annotationName[]; // Time Sample# Type sub Chan Num (Aux)
	protected ArrayList<Float[]> annotation = new ArrayList<Float[]>();
	protected ArrayList<Float[]> interval = new ArrayList<Float[]>();
	protected ArrayList<Float> rRInterval = new ArrayList<Float>();
	// PHeight PRInterval(pFront, rPeak) QRSComplex(qFront, sEnd)
	// QTinterval(qPeak, tEnd) STSegment(sPeak, tFront)

	public Annotation(String fileName) {
		annotationFile = new File(Signal.outputPath
				+ fileName.substring(fileName.length() - 4) + "pu.txt");
		annotationName = "Time Sample# Type sub Chan Num (Aux)".split(" ");
	}

	public Annotation(File annotationFile, float startTime, float finishTime) {
		try {
			this.annotationFile = annotationFile;
			this.startTime = startTime;
			this.finishTime = finishTime;
			@SuppressWarnings("resource")
			Scanner sc = new Scanner(this.annotationFile);
			this.annotationName = sc.nextLine().trim().split("\\s+(#\\s+)*");
			for (int i = 0; i < this.annotationName.length; i++) {
				this.annotationName[i] = this.annotationName[i].trim();
			}
			boolean start = false, stop = false;
			float prevT = -1f;
			float prevP = -1f;
			while (sc.hasNext() && !stop) {
				Float row[] = new Float[this.annotationName.length];
				for (int j = 0; j < this.annotationName.length - 1; j++) {
					if (j == 0) {
						String tim = sc.next();
						if (tim.indexOf(":") < 0) {
							row[j] = Float.parseFloat(tim);
						} else {
							if (tim.length() <= 8) {
								row[j] = Float.parseFloat(tim.substring(0, 1))
										* 60
										+ Float.parseFloat(tim.substring(2));
							} else {
								row[j] = Float.parseFloat(tim.substring(0, 2))
										* 60
										+ Float.parseFloat(tim.substring(3));
							}
						}
						if (row[j] >= this.startTime && !start) {
							start = true;
							this.startTime = row[j];
						} else if (row[j] > this.finishTime && !stop) {
							stop = true;
							this.finishTime = row[j];
							this.duration = this.finishTime - this.startTime;
							break;
						}
					} else if (j == 2) {
						String typ = sc.next();
						// p=1, t=3, N=2, (=0, )=4, |=9, ^=8
						if (typ.equals("p"))
							row[j] = (float) 1;
						else if (typ.equals("N"))
							row[j] = (float) 2;
						else if (typ.equals("t"))
							row[j] = (float) 3;
						else if (typ.equals("("))
							row[j] = (float) 0;
						else if (typ.equals(")"))
							row[j] = (float) 4;
						else
							row[j] = Float.parseFloat(typ);
					} else {
						row[j] = sc.nextFloat();
					}
				}
				if (start && !stop) {
					if (row[2] == 3) {
						prevT = row[0];
						prevP = -1f;
					} else if (row[2] == 4) {
						if (prevP < 0 && prevT >= 0) {
							prevT = row[0];
							prevP = row[0];
						}
					} else if (row[2] == 0) {
						if (prevP >= 0 && prevT >= 0) {
							prevP = row[0];
							Float row2[] = new Float[this.annotationName.length];
							row2[0] = (prevT + prevP) / 2;
							row2[2] = (float) 9;
							row2[4] = (float) 0;
							this.annotation.add(row2);
							prevP = -1;
							prevT = -1;
						}
					}
					this.annotation.add(row);
				}
			}
			findInterval();
		} catch (FileNotFoundException e) {
			System.out.println("ERROR : File Not Found (Annotation)");
			// e.printStackTrace();
		}
	}

	public boolean insertRDone = false;

	public void insertR(Signal s, ArrayList<Point> a) {
		if (!insertRDone) {
			for (int i = 0; i < a.size(); i++) {
				int index = annotation.size();
				for (int j = 0; j < annotation.size(); j++) {
					if (s.annotation.annotation.get(j)[0] >= s.signal.get(a
							.get(i).y)[0]) {
						index = j;
						break;
					}
				}
				Float[] row = new Float[this.annotationName.length];
				row[0] = s.signal.get(a.get(i).y)[0];
				row[1] = s.signal.get(a.get(i).y)[a.get(i).x];
				row[2] = 8.0f;
				row[4] = (float) a.get(i).x - 1;
				annotation.add(index, row);
			}
			insertRDone = true;
		}
	}
	
	// p=1, t=3, N=2, (=0, )=4, |=9, ^=8
	// Time Sample# Type sub Chan Num (Aux)
	// PHeightTime PRInterval(pFront, r) QRSComplex(qFront, sEnd) QTinterval(qFront, tEnd) STSegment(sEnd, tFront)
	public void findInterval() {
		float pHeightTime=-1, pFront=-1, qrsFront=-1, qrsEnd=-1, tEnd=-1, tFront=-1, pEnd = -1, tmpOpen=-1,r= -1;
		boolean qrsFound = false, pFound = false;
		for (int i = 0; i < annotation.size(); i++) {
			if(annotation.get(i)[4]==0){
				if(annotation.get(i)[2]==0&&tmpOpen==-1){
					tmpOpen = annotation.get(i)[0];
				}
				else if(annotation.get(i)[2]==1&&tmpOpen!=-1){
					pHeightTime = annotation.get(i)[0];
					pFront = tmpOpen;
					pFound = true;
				}
				else if(annotation.get(i)[2]==2&&tmpOpen!=-1){
					qrsFront = tmpOpen;
					r = annotation.get(i)[0];
					qrsFound = true;
				}
				else if(annotation.get(i)[2]==3&&tmpOpen!=-1){
					tFront = tmpOpen;
					
				}
				else if(annotation.get(i)[2]==4&&tmpOpen!=-1){
					tmpOpen=-1;
					if(pFound){
						pFound = false;
						pEnd = annotation.get(i)[0];
					}
					if(qrsFound){
						qrsEnd = annotation.get(i)[0];
						qrsFound = false;
					}
					if(tFront!=-1){
//						System.out.println(tmpOpen+" "+tFront);
//						tFront = tmpOpen;
						tEnd = annotation.get(i)[0];
						rRInterval.add(r);
						Float[] row = new Float[7];
						row[0] = pHeightTime;
						row[1] = pFront;
						row[2] = pEnd;
						row[3] = qrsFront;
						row[4] = qrsEnd;
						row[5] = tFront;
						row[6] = tEnd;
//						System.out.println(tFront+" "+row[5]);
						tFront = -1;
						interval.add(row);
					}
				}
			}
			
		}
	}
	
	public float findSPeakTime(){
		return -1;
	}
	
	public float findQPeakTime(){
		return -1;
	}

	public float rrInterval(float r1, float r2) {
		return Math.abs(r1 - r2);
	}

	public boolean qtIntervalIsNormal(float q, float t, float r1, float r2) {
		return ((t - q) / rrInterval(r1, r2) == 0.42);
	}
}
