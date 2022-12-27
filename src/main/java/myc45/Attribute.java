package myc45;

import java.util.ArrayList;
import java.util.List;

public class Attribute {
	public String       name;
	public int 			attr_index;
	public List<Values> values = new ArrayList<Values>();
	public double       gain_ratio_x =  0.0; //gain   = 0.0;
	
	public Attribute(String name,int attr_index){
		this.name = name;
		this.attr_index=attr_index;
	}
	
	public void Calc_gain_ratio_x (double info_t, int T) {
		double Ti;
		double info_x_t = 0.0;
		double split_info_x =  0.0;

		for (Values v : values) {
			v.Calc_info_t_i();
			Ti = 0;

			for(int i : v.classesCount)
				Ti += i;
			
			info_x_t += (Ti/T) * v.info_t_i;
			split_info_x += (-1 * (Ti/T) * (Math.log(Ti/T) / Math.log(2)) );
		}
		this.gain_ratio_x=0;
		if(split_info_x != 0)
			this.gain_ratio_x = (info_t - info_x_t)/split_info_x;
	}
	
	public void insertVal (String valueName, String itClass){
		for(Values v : values) {
			if(v.valueName.equals(valueName)){
				v.update(itClass);
				return;
			}
		}
		values.add(new Values(valueName, itClass));
	}

	public  void printAttribute(Attribute AttrNode) {
		if(AttrNode.values.size() == 0)
			return;

		System.out.println("Attr name: " + AttrNode.name + "  Attr index: " + AttrNode.attr_index + "  gain_ratio: " + AttrNode.gain_ratio_x);
		
		for(int v=0;v < AttrNode.values.size();v++){
			for(int pi=0;pi< AttrNode.values.get(v).classes.size();pi++){
				System.out.print(AttrNode.values.get(v).valueName +
						"[" + AttrNode.values.get(v).classes.get(pi) + ","+ AttrNode.values.get(v).classesCount.get(pi) + "]  ");
			}
			System.out.println("");
		}
		System.out.println("");
    }	
	
}
