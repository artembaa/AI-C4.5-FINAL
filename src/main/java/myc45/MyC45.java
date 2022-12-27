package myc45;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.lang.Math;

public class MyC45 {
	static int input_class_offset    = 0;
	static int input_attr_offset	 = 1;

	public static void main(String[] args) throws IOException {
		// .csv data
		String file = "data_sets/agaricus-lepiota.data";

		// headers match dataset structure
		String[] headers = {
								"class",
								"cap-shape",
								"cap-surface",
								"cap-color",
								"bruises",
								"odor",
								"gill-attachment",
								"gill-spacing",
								"gill-size",
							  	"gill-color",
								"stalk-shape",
								"stalk-root",
								"stalk-surface-abv-ring",
								"stalk-surface-below-ring",
							  	"stalk-color-abv-ring",
								"stalk-color-below-ring",
								"veil-type",
								"veil-color",
								"ring-number",
							  	"ring-type",
								"spore-print-color",
								"population",
								"habitat"
		};
		int numAttributes = headers.length-1;

		
		//01. read whole dataset
		List<String> InputSampleList= new ArrayList<String>();
		int totalNumSamples = 0;
		Scanner scan;
		scan = new Scanner(new File(file));		
		while(scan.hasNextLine()){
			String inLine = scan.nextLine();
			InputSampleList.add(inLine);
			totalNumSamples ++;
		}


		//02. prepare accessory array
		int[] attributeUsedInClassification = new int[numAttributes];
		for(int i = 0; i < numAttributes; i++)
			attributeUsedInClassification[i] = 1; // all disabled


		//03. enable only randomly selected attributes
		int numAttributesForTraining = (int)Math.ceil(Math.sqrt(numAttributes));
		int count = 0;
		while(true)
		{
			int index = (int)(Math.random() * (numAttributes - 1));
			if (attributeUsedInClassification[index] == 1)
			{
				attributeUsedInClassification[index] = 0;
				count++;
				if (count == numAttributesForTraining)
					break;
			}
		}
		/*
			DEBUG
			if(numAttributesForTraining==5)
			{
				attributeUsedinClassification[0]=0;
				attributeUsedinClassification[4]=0;
				attributeUsedinClassification[8]=0;
				attributeUsedinClassification[12]=0;
				attributeUsedinClassification[16]=0;
			 }
		 */


		//04. training process - generates decisionTree
		List<Attribute> decisionTree = new ArrayList<Attribute>();

		int sampleCountClassified;
		boolean trainingProcessCompleted = false;
		int countUsedAttributes = 0;

		int totalNumSamplesTraining = totalNumSamples * 80/100;
		
		while(!trainingProcessCompleted)
		{
			List<String>  classes      = new ArrayList<String>();
			List<Integer> classesCount = new ArrayList<Integer>();				
			Attribute[] attributes = new Attribute[numAttributes];
			for(int x = 0; x < numAttributes; x++)
				attributes[x] = new Attribute(headers[x+input_attr_offset], x);
				
			sampleCountClassified = 0;
			for(int sampleIndex = 0; sampleIndex < totalNumSamplesTraining; sampleIndex++) {

				String[] lineData = InputSampleList.get(sampleIndex).split("\\t");

				// check if lineData can be classified already
				if (Tree_Find_Leaf(decisionTree,lineData) != null)
				{
					sampleCountClassified++;
					continue;
				}

				// classes data
				if(!classes.contains(lineData[input_class_offset])){
					classes.add(lineData[input_class_offset]);
					classesCount.add(classes.indexOf(lineData[input_class_offset]), 1);
				}
				else {
					classesCount.set(classes.indexOf(lineData[input_class_offset]),
							classesCount.get(classes.indexOf(lineData[input_class_offset])) + 1);
				}
			
				// insert data into attributes
				for(int x = 0; x < numAttributes; x++)
					if(attributeUsedInClassification[x]==0)
						attributes[x].insertVal (lineData[x+input_attr_offset], lineData[input_class_offset]);
			}

			if (classesCount.size() == 0 || sampleCountClassified == totalNumSamplesTraining)	{
				trainingProcessCompleted = true;
				continue;
			}
			
			double info_t = Calc_Info_T(classesCount);
			
			// calculate gain_ratio_x
			double max_gain_ratio_x = 0;
			int index_max_gain_ratio_x = 0;
			for(int x = 0; x < numAttributes; x++) {
				attributes[x].Calc_gain_ratio_x(info_t,totalNumSamplesTraining - sampleCountClassified);
				if(max_gain_ratio_x < attributes[x].gain_ratio_x)
				{
					max_gain_ratio_x = attributes[x].gain_ratio_x;
					index_max_gain_ratio_x = x;
				}
				// for(Attribute AttrNode : attributes)
				// 	AttrNode.printAttribute(AttrNode,1);
			}
			
			decisionTree.add(attributes[index_max_gain_ratio_x]);
			attributeUsedInClassification[index_max_gain_ratio_x] = 1;

			countUsedAttributes++;
			if(countUsedAttributes == numAttributesForTraining)	{
				trainingProcessCompleted = true;
				continue;
			}			

			// calculate most frequent class of training dataset on first iteration!
			if(sampleCountClassified == 0) {
				int mostFrequentClassCount = 0;
				for (int i = 0; i < classesCount.size(); i++){
					if (mostFrequentClassCount < classesCount.get(i)) {
						mostFrequentClassCount = classesCount.get(i);
						mostFrequentClassNameOnTraining = classes.get(i);
					}
				}				
			}
		}


		//05. classification test
		List<String>  classes      = new ArrayList<String>();
		List<Integer> classesCount = new ArrayList<Integer>();	
		List<Integer> classesCountTP = new ArrayList<Integer>();	
		List<Integer> classesCountFP = new ArrayList<Integer>();
		List<Integer> classesCountFN = new ArrayList<Integer>();
		List<Integer> classesCountTN = new ArrayList<Integer>();									
		int totalNumSamplesTest=totalNumSamples-totalNumSamplesTraining;

		for (int sampleIndex=totalNumSamplesTraining; sampleIndex < totalNumSamples; sampleIndex++) {
			String[] lineData = InputSampleList.get(sampleIndex).split("\\t");

			if(!classes.contains(lineData[input_class_offset])){
				classes.add(lineData[input_class_offset]);
				classesCount.add(classes.indexOf(lineData[input_class_offset]), 1);
				classesCountTP.add(classes.indexOf(lineData[input_class_offset]), 0);//reset
				classesCountFP.add(classes.indexOf(lineData[input_class_offset]), 0);//reset
				classesCountFN.add(classes.indexOf(lineData[input_class_offset]), 0);//reset
				classesCountTN.add(classes.indexOf(lineData[input_class_offset]), 0);//reset												
			}
			else {
				classesCount.set(classes.indexOf(lineData[input_class_offset]),
									classesCount.get(classes.indexOf(lineData[input_class_offset])) + 1);
			}
		}

		sampleCountClassified = 0;

		for (int sampleIndex=totalNumSamplesTraining;sampleIndex < totalNumSamples;sampleIndex++) {
			String[] lineData = InputSampleList.get(sampleIndex).split("\\t");
			String proposedClass = Tree_Classify(decisionTree,lineData);

			/*
			Чтобы понять, что эта диаграмма (TP,FP,FN,TN) означает, в пример приводят мальчика,
			 который сторожит овец (пусть это будет Вася) и волка, который нападает на овец ночью.
			 Вася любит пугать жителей ночью и будит всех со словами: “Волк!”.
			 Но также бывает, что волк на самом деле приходит ночью. Так вот:
			 когда Вася кричит и волк на самом деле появился, то это True Positive (TP).
			 Когда Вася кричит и волка нет, то это False Positive (FP).
			 Когда Вася не кричит и волк пришёл, то это False Negative (FN).
			 А когда Вася не кричит и волк не пришел, то это True Negative (TN).

			Другими словами, Positive и Negative - это предсказания нашей модели (изображена ли на этой картинке кошка),
			 а True и False- это оценка того, правильно ли определила модель наш класс (действительно ли на этой картинке кошка).
			 Крик Васи — предсказание модели. Пришёл Волк — то, что на самом деле произошло,
			 и относительно чего дается оценка (True или False) крику нашего Васи.
			 */

			for(int i=0;i < classes.size();i++)
			{
				//  Волк Пришёл
				if(classes.get(i).equals(lineData[input_class_offset]))
				{
					// Вася кричит 
					if(proposedClass.equals(classes.get(i)))
					{
						classesCountTP.set(classes.indexOf(classes.get(i)),
											classesCountTP.get(classes.indexOf(classes.get(i))) + 1);
						sampleCountClassified++;
					}
					// Вася не кричит 
					else
					{
						classesCountFN.set(classes.indexOf(classes.get(i)),
											classesCountFN.get(classes.indexOf(classes.get(i))) + 1);
					}

				}
				//  Волк НЕ пришёл
				else
				{
					// Вася кричит 
					if(proposedClass.equals(classes.get(i)))
					{
						classesCountFP.set(classes.indexOf(classes.get(i)),
											classesCountFP.get(classes.indexOf(classes.get(i))) + 1);
					}
					// Вася не кричит 
					else
					{
						classesCountTN.set(classes.indexOf(classes.get(i)),
											classesCountTN.get(classes.indexOf(classes.get(i))) + 1);
					}
				}
			}
		}


		//06. print decision tree and classification results
		for (Attribute AttrNode : decisionTree)
			AttrNode.printAttribute(AttrNode);
		for (int i=0;i < classes.size();i++){
			System.out.println("Class: " + classes.get(i) + 
								"  Total class samples: " + classesCount.get(i) + 
								"  TP: " + classesCountTP.get(i) +
								"  FN: " + classesCountFN.get(i) +
								"  FP: " + classesCountFP.get(i) +
								"  TN: " + classesCountTN.get(i) ); 

			double Precision=	((double)classesCountTP.get(i))/(classesCountTP.get(i)+classesCountFP.get(i));	
			double Recall=	((double)classesCountTP.get(i))/(classesCountTP.get(i)+classesCountFN.get(i));		
			double Accuracy=	((double)classesCountTP.get(i)+classesCountTN.get(i))/
										(classesCountTP.get(i)+classesCountFP.get(i)+classesCountFN.get(i)+classesCountTN.get(i));	
			double Specificity= ((double)classesCountTN.get(i))/(classesCountTN.get(i)+classesCountFP.get(i));	
			System.out.println("Precision: " + Precision);
			System.out.println("Recall: " + Recall);
			System.out.println("Accuracy: " + Accuracy);	
			System.out.println("Specificity: " + Specificity);			
			System.out.println("ROC TPR: " + Recall);		
			System.out.println("ROC FPR: " + (1-Specificity));		
			System.out.println("");							
								
		}
		System.out.println("Total test samples: " + totalNumSamplesTest + "  Total positevely classifed samples: " + sampleCountClassified );
		System.out.println("");
						
	}


	private static double Calc_Info_T(List<Integer> classesCount) {
		double info_t = 0.0;
		double temp = 0.0;
		
		int totalNumClasses = 0;
		for(int i : classesCount) {
			totalNumClasses += i;
		}
		
		for(double d : classesCount) {
			temp = (-1 * (d/totalNumClasses)) * (Math.log((d/totalNumClasses)) / Math.log(2));
			info_t += temp;
		}
		return info_t;
	}


	private static String Tree_Find_Leaf(List<Attribute> decisionTree, String[] lineData) {
        if(decisionTree.size() == 0)
			return null;
		for(Attribute attrNode : decisionTree) {
			for(int v = 0; v < attrNode.values.size(); v++){
				// reached leaf state
				if (
					attrNode.values.get(v).classes.size() == 1 && // there is ONLY 1 class associated
					// attrNode.values.get(v).classesCount.get(0) != 0 && // associated class is NOT empty
					attrNode.values.get(v).valueName.equals(lineData[attrNode.attr_index+input_attr_offset])
				)
					return attrNode.values.get(v).classes.get(0);
			}
		}	
		return null;
	}


	private static String mostFrequentClassNameOnTraining;
	private static String Tree_Classify(List<Attribute> DecisionTree, String[] lineData) {
		String _class = Tree_Find_Leaf(DecisionTree,lineData);
		if(_class == null)
			return mostFrequentClassNameOnTraining;
		return _class;
	}	
  
}
