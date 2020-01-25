package Controllers;

import Services.*;
import Services.IncomingCommunication.IncomingStartStop;
import Services.OutgoingCommunication.OutgoingThread;
import Services.Utilities.IpPortValidator;
import Services.Utilities.MethodHelper;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static Services.Constants.*;

public class MainWindow_Controller {

    private static Stage stage;

    @FXML
    Pane pane;

    //region Toggles

    //Toggles
    @FXML
    private Toggle toggleInvalidRequestor;
    @FXML
    private Toggle toggleInvalidReceiver;
    @FXML
    private Toggle toggleTokenToWrongUser;
    @FXML
    private Toggle toggleRandomToken;
    @FXML
    private Toggle toggleEnctyptNonceWithWrongKey;
    @FXML
    private Toggle toggleEncryptWithWrongKey;
    @FXML
    private Toggle toggleEncryptWithWrongNumericalResponse;
    @FXML
    private Toggle toggleEncryptFileWithWrongKey;

    ToggleGroup radioButtonsGroup;

    //endregion Toggles

    //region Settings Section
    @FXML
    private Button btnEditSettings;
    @FXML
    private Button btnAddNeighbor;
    @FXML
    private Button btnRemoveNeighbor;
    @FXML
    private ListView lvNeighbors;
    @FXML
    private TextField txtSharedPassword;
    @FXML
    private TextField txtAddNeighborIPPort;
    @FXML
    private TextField txtMyPort;
    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtMACPassword;
    @FXML
    private TextField txtPrivatePassword;
    @FXML
    private TextField txtServerAddress;
    //endregion Settings Section

    //region Chat Section
    @FXML
    private Button btnSendFile;
    @FXML
    private Button btnSendMessage;
    @FXML
    private TextArea taMessage;
    @FXML
    private TextArea taChat;
    private static TextArea s_taChat;
    @FXML
    private ListView lvConnectedNeighbors;
    private static ListView s_lvConnectedNeighbors;
    @FXML
    private Button btnConnect;
    //endregion Chat Section

    //region Variables
    static Settings settings;
    static Map<String,String> neighbors;
    static Hasher hasher;
    static DateFormat df;
    static Date date;
    static String sharedPassword;
    static String macPassword;
    static String privatePassword;
    static String serverAddress;
    static String NeighborToGetFile;
    //endregion Variables

    //region Initialize
    public void initialize(){

        radioButtonsGroup = new ToggleGroup();
        toggleInvalidRequestor.setToggleGroup(radioButtonsGroup);
        toggleInvalidRequestor.setUserData(INAVLID_REQUESTOR);
        toggleInvalidReceiver.setToggleGroup(radioButtonsGroup);
        toggleInvalidReceiver.setUserData(INVALID_RECEIVER);
        toggleTokenToWrongUser.setToggleGroup(radioButtonsGroup);
        toggleTokenToWrongUser.setUserData(TOKEN_TO_WRONG_USER);
        toggleRandomToken.setToggleGroup(radioButtonsGroup);
        toggleRandomToken.setUserData(RANDOM_TOKEN);
        toggleEnctyptNonceWithWrongKey.setToggleGroup(radioButtonsGroup);
        toggleEnctyptNonceWithWrongKey.setUserData(ENCRYPT_NONCE_WITH_WRONG_KEY);
        toggleEncryptWithWrongKey.setToggleGroup(radioButtonsGroup);
        toggleEncryptWithWrongKey.setUserData(ENCRYPT_WITH_WRONG_KEY);
        toggleEncryptWithWrongNumericalResponse.setToggleGroup(radioButtonsGroup);
        toggleEncryptWithWrongNumericalResponse.setUserData(ENCRYPT_WITH_WRONG_NUMERICAL_RESPONSE);
        toggleEncryptFileWithWrongKey.setToggleGroup(radioButtonsGroup);
        toggleEncryptFileWithWrongKey.setUserData(ENCRYPT_FILE_WITH_WRONG_KEY);

        radioButtonsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                Toggle selectedToggle = radioButtonsGroup.getSelectedToggle();

                //set all values to false
                for (String toggleName :customMessagesToggles.keySet()) {
                    customMessagesToggles.put(toggleName, false);
                }

                if (selectedToggle != null) {
                    if(selectedToggle.isSelected()){
                        System.out.println(selectedToggle.getUserData().toString());
                        customMessagesToggles.put(selectedToggle.getUserData().toString(), true);
                    }
                }
            }
        });

        settings = new Settings();

        txtPrivatePassword.setText(settings.getPrivatePassword());
        txtServerAddress.setText(settings.getServerAddress());
        txtMyPort.setText(settings.getMyPort());
        txtSharedPassword.setText(settings.getSharedPassword());
        txtMACPassword.setText(settings.getMACPassword());
        //passwords
        sharedPassword = settings.getSharedPassword();
        privatePassword = settings.getPrivatePassword();
        macPassword = settings.getMACPassword();

        btnConnect.setStyle("-fx-background-color: Green");
        s_lvConnectedNeighbors = lvConnectedNeighbors;
        s_lvConnectedNeighbors.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onlineNeighbors_MouseClick(s_lvConnectedNeighbors.getSelectionModel().getSelectedIndex());
                stage = (Stage)((Node)((EventObject) event).getSource()).getScene().getWindow();
            }
        });
        /*
        s_lvConnectedNeighbors.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (newPropertyValue)
                {
                    //on focus
                }
                else
                {
                    //lost focus
                    btnSendFile.setDisable(true);
                    s_lvConnectedNeighbors.getSelectionModel().clearSelection();
                }
            }
        });
        */
        s_taChat = taChat;
        hasher = new Hasher();
        df = new SimpleDateFormat("HH:mm:ss");
        UpdateNeighborsList();
    }
    //endregion Initialize

    //region Methods

    /**
     * Show an alert
     * @param message alert message
     * @param alertType alert type
     */
    public static void showAlert(String message, Alert.AlertType alertType){
        Alert alert = new Alert(alertType, message);
        alert.showAndWait();
    }

    /**
     * Ask the user if wants to accept or decline a file transfer
     * @param filename filename
     * @param fromUser usert that sent the file transfer request
     * @return
     */
    private static boolean incomanigFileConfirmation(String filename , String fromUser) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Incoming New File");
        alert.setHeaderText(fromUser+" sent you a file. do you accept download the file?");
        alert.setContentText(filename);

        // option != null.
        Optional<ButtonType> option = alert.showAndWait();

        if (option.get() == null) {
            return false;
        } else if (option.get() == ButtonType.OK) {
            return true;
        } else if (option.get() == ButtonType.CANCEL) {
           return false;
        } else {
           return  false;
        }
    }

    //region Online Neighbors

    /**
     * Update the online neighbors
     */
    public static void updateOnlineNeighbors(){
        Platform.runLater(() -> {
            s_lvConnectedNeighbors.getItems().clear();
            for (Map.Entry<String,String> item:OnlineNeighbors.GetOnlineNeighbors().entrySet()) {
                s_lvConnectedNeighbors.getItems().add(item.getKey() + " ("+item.getValue()+")");
            }
        });
    }
    //endregion Online Neighbors

    /**
     * Send a message
     */
    public void SendMessage(){
        String message = taMessage.getText();
        taMessage.setText("");

        if(message.trim().equals("")){
            showAlert("Cannot send an empty message", Alert.AlertType.WARNING);
            return;
        }

        PrintMessageToScreen(message);

        if(OnlineNeighbors.GetOnlineNeighborsIPPort().size() > 0) {
            new OutgoingThread(OnlineNeighbors.GetOnlineNeighborsIPPort(), sharedPassword, macPassword, Constants.MSG_MESSAGE, message).start();
        }
        if(OnlineNeighbors.GetBadHmacOnlineNeighborsIPPort().size() > 0) {
            new OutgoingThread(OnlineNeighbors.GetBadHmacOnlineNeighborsIPPort(), sharedPassword, macPassword, Constants.MSG_MESSAGE, message).start();
        }
    }

    /**
     * Print message to the screen from a neighbor
     * @param message the message
     * @param neighbor neighbor
     */
    public static void PrintMessageToScreen(String message, String neighbor){
        neighbor = neighbor.replace("/", "");
        date = new Date();

        String neighborName = "";

        if(!neighbor.equals("")) {
            for (Map.Entry<String, String> item : OnlineNeighbors.GetOnlineNeighbors().entrySet()) {
                if (item.getValue().equals(neighbor)) {
                    neighborName = item.getKey();
                    break;
                }
            }
        }

        final String printToScreen = "["+df.format(date)+"] " + neighborName + ": " + message;

        Platform.runLater(() -> {
            s_taChat.appendText(printToScreen +  "\n");
        });
    }

    /**
     * Print own messages to the screen
     * @param message message
     */
    private void PrintMessageToScreen(String message){
        date = new Date();
        final String printToScreen = "["+df.format(date)+"] " + Globals.MY_USERNAME + ": " + message;
        s_taChat.appendText(printToScreen +  "\n");
    }

    /**
     * Ask a neighbor is he wants a file
     */
    public void AskToSendFile(){
        btnSendFile.setDisable(true);
        s_lvConnectedNeighbors.getSelectionModel().clearSelection();

        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            Globals.FILE_NAME_TO_SEND = selectedFile.getName();
            Globals.FILE_PATH_TO_SEND = selectedFile.getPath();
            new OutgoingThread(NeighborToGetFile, sharedPassword, macPassword, Constants.MSG_SENDFILE ,Globals.FILE_NAME_TO_SEND).start();
        }
    }

    /**
     * Ask the user where to save a file
     * @param filename
     * @param neighbor
     */
    public static void SaveFile(String filename, String neighbor ) {
        //check neighbor name by ip:port
        final String neighborName = OnlineNeighbors.GetOnlineNeighborName(neighbor);
        Globals.IS_TOKEN = true;
        Platform.runLater(() -> {
            //show confirmation dialog
            boolean isAccept = incomanigFileConfirmation(filename ,neighborName);

            if(isAccept)
            {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save file");
                fileChooser.setInitialFileName(filename);
                    //before open file chooser show dialog to accept or decline the file transfer.
                    //only if accept file transfer then show file save.

                    File savedFile = fileChooser.showSaveDialog(stage);
                    if (savedFile != null) {
                        System.out.println("Save file path: "+ savedFile.getPath());
                        //1.open a new listener thread for the file transfer with random port.
                        Globals.FILE_TRANSFER_PORT = MethodHelper.GetRandomNumberInRange(1000,2000);
                        IncomingStartStop.startIncoming(Globals.FILE_TRANSFER_PORT,savedFile.getPath(), Constants.INCOMING_FILE);
                        //2.send back ok
                        //new OutgoingThread(neighbor.split(":")[0]+":"+randomPort, sharedPassword, Constants.MSG_OK ,String.valueOf(randomPort)).start();
                        new OutgoingThread(neighbor, sharedPassword, macPassword, Constants.MSG_OK ,String.valueOf(Globals.FILE_TRANSFER_PORT)).start();
                        //3.save the path for the file.
                    }
                    else {
                        new OutgoingThread(neighbor, sharedPassword, macPassword, Constants.MSG_NO).start();
                    }
            }
            else{
                new OutgoingThread(neighbor, sharedPassword, macPassword, Constants.MSG_NO).start();
            }
        });
    }

    /**
     * Display an alert of an disconfirmation of a file transfer
     * @param neighbor neighbor IP:Port
     */
    public static void DeclineSendFile(String neighbor){
        final String neighborName = OnlineNeighbors.GetOnlineNeighborName(neighbor);
        Platform.runLater(() -> {
            showAlert(neighborName + " declined the file transfer request", Alert.AlertType.INFORMATION);
        });
    }

    /***
     * Display an alert wehn finishing to send a file
     */
    public static void FinishedSendingFile(String type){
        Platform.runLater(() -> {
            if(type.equals(Constants.MSG_ACK))
                showAlert("File successfully sent " + Globals.FILE_NAME_TO_SEND, Alert.AlertType.CONFIRMATION);
            else if(type.equals(Constants.MSG_FAILED))
                showAlert("Failed to send file " + Globals.FILE_NAME_TO_SEND, Alert.AlertType.WARNING);
        });
    }

    /**
     * Display an alert when finishing to send a file
     * @param filePath file path
     */
    public static void FinishedGettingFile(String filePath, String type){
        Platform.runLater(() -> {
            if(type.equals(Constants.MSG_ACK))
                showAlert( "Done getting file at:\n" + filePath, Alert.AlertType.CONFIRMATION);
            else if(type.equals(Constants.MSG_FAILED))
                showAlert("Failed to save file:\n "+ filePath, Alert.AlertType.WARNING);
        });
        IncomingStartStop.stopIncoming(Constants.INCOMING_FILE);
    }

    /**
     * Online neighbors mouse click event
     * @param index
     */
    public void onlineNeighbors_MouseClick(int index){
        if(index >= 0) {
            Object[] onlineNeighbors_IP = OnlineNeighbors.GetOnlineNeighbors().values().toArray();
            NeighborToGetFile = onlineNeighbors_IP[index].toString();
            btnSendFile.setDisable(false);
            //showAlert(onlineNeighbors_IP[index].toString());
        }
    }

    /**
     * Connect and disconnect from chat
     */
    public void ConnectToChat(){
        if(btnConnect.getText().equals("Connect")) {
            if(txtUsername.getText().equals("") || txtUsername.getText().contains(" ") || txtUsername.getText().contains("@")){
                showAlert("Please enter valid username before connecting", Alert.AlertType.WARNING);
                return;
            }
            if(btnEditSettings.getText().equals("Save Settings")) {
                showAlert("Please save settings before connecting", Alert.AlertType.WARNING);
                return;
            }
            btnConnect.setText("Disconnect");
            Globals.MY_USERNAME = txtUsername.getText();
            txtUsername.setDisable(true);
            IncomingStartStop.startIncoming(Integer.parseInt(settings.getMyPort()),Globals.MY_USERNAME,Constants.INCOMING_MESSAGE);
            new OutgoingThread(neighbors, sharedPassword, macPassword, Constants.MSG_HELLO, Globals.MY_USERNAME + " " + Constants.HELLO_FIRST).start();
            btnConnect.setStyle("-fx-background-color: Red");
            taMessage.setDisable(false);
            btnSendMessage.setDisable(false);
        }
        else{
            btnConnect.setText("Connect");
            new OutgoingThread(neighbors, sharedPassword, macPassword, Constants.MSG_BYE).start();
            OnlineNeighbors.ClearOnlineNeighbors();
            OnlineNeighbors.ClearBadHmacOnlineNeighbors();
            s_lvConnectedNeighbors.getItems().clear();
            btnConnect.setStyle("-fx-background-color: Green");
            txtUsername.setDisable(false);
            taMessage.setDisable(true);
            btnSendMessage.setDisable(true);
            IncomingStartStop.stopIncoming(Constants.INCOMING_MESSAGE);
        }
    }

    //region Settings

    /**
     * Add neighbor to the neighbors list
     */
    public void addNeighbor(){
        if(!txtAddNeighborIPPort.getText().equals("") && IpPortValidator.validate(txtAddNeighborIPPort.getText())) {
            String[] ipPort = txtAddNeighborIPPort.getText().split(":");
            btnRemoveNeighbor.setDisable(false);
            settings.addNeighbor(ipPort[0], ipPort[1]);
            txtAddNeighborIPPort.setText("");
            UpdateNeighborsList();
        }
        else{
            showAlert("Please enter correct IP:Port", Alert.AlertType.WARNING);
        }
    }

    /**
     * Remove a neighbor from the neighbors list
     */
    public void removeNeighbor(){
        //String neighborToRemove = lvNeighbors.getSelectionModel().getSelectedItem().toString();
        int index = lvNeighbors.getSelectionModel().getSelectedIndex();
        if(index >= 0) {
            Object[] neighborsNames = neighbors.keySet().toArray();
            settings.removeNeighbor(neighborsNames[index].toString());
            UpdateNeighborsList();
        }
        else{
            showAlert("Please select a neighbor to remove", Alert.AlertType.WARNING);
        }
    }

    /**
     * Update the neighbors list
     */
    private void UpdateNeighborsList(){
        OnlineNeighbors.SetNeighborsList(settings.getAllNeighbors());
        neighbors = settings.getAllNeighbors();
        lvNeighbors.getItems().clear();
        if(neighbors.size() == 0){
            btnRemoveNeighbor.setDisable(true);
        }
        else{
            //lvNeighbors.setItems(FXCollections.observableArrayList(neighbors.keySet()));
            for (String ip:neighbors.keySet()) {
                lvNeighbors.getItems().add(ip + ":" + neighbors.get(ip));
            }
        }
    }

    /**
     * Enable / Disable the editing of the settings
     */
    public void OnOffSettings(){
        if(btnConnect.getText().equals("Disconnect")){
            showAlert("Settings cannot be edited while connected to chat", Alert.AlertType.WARNING);
            return;
        }

        if(btnEditSettings.getText().equals("Edit Settings")) {
            btnEditSettings.setText("Save Settings");
            btnAddNeighbor.setDisable(false);
            if(neighbors.size() > 0){
                btnRemoveNeighbor.setDisable(false);
            }
            lvNeighbors.setDisable(false);
            txtSharedPassword.setDisable(false);
            txtMACPassword.setDisable(false);
            txtMyPort.setDisable(false);
            txtAddNeighborIPPort.setDisable(false);
            txtServerAddress.setDisable(false);
            txtPrivatePassword.setDisable(false);
        }
        else{
            if(txtMACPassword.getText().equals("")){
                showAlert("MAC password cannot be empty", Alert.AlertType.WARNING);
                return;
            }
            if(txtSharedPassword.getText().equals("")){
                showAlert("Shared password cannot be empty", Alert.AlertType.WARNING);
                return;
            }
            if(txtMyPort.getText().equals("")){
                showAlert("My port cannot be empty", Alert.AlertType.WARNING);
                return;
            }
            if(txtPrivatePassword.getText().equals("")){
                showAlert("Private password cannot be empty", Alert.AlertType.WARNING);
                return;
            }
            if(txtServerAddress.getText().equals("")){
                showAlert("Server address cannot be empty", Alert.AlertType.WARNING);
                return;
            }
            if(!txtMyPort.getText().matches("[0-9]+")) {
                showAlert("Port can be only numbers", Alert.AlertType.WARNING);
                return;
            }

            btnEditSettings.setText("Edit Settings");
            btnAddNeighbor.setDisable(true);
            btnRemoveNeighbor.setDisable(true);
            txtAddNeighborIPPort.setDisable(true);
            lvNeighbors.setDisable(true);
            //shared password
            txtSharedPassword.setDisable(true);
            sharedPassword = txtSharedPassword.getText();
            settings.setSharedPassword(sharedPassword);
            //mac password
            txtMACPassword.setDisable(true);
            macPassword = txtMACPassword.getText();
            settings.setMACPassword(macPassword);
            //private password
            txtPrivatePassword.setDisable(true);
            privatePassword = txtPrivatePassword.getText();
            settings.setPrivatePassword(privatePassword);
            //server address
            txtServerAddress.setDisable(true);
            serverAddress = txtServerAddress.getText();
            settings.setServerAddress(serverAddress);
            //my port
            txtMyPort.setDisable(true);
            String port = txtMyPort.getText();
            settings.setMyPort(port);
            //save all changes
            settings.SaveChanges();

        }
    }
    //endregion Settings

    //endregion Methods
}
