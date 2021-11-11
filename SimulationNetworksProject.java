package simulator;
import java.applet.Applet;
import java.awt.*;
import java.awt.Font;
import java.awt.event.*;

class ChoiceList extends Choice
{
   double data[];
   ChoiceList(String list[], double values[])
   {
	  for(int i=0;i<list.length;i++)
      {
		 super.addItem(list[i]); 
	  }  
      data = values;
      select(0);	  
   }  
   double getSelVal() 
   {
		int selIndex;
		selIndex = super.getSelectedIndex();
		return  data[selIndex];
   }
}

class DataPac
{
  double size, emTime;
  DataPac(double siz, double emit)
  {
    size=siz;
    emTime=emit;
  }
}

public class SimulationNetworksProject extends Applet
{
   String strLenList[] = new String[]{"10 km","100 km","1000 km"};      
   String strRateList[] = new String[]{"512 kbps","1 Mbps","10 Mbps","100 Mbps"};     
   String strSizeList[] = new String[]{"100 Bytes","500 Bytes","1 kBytes"};
   double valLenList[] = new double[]{10E3,100E3,1E6};
   double valRateList[] = new double[]{512E3,1E6,10E6,100E6};
   double valSizeList[] = new double[]{8E2,4E3,8E3};
   ChoiceList length = new ChoiceList(strLenList, valLenList);
   ChoiceList rate = new ChoiceList(strRateList, valRateList);
   ChoiceList size = new ChoiceList(strSizeList, valSizeList);
   //Creation of start and reset buttons
   Button start=new Button("Start");
   Button reset=new Button("Reset");
   Image rt;
   CommunicationLine com_obj;
   Thread proTimeThread; 
   TimerActivity timerActivity;
   boolean runSim = false;
   public void init()
   {
	 setFont(new Font ("Times New Roman", Font.BOLD, 15));  	 
     Label l1, l2, l3;	 
	 //adding length, rate and packet size combobox to applet
	 l1 = new Label("Length",Label.RIGHT);
	 add(l1);
     add(length); 
	 l2 = new Label("Rate",Label.RIGHT);
	 add(l2);
     add(rate); 
	 l3 = new Label("Packet Size",Label.RIGHT);
	 add(l3);
     add(size); 
	 add(start);
	 //Adding action for start button
	 start.addActionListener(
        new ActionListener()
        {
          public void actionPerformed (ActionEvent ae)
          {
			runSim = true;  //simulation action starts
			setAllEnable(false);   
			com_obj.setInitial(length.getSelVal(), rate.getSelVal()); //setInitial line
			com_obj.startPkt(size.getSelVal(),0);    
			timerActivity=new TimerActivity(1E-5,com_obj.timeCalc());  //setInitial timer
			proTimeThread = new Thread(timerActivity);			
			proTimeThread.start();
          }
        });            
     add(reset);
	 //Adding action for reset button
	 reset.addActionListener(
        new ActionListener()
        {      
          public void actionPerformed (ActionEvent ae)
          {//reset simulation action starts
			timerActivity.exitCurrent();
			runSim = false;
			setAllEnable(true);
            com_obj.transTim(0); 
            SimulationNetworksProject.this.repaint();  
          }
        });

	 rt = getImage(getDocumentBase(),"routericon.png");
	 //CommunicationLine creation
     com_obj = new CommunicationLine(125,120,388,10);
   }
   public void paint(Graphics g)
   {   
	 //Sender or Receiver Router setting to applet
   	  g.setColor(Color.black);
	  g.drawImage(rt, 50,110, this); 
	  g.drawImage(rt, 520,110, this); 
	  g.drawString("Sender",58,165);
	  g.drawString("Receiver",528,165);
	  g.setColor(Color.white);
      g.fillRect(125,120,388,10);
      g.setColor(Color.black);
      g.drawRect(125,120,388,10);	  
	  g.drawString("Propagation speed = 2.8 x 10^8 m/sec",180,170);
	  com_obj.pktMove(g);	  
   }
   void setAllEnable(boolean bv)
   {
    start.setEnabled(bv);
	size.setEnabled(bv);
    length.setEnabled(bv);
    rate.setEnabled(bv);    
   }
   //Timer Thread class creation
   class TimerActivity implements Runnable
   {
    double l, ctr, clck;
    public TimerActivity(double ck,double length)
    {
      l = length;
	  clck = ck;
      ctr = 0;
    }	
	void exitCurrent() 
	{
      l = ctr;
    }
	//method for thread running
    public void run()
    {
      while(SimulationNetworksProject.this.runSim)
      {
        ctr = ctr + clck;
        SimulationNetworksProject.this.com_obj.transTim(ctr);
        SimulationNetworksProject.this.repaint();
        if (ctr>=l)
        {
          SimulationNetworksProject.this.com_obj.rmPkts();
          SimulationNetworksProject.this.proTimeThread.suspend();
        }
        try  
		{
			SimulationNetworksProject.this.proTimeThread.sleep(50);  
		}
		catch (Exception ex) 
		{
			System.out.println(ex);
		}
      }
    }
  }
  public class CommunicationLine
  {
	int com_lineX, com_lineY, com_wid, com_ht; 
	DataPac pkt;
	//Propagation speed
	final double proSpeed = 2.8E+8; 
    double l, r, t;  //l=length, r=rate, t=time
	CommunicationLine(int clx, int cly, int wid, int ht)
	{
		com_lineX = clx;
		com_lineY = cly;
		com_wid = wid;
		com_ht = ht;
	}
	//method to set the initial values
	void setInitial(double length, double rate)
    {
      l = length;
      r = rate;
    }
	//method for making data packets to move
   void pktMove(Graphics g)
   {
    if (!(pkt==null))
    {
      //Time and Position Calculation
      double strt=t-pkt.emTime;
      double end=strt-(pkt.size/r);
      strt=strt*proSpeed*com_wid/l;
      end=end*proSpeed*com_wid/l;
      if(end<0) 
	  {
		  end=0;
	  }	  
      if(strt>com_wid) 
	  {
		  strt=com_wid;      
	  }	  
      g.setColor(Color.red);  
      g.fillRect(com_lineX+(int)(end),com_lineY+1,(int)(strt-end),com_ht-2);
    }
	timeconv(g,t);	
   }
   void timeconv(Graphics g, double t)
   {
	   g.setColor(Color.red);
	   g.drawString(convsTime(t),com_lineX+com_wid/2-10,com_lineY+com_ht+15);
   }
   //method for converting time numerical value
   String convsTime(double t)
  {
	int dotIndex;
	String rst, decpt, intpt, retVal;	
    rst = Double.toString(t*1000);
    dotIndex = rst.indexOf('.');
    decpt = rst.substring(dotIndex+1)+"000";
    decpt = decpt.substring(0,3);
    intpt = rst.substring(0,dotIndex);
	retVal = intpt+"."+decpt+" ms";
    return retVal;
  }
    //method for transmission time
	void transTim(double cur)
    {
     t=cur; 
     if(!(pkt == null))
     {
      if(cur > pkt.emTime+(pkt.size/r)+l*proSpeed)
      {
		 pkt =null;
      }
     }
    }
	//method to start sending the packets
    void startPkt(double strt, double emst)
    {
     pkt = new DataPac(strt,emst);
    }
	//method to remove the packets by making the pkt = null 
    void rmPkts()
    {
     pkt=null;
    }
	//method for time Calculation
    double timeCalc()
    {  
	  double eTime, oTime, totTime;
      eTime=(pkt.size/r);
      oTime=(l/proSpeed);
	  totTime = eTime+oTime;
      return totTime;
    }
 }
}