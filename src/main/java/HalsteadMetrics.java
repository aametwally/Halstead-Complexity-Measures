/**
 * @author  Ahmed Metwally
 */

public class HalsteadMetrics {
	// Check if the parameters are set or not before calculating any metric
	// make the original parameters valid for division
	
	public int DistOperators, DistOperands, TotOperators, TotOperands;
	
	
	private int Vocabulary=0;
	private int Proglen=0; 
	private double CalcProgLen=0; 
	private double Volume=0; 
	private double Difficulty=0;
	private double Effort=0;  
	private double TimeReqProg=0;
	private double TimeDelBugs=0;
	
	
	

	public HalsteadMetrics() {
		DistOperators=0;
		DistOperands=0;
		TotOperators=0;
		TotOperands=0;
	}
	
	public void setParameters(int x, int y, int z, int m)
	{
		DistOperators=x;
		DistOperands=y;
		TotOperators=z;
		TotOperands=m;
	}
	
	
	public int getVocabulary()
	{
		Vocabulary=DistOperators+DistOperands;
		System.out.println("Vocabulary= "+ Vocabulary);
		return Vocabulary;
	}
	
	public void getProglen()
	{
		Proglen=TotOperators+TotOperands;
		System.out.println("Program Length= "+ Proglen);
	}
	
	public void getCalcProgLen()
	{
		CalcProgLen = DistOperators*(Math.log(DistOperators) / Math.log(2)) + DistOperands*(Math.log(DistOperands) / Math.log(2));
		System.out.println("Calculated Program Length= "+ CalcProgLen);
	}
	
	public void getVolume()
	{
		Volume=(TotOperators+TotOperands)*(Math.log(DistOperators+DistOperands)/Math.log(2));
		System.out.println("Volume= "+ Volume);
	}
	
	public void getDifficulty()
	{
		Difficulty=(DistOperators/2)*(TotOperands/(double)DistOperands);// 
		System.out.println("Difficulty= "+ Difficulty);
	}
	
		
	public void getEffort()
	{
		Effort=((DistOperators/2)*(TotOperands/(double)DistOperands)) * ((TotOperators+TotOperands)*(Math.log(DistOperators+DistOperands)/Math.log(2)));
		System.out.println("Effort= "+ Effort);
	}
	
	
	public void getTimeReqProg()
	{
		TimeReqProg=(((DistOperators/2)*(TotOperands/(double)DistOperands)) * ((TotOperators+TotOperands)*(Math.log(DistOperators+DistOperands)/Math.log(2)))) /18;
		System.out.println("Time Required to Program= "+ TimeReqProg);
	}
	
	
	
	public void getTimeDelBugs()
	{
		TimeDelBugs = ((TotOperators+TotOperands)*(Math.log(DistOperators+DistOperands)/Math.log(2))) / 3000;
		System.out.println("Number of delivered bugs= "+ TimeDelBugs);
	}
	
	
}