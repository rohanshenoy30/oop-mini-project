import java.util.Scanner;

public class main{
    public static void main(String[] args) {
        Admin admin = new Admin();
        admin.sendUpdate(new Subsidy("Discount on seends for bottom 5% earning farmers", 20, 5));

    }
}

class Update{
    String updateContent;
    Update(String updateContent){this.updateContent = updateContent;}
    public void print(){
        System.out.println(updateContent);
    } 
}

class Subsidy extends Update{
    float waiver, coverage;
    Subsidy(String updateContent, float waiver, float coverage){
        super(updateContent);
        this.waiver = waiver;
        this.coverage = coverage;
    }
    @Override
    public void print(){
        System.out.println(updateContent + "Waiver : " + waiver + " Coverage : " + coverage);
    }
}

class Scheme extends Update{
    float coverage;
    Scheme(String updateContent, float coverage){
        super(updateContent);
        this.coverage = coverage;
    }
    @Override
    public void print(){
        System.out.println(updateContent + " Coverage : " + coverage);
    }
}

class Farmer{

/*
    Register with Admin
    Recieve Updates on schemes, subsidies, farmin practices
    Fetch realtime data on prices, forcasts
    Communicate with government for pest control, irrigation and policy
    post reports about crop poduction for government to recieve
*/
    String name;
    Update u = null;
    Farmer(String name) {
        this.name = name;
    }
    
    public void recieveUpdate(){
        if(u != null){
            System.out.println(this.name + " recieved update : ");
            u.print();
            u = null;
        }
    }
}

class Admin{
    /*
        Register Farmers
        Send updates on Schemes, subsidies, farmin practices
        broadcast realtime data
        communicate with farmers on pest control, irrigation and policy
        fetch reports on crop production
    */
   private Farmer userBase[] = null;
   Admin(){
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        userBase = new Farmer[n];
        for(int i = 0; i < n; i++){
            userBase[i] = new Farmer(sc.nextLine());
        }
   }

   public void sendUpdate(Update u){
        for(Farmer f : userBase){
            f.u = u;
        }
        for(Farmer f : userBase){
            f.recieveUpdate();
        }
   }
}