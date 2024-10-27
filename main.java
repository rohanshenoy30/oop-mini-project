import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;

public class AgricultureManagementSystem
{
    public static Admin admin;

    public static void main(String[] args)
    {
        admin = new Admin();
        RealTimeData realTimeData = new RealTimeData();
        Thread dataUpdateThread = new Thread(realTimeData);
        dataUpdateThread.start();

        admin.sendUpdate(new Subsidy("Discount on seeds for bottom 5% earning farmers", 20, 5));
    }
}

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
    public void print() { System.out.println(updateContent + " Waiver : " + waiver + " Coverage : " + coverage); }
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

        System.out.println(this.name + " received update: ");
        update.print();
        update = null;
    }

    public void sendRequest(String s)
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
        System.out.print("Enter number of farmers: ");
        int n = sc.nextInt();
        userBase = new Farmer[n];

        for(int i = 0; i < n; i++)
        {
            sc.nextLine();
            System.out.print("Enter name of farmer " + (i + 1) + ": ");
            String name = sc.nextLine();

            System.out.print("Enter number of crops for " + name + ": ");
            int c = sc.nextInt();
            sc.nextLine();

            String crops[] = new String[c];
            for(int j = 0; j < c; j++)
            {
                System.out.print("Enter crop " + (j + 1) + ": ");
                crops[j] = sc.nextLine();
            }

            userBase[i] = new Farmer(name, crops);
        }
    }

    public void sendUpdate(Update u)
    {
        for(Farmer f : userBase) f.update = u;
        for(Farmer f : userBase) f.receiveUpdate();
    }

    public void logRequest(Request r) { requestList.add(r); }

    public void printCropProductionDetails()
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

    public void printRequests() { for(Request r : requestList) r.print(); }

    class CropTracker
    {
        String crop;
        int count = 1;

        CropTracker(String crop) { this.crop = crop; }

        void increment() { count++; }

        boolean equals(String crop)
        {
            return this.crop.equalsIgnoreCase(crop);
        }
    }
}

class RealTimeData implements Runnable
{
    private Random random = new Random();

    private String getWeatherForecast()
    {
        String[] forecasts = {"Sunny", "Rainy", "Cloudy", "Windy", "Stormy"};
        return forecasts[random.nextInt(forecasts.length)];
    }

    private String getMarketPrices()
    {
        String[] prices = {"Wheat: 16000 rs/ton", "Rice: $14000 rs/ton", "Corn: 12000 rs/ton", "Barley: 12500 rs/ton"};
        return prices[random.nextInt(prices.length)];
    }

    private String getCropRecommendations()
    {
        String[] recommendations = {"Plant Wheat", "Plant Rice", "Plant Corn", "Plant Barley"};
        return recommendations[random.nextInt(recommendations.length)];
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                Thread.sleep(15000);

                System.out.println("\n[Real-Time Data Updates]");
                System.out.println("Weather Forecast: " + getWeatherForecast());
                System.out.println("Market Prices: " + getMarketPrices());
                System.out.println("Crop Recommendations: " + getCropRecommendations());
                

            }
            catch (InterruptedException e)
            {
                System.out.println("Real-time data update interrupted.");
            }
        }
    }
}