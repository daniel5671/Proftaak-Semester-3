package multiplayer;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import Units.Unit;
import fontyspublisher.IRemotePropertyListener;
import fontyspublisher.IRemotePublisherForDomain;
import fontyspublisher.IRemotePublisherForListener;

import java.beans.PropertyChangeEvent;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameManagerCommunicator
        extends UnicastRemoteObject
        implements IRemotePropertyListener {

    // Reference to whiteboard
    private final GameManagerClient gameManagerClient;

    // Remote publisher
    private IRemotePublisherForDomain publisherForDomain;
    private IRemotePublisherForListener publisherForListener;
    private static int portNumber = 1099;
    private static String bindingName = "publisher";
    private boolean connected = false;

    // Thread pool
    private final int nrThreads = 10;
    private transient ExecutorService threadPool = null;//SQ - Suggested serializable/transient, should work since this doesn't get passed/serialized

    public GameManagerCommunicator(GameManagerClient gameManagerClient) throws RemoteException {
        this.gameManagerClient = gameManagerClient;

        threadPool = Executors.newFixedThreadPool(nrThreads);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) throws RemoteException {

        String property = evt.getPropertyName();
        gameManagerClient.requestUnitAction(property, (Unit) evt.getOldValue() , (ObjectIdentifier) evt.getNewValue() );
    }

    public void connectToPublisher() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", portNumber);
            publisherForDomain = (IRemotePublisherForDomain) registry.lookup(bindingName);
            publisherForListener = (IRemotePublisherForListener) registry.lookup(bindingName);
            connected = true;
            System.out.println("Connection with remote publisher established");
        } catch (RemoteException | NotBoundException re) {
            connected = false;
            System.err.println("Cannot establish connection to remote publisher");
            System.err.println("Run GameServer to start remote publisher");
        }
    }

    public void register(String property) {
        if (connected) {
            try {
                // Nothing changes at remote publisher in case property was already registered
                publisherForDomain.registerProperty(property);
            } catch (RemoteException ex) {
                Logger.getLogger(GameManagerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void subscribe(final String property) {
        if (connected) {
            final IRemotePropertyListener listener = this;
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        publisherForListener.subscribeRemoteListener(listener, property);
                    } catch (RemoteException ex) {
                        Logger.getLogger(GameManagerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        }
    }

    public void unsubscribe(final String property) {
        if (connected) {
            final IRemotePropertyListener listener = this;
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        publisherForListener.unsubscribeRemoteListener(listener, property);
                    } catch (RemoteException ex) {
                        Logger.getLogger(GameManagerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        }
    }

    public void broadcast(final String property, final Object oldObject, final Object newObject) {
        if (connected) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        publisherForDomain.inform(property,oldObject,newObject);
                    } catch (RemoteException ex) {
                        Logger.getLogger(GameManagerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        }
    }

    public void stop() {
        if (connected) {
            try {
                publisherForListener.unsubscribeRemoteListener(this, null);
            } catch (RemoteException ex) {
                Logger.getLogger(GameManagerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            UnicastRemoteObject.unexportObject(this, true);
        } catch (NoSuchObjectException ex) {
            Logger.getLogger(GameManagerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
