import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Random;

public class AgricultureManagementSystem extends Application {
    public static Admin<Farmer> admin;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Agriculture Management System");

        // Input fields
        TextField farmerNameField = new TextField();
        TextField cropsField = new TextField();
        TextField subsidyField = new TextField();

        // Output area
        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);

        // Buttons
        Button addFarmerButton = new Button("Add Farmer");
        Button sendUpdateButton = new Button("Send Update");
        Button viewCropProductionButton = new Button("View Crop Production");

        // Layout
        VBox layout = new VBox(10);
        layout.getChildren().addAll(
                new Label("Farmer Name:"), farmerNameField,
                new Label("Crops (comma-separated):"), cropsField,
                new Label("Subsidy Info (description, waiver, coverage):"), subsidyField,
                addFarmerButton, sendUpdateButton, viewCropProductionButton,
                new Label("Output:"), outputArea
        );

        Scene scene = new Scene(layout, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initialize admin
        try {
            admin = new Admin<>();
        } catch (InsufficientDataException e) {
            outputArea.appendText("Error: " + e.getMessage() + "\n");
        }

        // Start real-time data updates
        new Thread(new RealTimeData(outputArea)).start();

        // Add Farmer Action
        addFarmerButton.setOnAction(e -> {
            String name = farmerNameField.getText();
            String[] cropsArray = cropsField.getText().split(",");
            ArrayList<String> crops = new ArrayList<>();
            for (String crop : cropsArray) {
                crops.add(crop.trim());
            }
            try {
                admin.addFarmer(new Farmer(name, crops));
                outputArea.appendText("Farmer added: " + name + "\n");
                farmerNameField.clear();
                cropsField.clear();
            } catch (InsufficientDataException ex) {
                outputArea.appendText("Error adding farmer: " + ex.getMessage() + "\n");
            }
        });

        // Send Update Action
        sendUpdateButton.setOnAction(e -> {
            String[] subsidyInfo = subsidyField.getText().split(",");
            if (subsidyInfo.length < 3) {
                outputArea.appendText("Error: Please provide complete subsidy info.\n");
                return;
            }
            String description = subsidyInfo[0].trim();
            float waiver = Float.parseFloat(subsidyInfo[1].trim());
            float coverage = Float.parseFloat(subsidyInfo[2].trim());

            try {
                admin.sendUpdate(new Subsidy(description, waiver, coverage));
                outputArea.appendText("Update sent: " + description + "\n");
                subsidyField.clear();
            } catch (InsufficientDataException | UpdateException ex) {
                outputArea.appendText("Error sending update: " + ex.getMessage() + "\n");
            }
        });

        // View Crop Production Action
        viewCropProductionButton.setOnAction(e -> {
            outputArea.appendText("Crop Production Details:\n");
            admin.printCropProductionDetails(outputArea);
        });
    }
}

// Existing classes remain unchanged...

class InsufficientDataException extends Exception { 
    InsufficientDataException(String msg) { super(msg); } 
}

class UpdateException extends Exception { 
    UpdateException(String msg) { super(msg); } 
}

class Update<T> {
    T updateContent;

    Update(T updateContent) throws InsufficientDataException {
        if (updateContent == null) throw new InsufficientDataException("Update content cannot be null.");
        this.updateContent = updateContent;
    }

    public void print() { System.out.println(updateContent); }
}

class Subsidy extends Update<String> {
    float waiver, coverage;

    Subsidy(String updateContent, float waiver, float coverage) throws InsufficientDataException {
        super(updateContent);
        this.waiver = waiver;
        this.coverage = coverage;
    }

    @Override
    public void print() { System.out.println(updateContent + " Waiver: " + waiver + " Coverage: " + coverage); }
}

class Request extends Update<String> {
    String farmerName;

    Request(String updateContent, String farmerName) throws InsufficientDataException {
        super(updateContent);
        if (farmerName == null || farmerName.isEmpty()) throw new InsufficientDataException("Farmer name cannot be null or empty.");
        this.farmerName = farmerName;
    }

    @Override
    public void print() { System.out.println(updateContent + ": request by " + farmerName); }
}

class Farmer {
    String name;
    ArrayList<String> crops;
    Update<?> update = null;

    Farmer(String name, ArrayList<String> crops) throws InsufficientDataException {
        if (name == null || name.isEmpty()) throw new InsufficientDataException("Farmer name cannot be null or empty.");
        if (crops == null || crops.isEmpty()) throw new InsufficientDataException("Crops cannot be null or empty.");
        this.name = name;
        this.crops = crops;
    }

    public void receiveUpdate() throws UpdateException {
        if (update == null) throw new UpdateException("No update available for farmer " + name);
        System.out.println(this.name + " received update: ");
        update.print();
        update = null;
    }

    public void sendRequest(String s) {
        try {
            Request request = new Request(s, name);
            AgricultureManagementSystem.admin.logRequest(request);
        } catch (InsufficientDataException e) { System.out.println("Error sending request: " + e.getMessage()); }
    }
}

class Admin<T extends Farmer> {
    private ArrayList<T> userBase;
    private ArrayList<Request> requestList;

    Admin() throws InsufficientDataException {
        userBase = new ArrayList<>();
        requestList = new ArrayList<>();
    }

    public void addFarmer(T farmer) throws InsufficientDataException {
        userBase.add(farmer);
    }

    public void sendUpdate(Update<?> u) throws UpdateException {
        if (userBase.isEmpty()) throw new UpdateException("No farmers to send updates to.");
        for (T f : userBase) f.update = u;
        for (T f : userBase) f.receiveUpdate();
    }

    public void logRequest(Request r) { requestList.add(r); }

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

    public void printRequests() { for (Request r : requestList) r.print(); }
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
        String[] prices = {"Wheat: 16000 rs/ton", "Rice: $14000 rs/ton", "Corn: 12000 rs/ton", "Barley: 12500 rs/ton"};
        return prices[random.nextInt(prices.length)];
    }

    private String getCropRecommendations() {
        String[] recommendations = {"Plant Wheat", "Plant Rice", "Plant Corn", "Plant Barley"};
        return recommendations[random.nextInt(recommendations.length)];
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(15000);
                // Update the output area with real-time data
                outputArea.appendText("\n[Real-Time Data Updates]\n");
                outputArea.appendText("Weather Forecast: " + getWeatherForecast() + "\n");
                outputArea.appendText("Market Prices: " + getMarketPrices() + "\n");
                outputArea.appendText("Crop Recommendations: " + getCropRecommendations() + "\n");
            } catch (InterruptedException e) {
                System.out.println("Real-time data update interrupted.");
            }
        }
    }
}
