package application;
import javafx.application.*;
import javafx.scene.control.*;
import javafx.scene.*;
import javafx.stage.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.Random;

public class Main extends Application
{
	public static Admin<Farmer> admin;
	
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Agriculture Management System");

        TextField farmerNameField = new TextField();
        TextField cropsField = new TextField();
        TextField subsidyField = new TextField();

        TextArea outputArea = new TextArea(); outputArea.setEditable(false);
        TextArea realTimeArea = new TextArea(); realTimeArea.setEditable(false);
        TextArea cropDetailsArea = new TextArea(); cropDetailsArea.setEditable(false);
        TextArea requestArea = new TextArea(); requestArea.setEditable(false);

        Button addFarmerButton = new Button("Add Farmer");
        Button sendSubsidyButton = new Button("Send Subsidy");

        GridPane layout = new GridPane(30, 30);
       
        VBox farmerLayout = new VBox(10);
        farmerLayout.getChildren().addAll(
                new Label("Farmer Name:"), 
                farmerNameField,
                new Label("Crops (comma-separated):"), 
                cropsField, 
                addFarmerButton
                );
        
        VBox outputLayout = new VBox(10);
        outputLayout.getChildren().addAll(new Label("Output:"), outputArea);
        
        VBox realTimeLayout = new VBox(10);
        realTimeLayout.getChildren().addAll(new Label("Real Time Updates: "), realTimeArea);
        
        VBox cropProductionLayout = new VBox(10);
        cropProductionLayout.getChildren().addAll(new Label("Crop production details: "), cropDetailsArea);
        
        VBox requestLayout = new VBox(10);
        requestLayout.getChildren().addAll(new Label("Pending requests: "), requestArea);
        
        VBox subsidyLayout = new VBox(10);
        subsidyLayout.getChildren().addAll(new Label("Subsidy content: "), subsidyField, sendSubsidyButton);
        
        layout.add(farmerLayout, 0, 0);
        layout.add(outputLayout, 1, 0);
        layout.add(requestLayout, 0, 1);
        layout.add(subsidyLayout, 1, 1);
        layout.add(realTimeLayout, 0, 2);
        layout.add(cropProductionLayout, 1, 2);
        Scene scene = new Scene(layout, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        try {
            admin = new Admin<Farmer>(requestArea);
        } catch (InsufficientDataException e) {
            outputArea.appendText("Error: " + e.getMessage() + "\n");
        }

        new Thread(new RealTimeData(realTimeArea)).start();

        addFarmerButton.setOnAction(e -> {
            String name = farmerNameField.getText();
            String[] cropsArray = cropsField.getText().split(",");
            ArrayList<String> crops = new ArrayList<>();
            for (String crop : cropsArray) {
                crops.add(crop.trim());
            }
            try {
                admin.addFarmer(new Farmer(name, crops));
            } catch (InsufficientDataException ex) {
                outputArea.appendText("Error adding farmer: " + ex.getMessage() + "\n");
            }
            
            outputArea.appendText("Farmer added: " + name + "\n");
            farmerNameField.clear();
            cropsField.clear();
            
            cropDetailsArea.clear();
            admin.printCropProductionDetails(cropDetailsArea);
        });

        sendSubsidyButton.setOnAction(e -> {
        	String subsidyContent = subsidyField.getText();
        	outputArea.appendText("Subsidy deployed: " + subsidyContent + "\n");
        	try { admin.deploySubsidy(new Subsidy(subsidyContent)); }
        	catch(InsufficientDataException | UpdateException ex) { outputArea.appendText("Error: " + ex.getMessage()); } 
        });
    }
}

class InsufficientDataException extends Exception { 
    InsufficientDataException(String msg) { super(msg); } 
}

class UpdateException extends Exception { 
    UpdateException(String msg) { super(msg); } 
}

class Update {
    String updateContent;

    Update(String updateContent) throws InsufficientDataException {
        if (updateContent == null) throw new InsufficientDataException("Update content cannot be null.");
        this.updateContent = updateContent;
    }

    public String toString() { return updateContent; } 
    
    public static boolean equals(Update a, Update b) { return a.updateContent.equalsIgnoreCase(b.updateContent); }
}

class Subsidy extends Update {
    Subsidy(String updateContent) throws InsufficientDataException {
        super(updateContent);
    }
}

class Request extends Update {
    String farmerName;

    Request(String updateContent, String farmerName) throws InsufficientDataException {
        super(updateContent);
        if (farmerName == null || farmerName.isEmpty()) throw new InsufficientDataException("Farmer name cannot be null or empty.");
        this.farmerName = farmerName;
    }

    @Override
    public String toString() { return updateContent + ": request by " + farmerName + "\n"; }
}

class Farmer implements Runnable{
    String name;
    ArrayList<String> crops;
    Thread thread;
    Random random = new Random();
    
    final static String toolsArray[] = {"Fertilizer", "Pesticides", "Loan"};
    
    Farmer(String name, ArrayList<String> crops) throws InsufficientDataException {
        if (name == null || name.isEmpty()) throw new InsufficientDataException("Farmer name cannot be null or empty.");
        if (crops == null || crops.isEmpty()) throw new InsufficientDataException("Crops cannot be null or empty.");
        this.name = name;
        this.crops = crops;
        
        thread = new Thread(this, name);
        thread.start();
    }

    public void sendRequest(String content) {
        try {
            Request request = new Request(content, name);
            Main.admin.logRequest(request);
        } catch (InsufficientDataException e) { System.out.println("Error sending request: " + e.getMessage()); }
    }

	@Override
	public void run() {
		while(true)
		{
			int t = random.nextInt(10) + 5;
			
			try {Thread.sleep(2000 * t);}
			catch(InterruptedException e) { }
			
			String s;
			int i = random.nextInt(crops.size() + toolsArray.length);
			if(i < toolsArray.length) s = toolsArray[i];
			else s = crops.get(i - toolsArray.length) + " seeds";
			sendRequest(s);
		}
	}
}

class Admin<T extends Farmer> {
    private ArrayList<T> userBase;
    private ArrayList<Request> requestList;
    TextArea requestArea;

    Admin(TextArea requestArea) throws InsufficientDataException {
        userBase = new ArrayList<>();
        requestList = new ArrayList<>();
        this.requestArea = requestArea;
    }

    public void addFarmer(T farmer) throws InsufficientDataException {
        userBase.add(farmer);
    }

    public void deploySubsidy(Subsidy s) throws UpdateException {
        if (userBase.isEmpty()) throw new UpdateException("No farmers to send updates to.");
        
        ArrayList<Request> deleteList = new ArrayList<Request>();
        for(Request r : requestList) if(Update.equals(r, s)) deleteList.add(r);
        for(Request r : deleteList) requestList.remove(r);
        
        updateRequestArea();
    }

    public void logRequest(Request r) 
    { 
    	requestList.add(r);
    	updateRequestArea();
    }
    
    void updateRequestArea()
    {
    	requestArea.clear();
    	for(Request r : requestList) requestArea.appendText(r.toString());
    }

    class CropTracker<K> {
        K crop;
        int count = 1;

        CropTracker(K crop) { this.crop = crop; }

        void increment() { count++; }

        boolean equalsTo(K crop) { return this.crop.toString().equalsIgnoreCase(crop.toString()); }
    }

    public void printCropProductionDetails(TextArea outputArea) {
        ArrayList<CropTracker<String>> cropList = new ArrayList<>();

        for (T f : userBase) {
            for (String crop : f.crops) {
                boolean cropExists = false;
                for (CropTracker<String> cropTracker : cropList) {
                    if (cropTracker.equalsTo(crop)) {
                        cropTracker.increment();
                        cropExists = true;
                        break;
                    }
                }
                if (!cropExists) { cropList.add(new CropTracker<>(crop)); }
            }
        }

        for (CropTracker<String> cropTracker : cropList) {
            outputArea.appendText(cropTracker.crop + ": " + cropTracker.count + "\n");
        }
    }
}

class RealTimeData implements Runnable {
    private Random random = new Random();
    private TextArea outputArea;

    RealTimeData(TextArea outputArea) {
        this.outputArea = outputArea;
    }

    private String getWeatherForecast() {
        String[] forecasts = {"Sunny", "Rainy", "Cloudy", "Windy", "Stormy"};
        return forecasts[random.nextInt(forecasts.length)];
    }

    private String getMarketPrices() {
        String[] prices = {"Wheat: 16000 rs/ton", "Rice: 14000 rs/ton", "Corn: 12000 rs/ton", "Barley: 12500 rs/ton"};
        return prices[random.nextInt(prices.length)];
    }

    private String getCropRecommendations() {
        String[] recommendations = {"Plant Wheat", "Plant Rice", "Plant Corn", "Plant Barley"};
        return recommendations[random.nextInt(recommendations.length)];
    }

    @Override
    public void run() {
        while (true) {
            outputArea.clear();
            outputArea.appendText("\n[Real-Time Data Updates]\n");
            outputArea.appendText("Weather Forecast: " + getWeatherForecast() + "\n");
            outputArea.appendText("Market Prices: " + getMarketPrices() + "\n");
            outputArea.appendText("Crop Recommendations: " + getCropRecommendations() + "\n");
            try { Thread.sleep(10000); } 
            catch (InterruptedException e) { outputArea.appendText("ERROR: SYSTEM DOWN\n"); }
        }
    }
} 