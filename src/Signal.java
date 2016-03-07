import java.io.File;
import java.util.ArrayList;

public class Signal {
	private File signalFile;
	private String signalName[]; // Time Signal0..N
	private ArrayList<Float[]> signal = new ArrayList<Float[]>();// Time(s)
																	// Signal0..N(mV)
	private float signalMax[], signalMin[];// SignalAll(mV) Signal0..N(mV)

	public Signal(File signalFile) {
		this.signalFile = signalFile;
	}
}
