import java.util.ArrayList;
import java.util.Scanner;

public class AgricultureManagementSystem 
{
    public static Admin<Farmer> admin;

    public static void main(String[] args) 
    {
        try 
        {
            admin = new Admin<>();
            admin.sendUpdate(new Subsidy("Discount on seeds for bottom 5% earning farmers", 20, 5));
        } 
        catch (InsufficientDataException | UpdateException e) { System.out.println(e.getMessage()); }
    }
}

class InsufficientDataException extends Exception { InsufficientDataException(String msg) { super(msg); } }

class UpdateException extends Exception { UpdateException(String msg) { super(msg); } }

class Update<T> 
{
    T updateContent;

    Update(T updateContent) throws InsufficientDataException 
    {
        if (updateContent == null) throw new InsufficientDataException("Update content cannot be null.");
        this.updateContent = updateContent;
    }

    public void print() { System.out.println(updateContent); }
}

class Subsidy extends Update<String> 
{
    float waiver, coverage;

    Subsidy(String updateContent, float waiver, float coverage) throws InsufficientDataException 
    {
        super(updateContent);
        this.waiver = waiver;
        this.coverage = coverage;
    }

    @Override
    public void print() { System.out.println(updateContent + " Waiver: " + waiver + " Coverage: " + coverage); }
}

class Scheme extends Update<String> 
{
    float coverage;

    Scheme(String updateContent, float coverage) throws InsufficientDataException 
    {
        super(updateContent);
        this.coverage = coverage;
    }

    @Override
    public void print() { System.out.println(updateContent + " Coverage: " + coverage); }
}

class Request extends Update<String> 
{
    String farmerName;

    Request(String updateContent, String farmerName) throws InsufficientDataException 
    {
        super(updateContent);
        if (farmerName == null || farmerName.isEmpty()) throw new InsufficientDataException("Farmer name cannot be null or empty.");
        this.farmerName = farmerName;
    }

    @Override
    public void print() { System.out.println(updateContent + ": request by " + farmerName); }
}

class Farmer 
{
    String name;
    ArrayList<String> crops;
    Update<?> update = null;

    Farmer(String name, ArrayList<String> crops) throws InsufficientDataException 
    {
        if (name == null || name.isEmpty()) throw new InsufficientDataException("Farmer name cannot be null or empty.");
        if (crops == null || crops.isEmpty()) throw new InsufficientDataException("Crops cannot be null or empty.");
        this.name = name;
        this.crops = crops;
    }

    public void receiveUpdate() throws UpdateException 
    {
        if (update == null) throw new UpdateException("No update available for farmer " + name);
        System.out.println(this.name + " received update: ");
        update.print();
        update = null;
    }

    public void sendRequest(String s) 
    {
        try 
        {
            Request request = new Request(s, name);
            AgricultureManagementSystem.admin.logRequest(request);
        } 
        catch (InsufficientDataException e) { System.out.println("Error sending request: " + e.getMessage()); }
    }
}

class Admin<T extends Farmer> 
{
    private ArrayList<T> userBase;
    private ArrayList<Request> requestList;

    Admin() throws InsufficientDataException 
    {
        Scanner sc = new Scanner(System.in);
        userBase = new ArrayList<>();
        requestList = new ArrayList<>();

        System.out.println("Enter number of farmers:");
        int n = sc.nextInt();
        sc.nextLine();

        for (int i = 0; i < n; i++) 
        {
            System.out.println("Enter farmer name:");
            String name = sc.nextLine();

            System.out.println("Enter number of crops:");
            int c = sc.nextInt();
            sc.nextLine();

            ArrayList<String> crops = new ArrayList<>();
            System.out.println("Enter crops:");
            for (int j = 0; j < c; j++) {
                crops.add(sc.nextLine());
            }

            userBase.add((T) new Farmer(name, crops));
        }
        sc.close();
    }

    public void sendUpdate(Update<?> u) throws UpdateException 
    {
        if (userBase.isEmpty()) throw new UpdateException("No farmers to send updates to.");
        for (T f : userBase) f.update = u;
        for (T f : userBase) f.receiveUpdate();
    }

    public void logRequest(Request r) { requestList.add(r); }

    class CropTracker<K> 
    {
        K crop;
        int count = 1;

        CropTracker(K crop) { this.crop = crop; }

        void increment() { count++; }

        boolean equalsTo(K crop) { return this.crop.toString().equalsIgnoreCase(crop.toString()); }
    }

    public void printCropProductionDetails() 
    {
        ArrayList<CropTracker<String>> cropList = new ArrayList<>();
        
        for (T f : userBase) 
        {
            for (String crop : f.crops) 
            {
                boolean cropExists = false;
                for (CropTracker<String> cropTracker : cropList) 
                {
                    if (cropTracker.equalsTo(crop)) 
                    {
                        cropTracker.increment();
                        cropExists = true;
                        break;
                    }
                }
                if (!cropExists) { cropList.add(new CropTracker<>(crop)); }
            }
        }

        for (CropTracker<String> cropTracker : cropList) { System.out.println(cropTracker.crop + ": " + cropTracker.count); }
    }

    public void printRequests() { for (Request r : requestList) r.print(); }
}