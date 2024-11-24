package com.complan.function_handler;

import java.time.LocalDateTime;
//import java.util.Vector;
import java.util.LinkedHashMap;
import com.complan.basic_classes.*;

public class UserHandler {
    private LinkedHashMap<String, User> Users;
    private LinkedHashMap<String, WMSlot> WMSlots;
    private LinkedHashMap<String, VPBooking> VPBookings;
    private LinkedHashMap<String, Requests> RequestsList;
    private String WMStatus;
    private boolean isLoggedIn = false;
    private User currentUser;

    static{
        System.loadLibrary("DB_Access");
    }

    public static native String ReadFromDB(String filename);

    public UserHandler() {
        this.Users = new LinkedHashMap<String, User>();
        this.WMSlots = new LinkedHashMap<String, WMSlot>();
        this.VPBookings = new LinkedHashMap<String, VPBooking>();
        this.RequestsList = new LinkedHashMap<String, Requests>();
        this.isLoggedIn = false;
        this.currentUser = null;
        /* DB acquire data and put in these Vectors logic */
    }

    // As a UserHandler object is needed for reading data, Reading data is done in separate method
    // Read from "user.txt"
    public void FetchUserData(){
        //UserHandler uh = new UserHandler();
        String allUserData = UserHandler.ReadFromDB("user.txt");
        String[] data = allUserData.split("\n");
        for (int i = 0;i < data.length(); i++){
            String[] userDetails = data[i].split(";");
            User user = new User(userDetails[0], userDetails[1], userDetails[2], userDetails[4], userDetails[3]);
            Users.put(userDetails[1], user);
        }
    }

    // Read from "WMSlots.txt"
    public void FetchWMSlots(){
        //UserHandler uh = new UserHandler();
        String WMData = UserHandler.ReadFromDB("WMSlots.txt");
        String[] data = WMData.split("\n");
        for (int i = 0;i < data.length(); i++){
            String[] slotDetails = data[i].split(";");
            WMSlot slots = new WMSlot(slotDetails[0], slotDetails[1], slotDetails[2], slotDetails[3], slotDetails[4]);
            WMSlots.put(slotDetails[4], slots);
        }
    }

    // Read from "VPBookings.txt"
    public void FetchVPBookings(){
        //UserHandler uh = new UserHandler();
        String vpb = UserHandler.ReadFromDB("VPBookings.txt");
        String[] data = vpb.split("\n");
        for (int i = 0;i < data.length(); i++){
            String[] BookingDetails  = data[i].split(";");
            VPBooking vpbooking = new VPBooking(Users.get(BookingDetails[0]), BookingDetails[1], BookingDetails[2], BookingDetails[3], BookingDetails[5]);
            for (String i : BookingDetails[4].split("+")){
                vpbooking.addPartner(Users.get(i));
            }
            VPBookings.put(BookingDetails[5], vpbooking);
        }
    }

    // Read from "Requests.txt"
    public void FetchRequests(){
        //UserHandler uh = new UserHandler();
        String ReqData = UserHandler.ReadFromDB("Requests.txt");
        String[] data = ReqData.split("\n");
        for (int i = 0;i < data.length(); i++){
            String[] RequestData = data[i].split(";");
            Request new_request = new Request(RequestData[0], RequestData[1], RequestData[2], RequestData[3], RequestData[6], RequestData[5]);
            Requests.put(RequestData[6], new_request);
        }
    }

    // GETTERS AND SETTERS
    public LinkedHashMap<String, User> getUsers() {
        return Users;
    }

    public void setUsers(LinkedHashMap<String, User> users) {
        Users = users;
    }

    public LinkedHashMap<String, Requests> getRequests() {
        return RequestsList;
    }

    public void setRequests(LinkedHashMap<String, Requests> rl) {
        this.RequestsList = rl;
    }

    public LinkedHashMap<String, WMSlot> getWMSlots() {
        return WMSlots;
    }

    public void setWMSlots(LinkedHashMap<String, WMSlot> wMSlots) {
        WMSlots = wMSlots;
    }

    public LinkedHashMap<String, VPBooking> getVPBookings() {
        return VPBookings;
    }

    public void setVPBookings(LinkedHashMap<String, VPBooking> vPBookings) {
        VPBookings = vPBookings;
    }

    public String getWMStatus() {
        return WMStatus;
    }

    public void setWMStatus(String wMStatus) {
        WMStatus = wMStatus;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    // MAIN LOGIC
    public String encryptPassword(String password) {
        int x = 0;
        for (int i = 0; i < password.length(); i++) {
            x += Math.abs(password.charAt(i) - 'a') * Math.pow(10, i);
        }
        return Integer.toString(x);
    }

    public int generateOTP(int size) {
        int OTP = 0;
        for (int i = 0; i < size; i++) {
            OTP = OTP * 10;
            OTP = OTP + (int) Math.floor(Math.random() * 10);
        }
        return OTP;
    }

    public int Register(String name, String password, String MobileNumber, String emailID) {
        /*
         * Status Code
         * 1 --> User already exists
         * 0 --> Registered successfully
         */

        if (Users.get(emailID) != null) {
            return 1;
        }

        String Uid = "U" + Integer.toString(Users.size());
        User user = new User(name, emailID, MobileNumber, encryptPassword(password), Uid);
        Users.put(emailID, user);
        return 0;
    }

    public int Login(String email, String password) {
        /*
         * Status Codes
         * 0--> Succesfull Login
         * 1-->Incorrect password
         * 2-->User not found
         * 3 --> already logged in
         */

        User u = new User();
        if (u.equals(currentUser) == false) {
            return 3;
        }

        if (Users.get(email) == null) {
            return 2;
        }

        User u1 = Users.get(email);
        if (encryptPassword(password).equals(u1.getPassword())) {
            this.isLoggedIn = true;
            this.currentUser = u1;
            return 0;
        } else {
            return 1;
        }
    }

    public int Logout() {
        if (isLoggedIn() == false || currentUser == null) {
            return -1;
        }
        isLoggedIn = false;
        Users.get(currentUser.getEmail()).copy(currentUser);
        currentUser = null;
        return 0;
    }
     
    public native void WriteToDB(String userdata, String filename);

    public int Exit() {
        isLoggedIn = false;
        if (currentUser != null) {
            Users.get(currentUser.getEmail()).copy(currentUser);
        }
        currentUser = null;
        /* Write back to DB function */
        // writing userdata to "user.txt"
        String allUserData = "";
        for (String i : Users.keySlot()){
            allUserData = allUserData + Users.get(i).getName() + ";";
            allUserData = allUserData + Users.get(i).getEmail() + ";";
            allUserData = allUserData + Users.get(i).getMobileNumber() + ";";
            allUserData = allUserData + Users.get(i).getUserID() + ";";
            allUserData = allUserData + Users.get(i).getPassword() + ";";
            allUserData = allUserData + Users.get(i).getAdmin() + ";";
            allUserData = allUserData + Users.get(i).getCredits() + ";";
            allUserData = allUserData + Users.get(i).getPoints() + ";\n";
        }
        UserHandler uh = new UserHandler();
        uh.WriteToDB(allUserData, "user.txt");

        // writing WMSlot information to "WMSlots.txt"
        String WMData = "";
        for (String i : WMSlots.keySlot()){
            WMData = WMData + WMSlots.get(i).getUser().getEmail() + ";";
            WMData = WMData + WMSlots.get(i).getStartTime().toString() + ";";
            WMData = WMData + WMSlots.get(i).getEndTime().toString() + ";";
            WMData = WMData + WMSlots.get(i).getOTP() + ";";
            WMData = WMData + WMSlots.get(i).getSlotId() + ";";
            WMData = WMData + WMSlots.get(i).getWMran() + ";";
            WMData = WMData + WMSlots.get(i).getPointsDeducted() + ";\n";
        }
        uh.WriteToDB(WMData, "WMSlots.txt");

        // writing VPBookings details to "VPBookings.txt"
        String VPData = "";
        for (String i : VPBookings.keySlot()){
            VPData = VPData + VPBookings.get(i).getOwner().getEmail() + ";";
            VPData = VPData + VPBookings.get(i).getDeparture().toString() + ";";
            VPData = VPData + VPBookings.get(i).getFromLocation() + ";";
            VPData = VPData + VPBookings.get(i).getToLocation() + ";";
            int j = 0;
            for (j; j < VPBookings.get(i).getPartners().size() - 1; j++){
                VPData = VPData + VPBookings.get(i).getPartners().get(j).getEmail() + "+";
            }
            VPData = VPData + VPBookings.get(i).getPartners().get(j).getEmail() + ";";
            VPData = VPData + VPBookings.get(i).getVPID() + ";\n";
        }
        uh.WriteToDB(VPData, "VPBookings.txt");

        // writing Request information to "Requests.txt"
        String RequestData = "";
        for (String i : RequestsList.keySlot()){
            RequestData = RequestData + RequestsList.get(i).getFrom() + ";";
            RequestData = RequestData + RequestsList.get(i).getTo() + ";";
            RequestData = RequestData + RequestsList.get(i).getType() + ";";
            RequestData = RequestData + RequestsList.get(i).getTimeOfRequest() + "\";";
            RequestData = RequestData + RequestsList.get(i).getAccept() + ";";
            RequestData = RequestData + RequestsList.get(i).getSlotId() + ";";
            RequestData = RequestData + RequestsList.get(i).getRequestId() + ";\n";
        }
        uh.WriteToDB(RequestData, "Requests.txt");
        return 0;
    }

    public int createWMSlot(LocalDateTime start_Time) {
        /*
         * Status Codes
         * 0 --> Successfull Booking
         * 1 --> Insufficient Credits
         * 2 --> Timing not available
         * 3 --> Invalid/old timing
         * -1 --> user not logged in
         */
        LocalDateTime t = LocalDateTime.now();
        if (start_Time.isBefore(t)) {
            return 3;
        }

        if (isLoggedIn() == false || currentUser == null) {
            return -1;
        }

        if (currentUser.getCredits() > 0) {// checking for sufficient credit
            currentUser.setCredits(currentUser.getCredits() - 1);// decremnt credit
        } else {
            return 1;
        }
        LocalDateTime end_Time = start_Time.plusHours(1);

        for (String i : WMSlots.keySet()) { // checking for time clash b/w slots
            LocalDateTime st = WMSlots.get(i).getStartTime().minusMinutes(15);
            LocalDateTime et = WMSlots.get(i).getEndTime().plusMinutes(15);
            if (start_Time.isBefore(et) && st.isBefore(end_Time)) {
                return 2;
            }
        }

        String WMid = "WM" + Integer.toString(WMSlots.size());
        WMSlot wmslot = new WMSlot(currentUser, start_Time, end_Time, generateOTP(4), WMid);
        WMSlots.put(WMid, wmslot);
        return 0;
    }

    public int deleteWMSlot(String slotID, LocalDateTime start_Time) {
        /*
         * Status Codes
         * 0 --> Deleted successfully
         * 1 --> Slot not found
         * 2 --> Cannot delete slot for less than 2 hours prior to start time
         * -1 --> user not logged in
         */

        if (WMSlots.get(slotID) == null) {// slot does not exist
            return 1;
        }

        if (isLoggedIn() == false || currentUser == null) {
            return -1;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(start_Time.minusHours(2)) == true) {// check if delete time is more than 2 hours prior to slot
            currentUser.setPoints(currentUser.getPoints() - 10);
            currentUser.setCms(currentUser.getCms() - 10);
            currentUser.setCredits(currentUser.getCredits() + 1);
            WMSlots.remove(slotID);// removing slot // start time
            return 2;
        }

        WMSlots.remove(slotID);// removing slot
        currentUser.setCredits(currentUser.getCredits() + 1);// increasing credit back
        return 0;
    }

    public int askWMSlot(String slotID, String to_email, LocalDateTime start_Time) {
        /*
         * Status Codes
         * 0 --> Request sent successfully
         * 1 --> Slot not found
         * 2 --> User not found
         * 3 --> insufficient credits to ask for slot
         * -1 --> user not logged in
         */

        if (WMSlots.get(slotID) == null) {// slot does not exist
            return 1;
        }

        if (currentUser.getCredits() <= 0) {
            return 3;
        }

        if (Users.get(to_email) == null) {// user does not exist
            return 2;
        }

        if (isLoggedIn() == false || currentUser == null) {
            return -1;
        }

        LocalDateTime now = LocalDateTime.now();
        String Rid = "R" + Integer.toString(RequestsList.size());
        Requests request = new Requests(currentUser.getEmail(), to_email, "ask WMSlot", now, Rid,
                slotID);
        RequestsList.put(Rid, request);
        return 0;
    }

    public int receive_WMSlot(String requestID, boolean accept) {
        /*
         * Status Code
         * 1 --> request not found
         * 2 --> user does not exist
         * 3 --> ask request rejected
         * 4 --> receiver does not have enough credits to accept request
         * -1 --> user not logged in
         */

        if (RequestsList.get(requestID) == null) {// checking if request exists
            return 1;
        }

        if (Users.get(RequestsList.get(requestID).getFrom()) == null) {// user does not exist
            return 2;
        }

        if (isLoggedIn() == false || currentUser == null) {
            return -1;
        }

        boolean isWMR = RequestsList.get(requestID).getType() == "ask WMSlot" ? true : false;
        if (accept == true) {// request accepted
            if (isWMR == true) {
                WMSlots.get(RequestsList.get(requestID).getSlotId()).setUser(currentUser);// set to current user
                WMSlots.get(RequestsList.get(requestID).getSlotId()).setOTP(generateOTP(4));// generate new OTP
                Users.get(RequestsList.get(requestID).getFrom())
                        .setCredits(Users.get(RequestsList.get(requestID).getFrom()).getCredits() + 1);// increment
                                                                                                       // sender's
                                                                                                       // credit
                currentUser.setCredits(currentUser.getCredits() - 1);// decrement receiver's credits
                return 0;
            } else {
                RequestsList.get(requestID).setAccept(true);
                VPBookings.get(RequestsList.get(requestID).getSlotId())
                        .addPartner(Users.get(RequestsList.get(requestID).getFrom()));
                return 0;
            }
        }

        return 3;// request rejected
    }

    public int useWMSlot(String slotID, LocalDateTime start_Time, int OTP) {
        /*
         * Status Codes
         * 0 --> WM started running
         * 1 --> Slot not found
         * 2 --> Wrong OTP
         * 3 --> Too early to start slot
         * 4 --> Too late to start slot
         * -1 --> user not logged in
         */

        if (WMSlots.get(slotID) == null) {// slot does not exist
            return 1;
        }

        if (isLoggedIn() == false || currentUser == null) {
            return -1;
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(start_Time) && now.isBefore(start_Time.plusMinutes(15))) {
            if (WMSlots.get(slotID).getOTP() == OTP) {
                this.WMStatus = "In use";
                currentUser.setPoints(currentUser.getPoints() + 10);
                currentUser.setCms(currentUser.getCms() + 10);
                WMSlots.get(slotID).setWMran(true);
                return 0;
            } else {
                return 2;
            }
        } else if (now.isBefore(start_Time)) {
            return 3;
        } else if (now.isAfter(start_Time.plusMinutes(15))) {
            return 4;
        }

        return -1;
    }

    public int displayAllSlotsOnDay(LocalDateTime day, boolean yourslot) {
        /*
         * -1 --> user not logged in
         * 0 --> slots displayed
         */
        if (isLoggedIn() == false || currentUser == null) {
            return -1;
        }

        LinkedHashMap<String, WMSlot> allslots = new LinkedHashMap<String, WMSlot>();
        for (String i : WMSlots.keySet()) {
            LocalDateTime date = WMSlots.get(i).getStartTime();
            if (date.getDayOfMonth() == day.getDayOfMonth() && date.getMonth() == day.getMonth()
                    && date.getYear() == day.getYear()) {
                if (yourslot == true) {
                    if (WMSlots.get(i).getUser().getName() == currentUser.getName()) {
                        allslots.put(i, WMSlots.get(i));
                    }
                } else {
                    allslots.put(i, WMSlots.get(i));
                }
            }
        }

        // display slots jni code (Cpp)
        return 0;
    }

    public int viewVPSlotsOnDay(LocalDateTime day, String from, String to, boolean inSlot) {
        /*
         * Status Codes
         * 0 --> slots displayed
         * -1 --> user not logged in
         */
        if (isLoggedIn() == false || currentUser == null) {
            return -1;
        }

        LinkedHashMap<String, VPBooking> vpbookings = new LinkedHashMap<String, VPBooking>();

        for (String i : VPBookings.keySet()) {
            VPBooking vp = VPBookings.get(i);
            if (vp.getDeparture() == day && vp.getFromLocation() == from && vp.getToLocation() == to) {
                vpbookings.put(vp.getVPID(), vp);
            }
        }
        return 0;
        // display slots JNI Code
    }

    public int createSlot(LocalDateTime day, String from, String to) {
        /*
         * Status Codes
         * 0 --> slot created
         * 1 --> invalid/old date time
         * -1 --> user not logged in
         */
        LocalDateTime t = LocalDateTime.now();
        if (day.isBefore(t) == true) {
            return 1;
        }

        if (isLoggedIn() == false || currentUser == null) {
            return -1;
        }

        String VPid = "VP" + Integer.toString(VPBookings.size());
        VPBooking vp = new VPBooking(currentUser, day, from, to, VPid);
        VPBookings.put(VPid, vp);
        return 0;
    }

    public int sendJoinRequest(String id) {
        /*
         * Status Codes
         * 1 --> Invalid Slot ID
         * 0 --> Request sent
         * -1 --> user not logged in
         */
        if (VPBookings.get(id) == null) {
            return 1;
        }

        if (isLoggedIn() == false || currentUser == null) {
            return -1;
        }

        String rID = "R" + Integer.toString(RequestsList.size());
        Requests r = new Requests(currentUser.getName(), VPBookings.get(id).getOwner().getName(), "VP Join Request",
                LocalDateTime.now(), rID, id);
        RequestsList.put(rID, r);
        return 0;
    }

    public int removePartner(String partnerEmail, String slotID) {
        /*
         * Status Code
         * 1 --> invalid slot
         * 2 --> partner unavailable or does not exist
         * 3 --> not owner
         * -1 --> user not logged in
         */

        if (VPBookings.get(slotID) == null) {
            return 1;
        }

        if (VPBookings.get(slotID).getOwner().equals(currentUser) == false) {
            return 3;
        }

        if (isLoggedIn() == false || currentUser == null) {
            return -1;
        }

        int isPartner = -1;
        for (int i = 0; i < VPBookings.get(slotID).getPartners().size(); i++) {
            if (VPBookings.get(slotID).getPartners().get(i).getEmail().equals(partnerEmail)) {
                isPartner = i;
            }
        }

        if (isPartner == -1 || Users.get(partnerEmail) == null) {
            return 2;
        }

        VPBookings.get(slotID).getPartners().remove(isPartner);
        return 0;
    }

    public int leaveVPSlot(String slotID) {
        /*
         * Status Codes
         * 1 --> invalid slot id
         * 2 --> owner left slot. randomly assigned new owner
         * 3 --> not found in slot
         * 0 --> left slot
         * -1 --> user not logged in
         */

        if (VPBookings.get(slotID) == null) {// vp slot not found
            return 1;
        }

        if (isLoggedIn() == false || currentUser == null) {
            return -1;
        }

        if (VPBookings.get(slotID).getOwner() == currentUser) {// if owner change owner
            int Index = (int) Math.random() * VPBookings.get(slotID).getPartners().size();
            VPBookings.get(slotID).setOwner(VPBookings.get(slotID).getPartners().get(Index));
            VPBookings.get(slotID).getPartners().remove(Index);
            return 2;
        }

        int UserIndex = -1;
        for (int i = 0; i < VPBookings.get(slotID).getPartners().size(); i++) {
            if (VPBookings.get(slotID).getPartners().get(i) == currentUser) {
                UserIndex = i;
            }
        }

        if (UserIndex == -1) {// partner not found
            return 3;
        }

        VPBookings.get(slotID).getPartners().remove(UserIndex);
        return 0;
    }

    public int deleteVPSlot(String slotID) {
        /*
         * Status Code
         * 0 --> slot deleted successfully
         * 1 --> invalid slot id
         * 2 --> not owner;
         * -1 --> user not logged in
         */

        if (VPBookings.get(slotID) == null) {// slot not found
            return 1;
        }

        if (VPBookings.get(slotID).getOwner() != currentUser) {// not owner
            return 2;
        }

        if (isLoggedIn() == false || currentUser == null) {
            return -1;
        }

        VPBookings.remove(slotID);
        return 0;
    }

    public int availCredit() {
        /*
         * Status Codes
         * 1 --> not enough credits
         * -1 --> user not logged in
         * 0 --> credit availed
         */
        if (isLoggedIn() == false || currentUser == null) {
            return -1;
        }
        if (currentUser.getPoints() < 50) {
            return 1;
        }

        currentUser.setPoints(currentUser.getPoints() - 50);
        currentUser.setCredits(currentUser.getCredits() + 1);
        return 0;
    }

    public int viewCredit() {
        if (isLoggedIn() == false || currentUser == null) {
            return -1;
        }
        return currentUser.getCredits();
    }

    public int viewPoints() {
        if (isLoggedIn() == false || currentUser == null) {
            return -1;
        }
        return currentUser.getPoints();
    }

    public int viewCms() {
        if (isLoggedIn() == false || currentUser == null) {
            return -1;
        }
        return currentUser.getCms();
    }

    public String viewWMStatus() {
        return this.WMStatus;
    }

    public void displayLeaderBoard() {
        // JNI Cpp Call
    }

    public void BackgroundTaskRunner() {
        LocalDateTime now = LocalDateTime.now();
        boolean isRunning = false;
        for (String key : WMSlots.keySet()) {
            if (WMSlots.get(key).getWMran() == false && now.isAfter(WMSlots.get(key).getEndTime())
                    && WMSlots.get(key).getPointsDeducted() == false) {
                if (WMSlots.get(key).getUser().equals(currentUser)) {
                    currentUser.setPoints(currentUser.getPoints() - 10);
                } else {
                    Users.get(WMSlots.get(key).getUser().getEmailId())
                            .setPoints(Users.get(WMSlots.get(key).getUser().getEmailId()).getPoints() - 10);
                }
                WMSlots.get(key).setPointsDeducted(true);
            }

            if (now.isAfter(WMSlots.get(key).getStartTime()) && now.isBefore(WMSlots.get(key).getEndTime())
                    && WMSlots.get(key).getWMran() == true) {
                isRunning = true;
            }
        }

        if (isRunning == true) {
            this.WMStatus = "WM is in use";
        } else {
            this.WMStatus = "WM is free to use";
        }
    }
}