import java.util.ArrayList;
import java.util.Scanner;

public class AgricultureManagementSystem
{
    public static Admin admin;

    public static void main(String[] args) 
    {
        admin = new Admin();
        admin.sendUpdate(new Subsidy("Discount on seends for bottom 5% earning farmers", 20, 5));
    }
}

/*
TODO: real time data
- possible implementation: create a thread that periodically updates the weather, market prices and crop recommendations (maybe every 15 seconds)
- when it updates the data, it randomly generates new data

TODO: input output
- the code to input all the farmer details is in the Admin constructor
- the user must be able to call the functions marked by ~, maybe by a menu driven interface

TODO (optional): exception handling
- just put some try-catch block somewhere idk

change the implementation idea and the code if you think there's a better way
*/

class Update
{
    String updateContent;
    
    Update(String updateContent) { this.updateContent = updateContent; }
    
    public void print() { System.out.println(updateContent); } 
}

class Subsidy extends Update
{
    float waiver, coverage;

    Subsidy(String updateContent, float waiver, float coverage)
    {
        super(updateContent);
        this.waiver = waiver;
        this.coverage = coverage;
    }

    @Override
    public void print() { System.out.println(updateContent + "Waiver : " + waiver + " Coverage : " + coverage); }
}

class Scheme extends Update
{
    float coverage;

    Scheme(String updateContent, float coverage)
    {
        super(updateContent);
        this.coverage = coverage;
    }

    @Override
    public void print() { System.out.println(updateContent + " Coverage : " + coverage); }
}

class Request extends Update
{
    String farmerName;

    Request(String updateContent, String farmerName)
    {
        super(updateContent);
        this.farmerName = farmerName;
    }

    @Override
    public void print() { System.out.println(updateContent + ": request by " + farmerName); }
}

class Farmer
{
    String name;
    String[] crops;

    Update update = null;

    Farmer(String name, String[] crops) 
    { 
        this.name = name; 
        this.crops = crops;
    }
    
    public void receiveUpdate()
    {
        if(update == null) return;

        System.out.println(this.name + " recieved update : ");
        update.print();
        update = null;
    }

    public void sendRequest(String s)   //~
    {
        Request request = new Request(s, name);
        AgricultureManagementSystem.admin.logRequest(request);
    }
}

class Admin
{
    private Farmer userBase[];
    private ArrayList<Request> requestList = new ArrayList<Request>();

    Admin()
    {
        Scanner sc = new Scanner(System.in);

        //prompt user for n value
        int n = sc.nextInt();
        userBase = new Farmer[n];
        //prompt user for farmer details in every iteration
        for(int i = 0; i < n; i++) 
        {
            //promt user for name
            String name = null; // = sc.nextLine(); (null is just a placeholder)
            //prompt user to enter number of crops
            int c = 1; // = sc.nextInt(); (1 is just a placeholder)
            //prompt user to enter crops
            String crops[] = new String[c];
            //for(int j = 0; j < c; j++) crops[j] = sc.nextLine();
            userBase[i] = new Farmer(name, crops);
        }
    }

    public void sendUpdate(Update u)
    {
        for(Farmer f : userBase) f.update = u;
        for(Farmer f : userBase) f.receiveUpdate();
    }

    public void logRequest(Request r) { requestList.add(r); }

    class CropTracker
    {
        String crop;
        int count = 1;

        CropTracker(String crop) { this.crop = crop; }

        void increment() { count++; }

        boolean equals(String crop)
        {
            if(this.crop.equalsIgnoreCase(crop)) return true;
            return false;
        }
    }
    
    public void printCropProductionDetails()        //~
    {
        ArrayList<CropTracker> cropList = new ArrayList<CropTracker>();
        for(Farmer f : userBase)
            for(String crop : f.crops)
            {
                boolean cropExists = false;
                for(CropTracker cropTracker : cropList)
                {
                    if(cropTracker.equals(crop)) 
                    {
                        cropTracker.increment();
                        cropExists = true;
                        break;
                    }
                }
                if(!cropExists) cropList.add(new CropTracker(crop));
            }
        
        for(CropTracker cropTracker : cropList) System.out.println(cropTracker.crop + ": " + cropTracker.count);
    }
    
    public void printRequests() { for(Request r : requestList) r.print(); }       //~

    /*
    public void printRealTimeData() //~
    {
        realTimeData.printWeatherForecast();
        realTimeData.printMarketPrices();
        realTimeData.printCropRecommendations();
    }
    */
}

/* 
class RealTimeData
{
    details at the top

    static void printWeatherForecast()
    {
        print weather forecast
    }

    static void printMarketPrices()
    {
        print market prices
    }

    static void printCropRecommendations()
    {
        print crop recommendations
    }
}
*/
