package com.htm.twilio;

public class simpleSMS {
    private String message;
    private String number;
    private Double[] location;

    public simpleSMS(String message, String number) {
        this.message = message;
        this.number = number;

    }

    public boolean checkInput(String message) {
        if (message == null) {
            return false;
        }
        String[] splited = message.split(" to ");
        if (splited.length != 2) {
            return false;
        }
        if (splited[0].length() < 1 || splited[1].length() < 1) {
            return false;
        }
        return true;
    }

    public void setLocation(Double[] location) {
        this.location = location;
    }

    public String getLocText() {
        return (String) location[0].toString() + ":" + location[1].toString() + ":" + message;
    }

    public boolean checkLocMessage(String message) {
        String[] location = message.split(":");
        if (location.length != 3) {
            return false;
        }
        String route = location[2];
        if (route.isEmpty() || route == null) {
            return false;
        }
        return checkInput(route);
    }
}
