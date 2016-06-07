import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeSet;

public class Marker {
	protected TreeSet<Float> times = new TreeSet<Float>();
	protected ArrayList<TreeSet<Float>> signals = new ArrayList<TreeSet<Float>>();

	public Marker(File f) {
		for (int i = 0; i < Signal.SIGNALS.length; i++) {
			signals.add(new TreeSet<Float>());
		}
		if (f != null) {
			Scanner file;
			try {
				file = new Scanner(f);
				int index = -1;
				while (file.hasNext()) {
					if (file.hasNextFloat()) {
						Float time = times.size() > 0 ? times.last() : null;
						times.add(file.nextFloat());
						if (time != null) {
							time = (time + times.last()) / 2;
							if (index != -1) {
								signals.get(index).add(time);
								index = -1;
							}
						}
					} else {
						String signal = file.next();
						for (int i = 0; i < Signal.SIGNALS.length; i++) {
							if (Signal.SIGNALS[i].equals(signal)) {
								index = i;
							}
						}
					}
				}
			} catch (FileNotFoundException e) {
				f = null;
			}
		}
	}

	public void save(File f) {
		try {
			PrintStream p = new PrintStream(f);
			Float prev = null;
			for (Float x : times) {
				for (int i = 0; i < signals.size(); i++) {
					TreeSet<Float> ts = signals.get(i);
					if (prev != null && ts.ceiling(prev) != null && ts.ceiling(prev).equals(ts.floor(x))) {
						p.println(Signal.SIGNALS[i]);
						break;
					}
				}
				prev = x;
				p.println(x);
			}
			p.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
