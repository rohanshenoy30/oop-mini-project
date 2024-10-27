import java.util.ArrayList;
import java.util.Scanner;

public class AgricultureManagementSystem {
    public static Admin<Farmer> admin;

    public static void main(String[] args) {
        admin = new Admin<>();
        admin.sendUpdate(new Subsidy("Discount on seeds for bottom 5% earning farmers", 20, 5));
    }
}

// Make Update class generic to handle different types of content
class Update<T> {
    T updateContent;
    
    Update(T updateContent) { 
        this.updateContent = updateContent; 
    }
    
    public void print() { 
        System.out.println(updateContent); 
    } 
}

// Extend Update with String type for Subsidy
class Subsidy extends Update<String> {
    float waiver, coverage;

    Subsidy(String updateContent, float waiver, float coverage) {
        super(updateContent);
        this.waiver = waiver;
        this.coverage = coverage;
    }

    @Override
    public void print() { 
        System.out.println(updateContent + " Waiver: " + waiver + " Coverage: " + coverage); 
    }
}

// Extend Update with String type for Scheme
class Scheme extends Update<String> {
    float coverage;

    Scheme(String updateContent, float coverage) {
        super(updateContent);
        this.coverage = coverage;
    }

    @Override
    public void print() { 
        System.out.println(updateContent + " Coverage: " + coverage); 
    }
}

// Extend Update with String type for Request
class Request extends Update<String> {
    String farmerName;

    Request(String updateContent, String farmerName) {
        super(updateContent);
        this.farmerName = farmerName;
    }

    @Override
    public void print() { 
        System.out.println(updateContent + ": request by " + farmerName); 
    }
}

class Farmer {
    String name;
    ArrayList<String> crops;  // Changed from array to ArrayList
    Update<?> update = null;  // Use wildcard generic for Update

    Farmer(String name, ArrayList<String> crops) { 
        this.name = name; 
        this.crops = crops;
    }
    
    public void receiveUpdate() {
        if(update == null) return;
        System.out.println(this.name + " received update: ");
        update.print();
        update = null;
    }

    public void sendRequest(String s) {
        Request request = new Request(s, name);
        AgricultureManagementSystem.admin.logRequest(request);
    }
}

// Make Admin class generic to handle different types of users
class Admin<T extends Farmer> {
    private ArrayList<T> userBase;  // Changed from array to ArrayList
    private ArrayList<Request> requestList;

    Admin() {
        Scanner sc = new Scanner(System.in);
        userBase = new ArrayList<>();
        requestList = new ArrayList<>();

        System.out.println("Enter number of farmers:");
        int n = sc.nextInt();
        sc.nextLine(); // Consume newline

        for(int i = 0; i < n; i++) {
            System.out.println("Enter farmer name:");
            String name = sc.nextLine();
            
            System.out.println("Enter number of crops:");
            int c = sc.nextInt();
            sc.nextLine(); // Consume newline
            
            ArrayList<String> crops = new ArrayList<>();
            System.out.println("Enter crops:");
            for(int j = 0; j < c; j++) {
                crops.add(sc.nextLine());
            }
            
            userBase.add((T) new Farmer(name, crops));  // Cast to T
        }
        sc.close();
    }

    public void sendUpdate(Update<?> u) {
        for(T f : userBase) f.update = u;
        for(T f : userBase) f.receiveUpdate();
    }

    public void logRequest(Request r) { 
        requestList.add(r); 
    }

    // Make CropTracker generic to handle different types of crop identifiers
    class CropTracker<K> {
        K crop;
        int count = 1;

        CropTracker(K crop) { 
            this.crop = crop; 
        }

        void increment() { 
            count++; 
        }

        boolean equals(K crop) {
            return this.crop.toString().equalsIgnoreCase(crop.toString());
        }
    }
    
    public void printCropProductionDetails() {
        ArrayList<CropTracker<String>> cropList = new ArrayList<>();
        for(T f : userBase) {
            for(String crop : f.crops) {
                boolean cropExists = false;
                for(CropTracker<String> cropTracker : cropList) {
                    if(cropTracker.equals(crop)) {
                        cropTracker.increment();
                        cropExists = true;
                        break;
                    }
                }
                if(!cropExists) {
                    cropList.add(new CropTracker<>(crop));
                }
            }
        }
        
        for(CropTracker<String> cropTracker : cropList) {
            System.out.println(cropTracker.crop + ": " + cropTracker.count);
        }
    }
    
    public void printRequests() { 
        for(Request r : requestList) r.print(); 
    }
}
