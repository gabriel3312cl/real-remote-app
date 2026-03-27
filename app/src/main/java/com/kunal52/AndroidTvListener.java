package com.kunal52;

public interface AndroidTvListener {
    void onSessionCreated();
    void onSecretRequested();
    void onPaired();
    void onConnectingToRemote();
    void onConnected();
    void onDisconnect();
    void onError(String error);
    /** Called for each granular step during the connection handshake. */
    void onConnectionStep(String step);
}