package myc45;

import java.util.ArrayList;
import java.util.List;

public class Values {
	public String        valueName;
	public List<String>  classes      = new ArrayList<String>();
	public List<Integer> classesCount = new ArrayList<Integer>();
	public double        info_t_i        = 0.0;
	
	public Values(String valName, String newClass) {
		this.valueName = valName;
		this.classes.add(newClass);
		this.classesCount.add(1);
	}
	
	public void Calc_info_t_i() {
		int totalNumClasses = 0;
		for(int i : this.classesCount) {
			totalNumClasses += i;
		}
		
		this.info_t_i = 0.0;
		for(double d : classesCount) {
			this.info_t_i += (-1 * (d/totalNumClasses)) * (Math.log((d/totalNumClasses)) / Math.log(2));
		}
	}

	public void update(String itClass) {
		if(this.classes.contains(itClass)) {
			this.classesCount.set(this.classes.indexOf(itClass),
					this.classesCount.get(this.classes.indexOf(itClass)) + 1);
		}
		else{
			this.classes.add(itClass);
			this.classesCount.add(this.classes.indexOf(itClass), 1);
		}
	}
}
