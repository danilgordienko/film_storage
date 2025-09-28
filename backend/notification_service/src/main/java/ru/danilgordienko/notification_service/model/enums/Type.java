package ru.danilgordienko.notification_service.model.enums;

public enum Type {
    FRIEND_REQUEST,
    ACCEPT_FRIEND_REQUEST,
    DECLINE_FRIEND_REQUEST,
    RECOMMENDATION_REQUEST;

    public String getValue(){
        return this.name().toLowerCase();
    }
}
