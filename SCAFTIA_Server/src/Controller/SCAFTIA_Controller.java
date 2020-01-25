package Controller;

import Services.Hasher;
import Services.IncomingCommunication.IncomingStartStop;
import Services.Settings;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static Services.Constants.*;

public class SCAFTIA_Controller {

    @FXML
    private Toggle toggleInvalidNonce, toggleInvalidTargetName, toggleEncryptWrongRequestor, toggleEncryptWrongRecipient;

    private static Map<String, Boolean> customResponsesToggles  = new HashMap<String, Boolean>() {{
        put(INVALID_NONCE, false);
        put(INVALID_TARGET_NAME, false);
        put(ENCRYPT_WRONG_REQUESTOR, false);
        put(ENCRYPT_WRONG_RECIPIENT, false);
    }};

    @FXML
    private Button btnStartStop, btnEditSettings, btnAddUser, btnRemoveUser;

    @FXML
    private TextField tfIPPort, tfUsername, tfPassword, tfMacPass, tfSharedPass;

    @FXML
    private TextArea teMessages;
    private static TextArea s_teMessages;

    @FXML
    private ListView lvUsers;

    static Map<String,String> users;
    static Settings settings;
    ToggleGroup radioButtonsGroup;
    Hasher hasher;

    public void initialize(){
        radioButtonsGroup = new ToggleGroup();
        toggleInvalidNonce.setToggleGroup(radioButtonsGroup);
        toggleInvalidNonce.setUserData(INVALID_NONCE);
        toggleInvalidTargetName.setToggleGroup(radioButtonsGroup);
        toggleInvalidTargetName.setUserData(INVALID_TARGET_NAME);
        toggleEncryptWrongRequestor.setToggleGroup(radioButtonsGroup);
        toggleEncryptWrongRequestor.setUserData(ENCRYPT_WRONG_REQUESTOR);
        toggleEncryptWrongRecipient.setToggleGroup(radioButtonsGroup);
        toggleEncryptWrongRecipient.setUserData(ENCRYPT_WRONG_RECIPIENT);
        radioButtonsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                Toggle selectedToggle = radioButtonsGroup.getSelectedToggle();

                //set all values to false
                for (String toggleName :customResponsesToggles.keySet()) {
                    customResponsesToggles.put(toggleName, false);
                }

                if (selectedToggle != null) {
                    if(selectedToggle.isSelected()){
                        System.out.println(selectedToggle.getUserData().toString());
                        customResponsesToggles.put(selectedToggle.getUserData().toString(), true);
                    }
                }
            }
        });

        hasher = new Hasher();
        settings = new Settings();
        UpdateUsersList();
        btnStartStop.setStyle("-fx-background-color: Green");
        users = settings.getAllUsers();
        tfIPPort.setText(settings.getIPPort());
        tfMacPass.setText(settings.getMacPassword() == null ? "" : settings.getMacPassword());
        tfSharedPass.setText(settings.getSharedPassword() == null ? "" : settings.getSharedPassword());
        s_teMessages = teMessages;
    }
    /**
     * Show an alert
     * @param message alert message
     * @param alertType alert type
     */
    public static void showAlert(String message, Alert.AlertType alertType){
        Alert alert = new Alert(alertType, message);
        alert.showAndWait();
    }

    public static boolean IsSelectedCustomResponse(String toggleName){
        return customResponsesToggles.get(toggleName);
    }

    /**
     *
     * @param message
     */
    public static void PrintMessageToScreen(String message){
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        final String printToScreen = "["+df.format(date)+"] " + message;

        Platform.runLater(() -> {
            s_teMessages.appendText(printToScreen +  "\n");
        });
    }

    public void AddUser(){
        if(!tfUsername.getText().equals("") && !tfPassword.getText().equals("")) {
            btnAddUser.setDisable(false);
            String encryptedPassword = hasher.encryptPassword(tfPassword.getText());
            settings.addUser(tfUsername.getText(), encryptedPassword);
            tfPassword.setText("");
            tfUsername.setText("");
            UpdateUsersList();
        }
        else{
            showAlert("Please enter username and password", Alert.AlertType.WARNING);
        }
    }

    /**
     * Remove a neighbor from the neighbors list
     */
    public void RemoveUser(){
        int index = lvUsers.getSelectionModel().getSelectedIndex();
        if(index >= 0) {
            Object[] usernames = users.keySet().toArray();
            settings.removeUser(usernames[index].toString());
            UpdateUsersList();
        }
        else{
            showAlert("Please select a neighbor to remove", Alert.AlertType.WARNING);
        }
    }

    public void StartStopServer(){
        if(btnStartStop.getText().equals("Start Server")){
            btnStartStop.setText("Stop Server");
            btnStartStop.setStyle("-fx-background-color: Red");
            String serverIP = tfIPPort.getText().split(":")[0];
            int serverPort = Integer.parseInt(tfIPPort.getText().split(":")[1]);
            IncomingStartStop.startIncoming(serverIP,serverPort);
        }
        else{
            btnStartStop.setText("Start Server");
            btnStartStop.setStyle("-fx-background-color: Green");
            IncomingStartStop.stopIncoming();
        }
    }

    /**
     * Enable / Disable the editing of the settings
     */
    public void OnOffSettings(){
        if(btnStartStop.getText().equals("Stop Server")){
            showAlert("Settings cannot be edited while connected to chat", Alert.AlertType.WARNING);
            return;
        }

        if(btnEditSettings.getText().equals("Edit Settings")) {
            btnEditSettings.setText("Save Settings");
            btnAddUser.setDisable(false);
            if(users.size() > 0){
                btnRemoveUser.setDisable(false);
            }
            lvUsers.setDisable(false);
            tfIPPort.setDisable(false);
            tfPassword.setDisable(false);
            tfUsername.setDisable(false);
            tfMacPass.setDisable(false);
            tfSharedPass.setDisable(false);
        }
        else{
            if(tfIPPort.getText().equals("")){
                showAlert("Server IP:Port cannot be empty", Alert.AlertType.WARNING);
                return;
            }
            if(tfMacPass.getText().equals("")){
                showAlert("MAC password cannot be empty", Alert.AlertType.WARNING);
                return;
            }
            if(tfSharedPass.getText().equals("")){
                showAlert("Shared password cannot be empty", Alert.AlertType.WARNING);
                return;
            }
            btnEditSettings.setText("Edit Settings");
            tfPassword.setDisable(true);
            tfUsername.setDisable(true);
            tfSharedPass.setDisable(true);
            tfMacPass.setDisable(true);
            btnAddUser.setDisable(true);
            btnRemoveUser.setDisable(true);
            tfIPPort.setDisable(true);
            lvUsers.setDisable(true);
            //my port
            tfIPPort.setDisable(true);
            String IP_Port = tfIPPort.getText();
            settings.addIPPort(IP_Port);

            settings.addMacPassword(tfMacPass.getText());
            settings.addSharedPassword(tfSharedPass.getText());

            //save all changes
            settings.SaveChanges();
        }
    }

    /**
     * Update the neighbors list
     */
    private void UpdateUsersList(){
        users = settings.getAllUsers();
        lvUsers.getItems().clear();

        if(users.size() == 0){
            btnRemoveUser.setDisable(true);
        }
        else{
            //lvNeighbors.setItems(FXCollections.observableArrayList(neighbors.keySet()));
            for (String name:users.keySet()) {
                lvUsers.getItems().add(name);
            }
        }
    }

}
