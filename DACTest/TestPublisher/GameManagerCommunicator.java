/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import Game.GameManager;
import Units.Unit;
import fontyspublisher.IRemotePropertyListener;
import fontyspublisher.IRemotePublisherForDomain;
import fontyspublisher.IRemotePublisherForListener;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Array;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Communicator for white board. Establishes connection with Remote Publisher.
 *
 * @author Nico Kuijpers
 */
public class GameManagerCommunicator
        extends UnicastRemoteObject
        implements IRemotePropertyListener {

    // Reference to whiteboard
    private final GameManagerClient gameManager;

    // Remote publisher
    private IRemotePublisherForDomain publisherForDomain;
    private IRemotePublisherForListener publisherForListener;
    private static int portNumber = 1099;
    private static String bindingName = "publisher";
    private boolean connected = false;

    // Thread pool
    private final int nrThreads = 10;
    private ExecutorService threadPool = null;

    /**
     * Constructor.
     * @param gameManager  reference to white board
     * @throws java.rmi.RemoteException
     */
    public GameManagerCommunicator(GameManagerClient gameManager) throws RemoteException {
        this.gameManager = gameManager;
        threadPool = Executors.newFixedThreadPool(nrThreads);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) throws RemoteException {
        String property = evt.getPropertyName();
        DrawEvent drawEvent = (DrawEvent) evt.getNewValue();

        ArrayList<Unit> units = new ArrayList<Unit>();
        units.add(1, new Units.BuilderUnit());
        //TODO: methods die aangeroepen moeten worden in de client
        gameManager.requestSetUnits(units);
    }

    /**
     * Establish connection with remote publisher.
     */
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
            System.err.println("Run WhiteBoardServer to start remote publisher");
        }
    }

    /**
     * Register property at remote publisher.
     * @param property  property to be registered
     */
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

    /**
     * Subscribe to property.
     * @param property property to subscribe to
     */
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

    /**
     * Unsubscribe to property.
     * @param property property to unsubscribe to
     */
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

    /**
     * Broadcast draw event.
     * @param property the correct property type
     * @param object value of the property to be broadcasted
     * draw event
     */
    public void broadcast(final String property, final Object object) {
        if (connected) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        publisherForDomain.inform(property,null,object);
                    } catch (RemoteException ex) {
                        Logger.getLogger(GameManagerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        }
    }

    /**
     * Stop communicator.
     */
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
