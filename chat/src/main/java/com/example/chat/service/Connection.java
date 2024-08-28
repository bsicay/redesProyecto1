package com.example.chat.service;
import com.example.chat.api.model.User;
import jakarta.annotation.PostConstruct;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.filetransfer.*;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.form.FillableForm;
import org.jivesoftware.smackx.xdata.FormField;
import org.jivesoftware.smackx.xdata.packet.DataForm.Type;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 *  This class allows the application to connect with the XMPP server. It handles all the actions related to connection,
 *  requests and notifications with and to the server. It implements several methods that allow to log in, register,
 *  delete account, send and receive messages, etc.
 * @author Brandon Sicay 21757
 */

public class Connection {

    private AbstractXMPPConnection connection;
    private  Roster roster;
    private XMPPTCPConnectionConfiguration config;

    private static HashMap<String, ArrayList<String>> messages;
    private static HashMap<String, ArrayList<String>> groupMessages;
    private static HashMap<String, MultiUserChat> groupChatCredentials;
    private String screenCleaner;
    private static final String red = "\u001B[31m";
    private static final String green = "\u001B[32m";
    private static final String yellow = "\u001B[33m";
    private static final String blue = "\u001B[34m";
    private static final String reset = "\u001B[0m";
    private static Semaphore semaphore = new Semaphore(1);
    private ChatManager chatManager;
    private Scanner scanner;
    public static String currentChatUser;
    private ChatMessageListener chatListener;
    private MultiUserChatManager multiUserChatManager;
    ReconnectionManager reconnectionManager;
    private String currentUser = null;
    private boolean stanzaListenerAdded;
    private FileTransferManager fileManager;
    private String alias;
    private static ConcurrentLinkedQueue<String> notificationQueue = new ConcurrentLinkedQueue<>();


    /**
     * The constructor of this class initializes objects related to chat history and to receive some inputs from user.
     */
    public Connection() {
        messages = new HashMap<String, ArrayList<String>>();
        groupMessages = new HashMap<String, ArrayList<String>>();
        groupChatCredentials = new HashMap<String, MultiUserChat>();
        stanzaListenerAdded = false;
    }

    /**
     * This method creates the configuration and connects to server.
     * @param server the server to connect to.
     */
    @PostConstruct
    public void connect(String server) {
        if (connection == null) {
            try {
                // Create the configuration for the connection, disabling security mode and setting up the server domain.
                config = XMPPTCPConnectionConfiguration.builder()
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                        .setXmppDomain(server)
                        .setHost(server)
                        .setPort(5222)
                        .setResource("resource")
                        .build();
                connection = new XMPPTCPConnection(config);
                connection.connect();
                Thread.sleep(150);
                // It initializes managers for messages and presence.
                initializeManagers();
                createListeners();
            } catch (Exception e) {
                 e.printStackTrace();
            }
        }
    }

    /**
     * This method removes the incoming messages listener whenever the connection is disconnected.
     */
    private void removeChatListener() {
        if (chatListener != null)
            chatManager.removeIncomingListener(chatListener);
        chatListener = null;
    }

    /**
     * This method resets the chat manager for the connection, allowing to have the newest manager to make future listeners.
     */
    private void resetChatManager() {
        chatManager = ChatManager.getInstanceFor(connection);
        removeChatListener();
        chatListener = new ChatMessageListener();
        chatManager.addIncomingListener(chatListener);
    }

    /**
     * This method registers a user.
     * @param username the username to register
     * @param password the password of the new user
     */
    public int register(String username, String password) {
        try {
            if (!connection.isConnected()) {
                connection.connect();
            }
            AccountManager accountManager = AccountManager.getInstance(connection);
            // It is needed to allow register on an insecure connection.
            accountManager.sensitiveOperationOverInsecureConnection(true);
            Localpart localPartUsername = Localpart.from(username);
            accountManager.createAccount(localPartUsername, password);
            login(username, password);
            System.out.println("Registro exitoso e inicio de sesion exitosos.");
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Credenciales invalidas. No te pudimos registrar.");
            return -1;
        }

    }

    public List<String> getAllMessages() {
        List<String> allMessages = new ArrayList<>();

        try {
            semaphore.acquire();
            for (Map.Entry<String, ArrayList<String>> entry : messages.entrySet()) {
                System.out.println(entry);
                allMessages.addAll(entry.getValue());
            }
            semaphore.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return allMessages;
    }


    /**
     * This method handles the notifications of subscriptions received.
     */
    private void addStanzaListener() {
        if (!stanzaListenerAdded) {
            StanzaFilter presenceFilter = new StanzaFilter() {
                @Override
                public boolean accept(Stanza stanza) {
                    return stanza instanceof Presence && ((Presence) stanza).getType().equals(Presence.Type.subscribe);
                }
            };

            connection.addAsyncStanzaListener((stanza) -> {
                // Conversion from stanza to presence is needed to get the source user of subscription.
                Presence presence = (Presence) stanza;
                // The source user is gotten from the stanza.
                String from = presence.getFrom().toString();
                if (presence.getType().equals(Presence.Type.subscribe)) {
                    System.out.println(yellow + "Has recibido una solicitud de suscripción de: " + from + ". Aceptada automaticamente." + reset);
                    // We prepare a new stanza to subscribe back to the user.
                    Presence subscribedPresence = new Presence(Presence.Type.subscribe);
                    subscribedPresence.setTo(presence.getFrom());
                    try {
                        connection.sendStanza(subscribedPresence);
                        Thread.sleep(150);
                    } catch (SmackException.NotConnectedException | InterruptedException e) {
                        // e.printStackTrace();
                    }
                }
                System.out.print("\n> ");
            }, presenceFilter);
            stanzaListenerAdded = true;
        }
    }

    /**
     * This method creates a listener to identify when a user has changed their status.
     */
    private void createRosterListener() {
        roster.addRosterListener(new RosterListener() {
            @Override
            public void entriesAdded(Collection<Jid> addresses) {

            }

            @Override
            public void entriesUpdated(Collection<Jid> addresses) {

            }

            @Override
            public void entriesDeleted(Collection<Jid> addresses) {

            }

            public void presenceChanged(Presence presence) {
                // Notify the change.
                System.out.println(yellow + "Presence changed from user " + presence.getFrom().asBareJid().toString() + ". Type: " + presence.getType() + "; Mode: " + presence.getMode() + "; Status: " + presence.getStatus() + reset);
                System.out.print("\n> ");
            }
        });
    }

    /**
     * This method handles the notifications and acceptance of invitations to groupchats.
     */
    private void createInvitationListener() {
        multiUserChatManager.addInvitationListener(new InvitationListener() {
            @Override
            public void invitationReceived(XMPPConnection xmppConnection, MultiUserChat multiUserChat, EntityJid entityJid, String s, String s1, Message message, MUCUser.Invite invite) {
                // Aquí es donde aceptas la invitación automáticamente
                try {
                    // When an invitation is received, the alias of the user is their username by default. This is the variable alias.
                    multiUserChat.join(Resourcepart.from(alias));
                    // The new group chat is added to the dictionary containing its history.
                    addGroupChatToHistory(multiUserChat.getRoom().toString());
                    // We create a listener for incoming messages from the group.
                    addMUCListener(multiUserChat, multiUserChat.getRoom().toString(), alias);
                    // We store the group chat name and its corresponding multiuser chat object.
                    newCredentialGroupChat(multiUserChat.getRoom().toString(), multiUserChat);
                    System.out.println(yellow + "Te ha llegado una invitacion de " + invite.getFrom().toString() + " para unirte al chat grupal " + multiUserChat.getRoom().toString() + ". La hemos aceptado automaticamente." + reset);
                    System.out.print("\n> ");
                } catch (Exception e) {

                }
            }
        });
    }

    /**
     * This method handles events of errors in the connection
     */
    private void createConnectionErrorListener() {
        connection.addConnectionListener(new ConnectionListener() {
            @Override
            public void connected(XMPPConnection connection) {
                // Este método se llama cuando la conexión se establece con éxito.
            }

            @Override
            public void authenticated(XMPPConnection connection, boolean resumed) {
                // Este método se llama cuando la autenticación es exitosa.
            }

            @Override
            public void connectionClosed() {
                // Este método se llama cuando la conexión se cierra normalmente.
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                // Este método se llama cuando la conexión se cierra debido a un error.
                // Puedes obtener información sobre el error de la excepción 'e'.
            }
        });
    }

    /**
     * This method creates the listeners for roster changes, stanzas, connection errors and invitations to groupchats received.
     */
    private void createListeners() {
        createRosterListener();
        addStanzaListener();
        createConnectionErrorListener();
        createInvitationListener();
    }

    /**
     * This method initializes the managers needed for roster, files, groups and reconnection.
     */
    private void initializeManagers() {
        roster = Roster.getInstanceFor(connection);
        fileManager = FileTransferManager.getInstanceFor(connection);
        multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
        reconnectionManager = ReconnectionManager.getInstanceFor(connection);
        // If the connection fails, the app is enabled to reconnect automatically.
        reconnectionManager.enableAutomaticReconnection();
        System.out.printf("initilzie");
    }

    /**
     * This method sets the alias used by default in a group chat. This is useful for invitations.
     */
    private void setAlias(String username) {
        alias = username.replace("@alumchat.lol", "");
    }

    /**
     * This method allows a user to log in to the chat.
     * @param username
     * @param password
     * @return a number indicating if everything was correct when logging in.
     */
    public int login(String username, String password) {
        try {
            if (!connection.isConnected()) {
                connection = new XMPPTCPConnection(config);
                connection.connect();
                Thread.sleep(150);
                // If the connection was not enabled, it is needed to reinitialize managers and listeners
                initializeManagers();
                createListeners();
            } else {
                // If the connection was up, it is needed to reinitialize the managers only.
                initializeManagers();
            }
            connection.login(username, password);
            setAlias(username);
            // Whenever a user logs in, it is needed to tell the server that it is available again.
            sendAvailableStanza();

            // It is needed to get the latest roster
            if (!roster.isLoaded())
                roster.reloadAndWait();
            // The application accepts any subscription automatically.
            roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
            currentUser = username;
            System.out.println("Inicio de sesion exitoso.");
            resetChatManager();
            return 0;
        } catch (Exception e) {
             e.printStackTrace();
            System.out.println("Credenciales invalidas. No puedes iniciar sesion");
            return -1;
        }

    }

    /**
     * This method clears the history of a user when logging out.
     */
    private void clearHistory() {
        messages.clear();
        groupMessages.clear();
        groupChatCredentials.clear();
        currentUser = null;
    }

    /**
     * This method logs out a user.
     * @return a number indicating that everything was correct when logging out.
     */
    public int logout() {
        removeChatListener();
        clearHistory();
        connection.disconnect();
        try {
            Thread.sleep(300);
        } catch (Exception e) {

        }
        System.out.println("Se ha cerrado sesion exitosamente.\n");
        return 0;
    }

    /**
     * This method deletes an account from server.
     * @return a number indicating whether the deletion was successful.
     */
    public int deleteAccount() {
        try {
            messages.clear();
            AccountManager accountManager = AccountManager.getInstance(connection);
            accountManager.deleteAccount();
            System.out.println("Se elimino cuenta exitosamente.");
            return 0;
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println("No pudimos eliminar tu cuenta. Lo sentimos :(");
            return -1;
        }
    }

    /**
     * This method sends a subscription to a user.
     * @param userJid user to send subscription to
     */
    public void sendSubscription(String userJid) {
        try {
            Presence presence = new Presence(Presence.Type.subscribe);
            presence.setTo(JidCreate.bareFrom(userJid));
            connection.sendStanza(presence);
            System.out.println("Subscription request sent to: " + userJid);
        } catch (Exception e) {
            System.out.println("Failed to send subscription request: " + e.getMessage());
        }
    }

    /**
     * This method gets the details of a contact.
     * @param user user to get details from.
     */
    public User getUserDetail(String user) {
        User userDetails = null;

        try {
            Jid jid = JidCreate.from(user);
            RosterEntry entry = roster.getEntry(jid.asBareJid());

            if (entry != null) {
                String name = (entry.getName() != null) ? entry.getName() : entry.getUser();
                String userJid = entry.getUser();
                Presence presence = roster.getPresence(jid.asBareJid());

                String status = presence.isAvailable() ? "Online" : "Offline";
                String mode = (presence.getMode() != null) ? presence.getMode().name() : "none";
                String statusMessage = (presence.getStatus() != null) ? presence.getStatus() : "none";

                userDetails = new User(name, userJid, status, mode, statusMessage);
            } else {
                System.out.println("Usuario no presente en roster.");
            }
        } catch (Exception e) {
            System.out.println("No pudimos obtener el usuario :(");
            e.printStackTrace();
        }

        return userDetails;
    }

    /**
     * This method prints out the whole roster of a user.
     * @return string containing the result of the roster.
     */
    public List<User> getRoster() {
        List<User> userList = new ArrayList<>();

        try {
            roster = Roster.getInstanceFor(connection);
            roster.reloadAndWait();

            for (RosterEntry entry : roster.getEntries()) {
                String name = (entry.getName() != null) ? entry.getName() : entry.getUser();
                String userJid = entry.getUser();
                BareJid jid = JidCreate.bareFrom(userJid);
                Presence presence = roster.getPresence(jid);

                String status = presence.isAvailable() ? "Online" : "Offline";
                String mode = (presence.getMode() != null) ? presence.getMode().name() : "none";
                String statusMessage = (presence.getStatus() != null) ? presence.getStatus() : "none";

                User user = new User(name, userJid, status, mode, statusMessage);
                userList.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userList;
    }

    public List<User> searchUsers(String searchTerm) {
        List<User> foundUsers = new ArrayList<>();
        try {
            UserSearchManager searchManager = new UserSearchManager(connection);
            System.out.println(searchTerm);
            // Get the form and create an answer form
            DataForm searchForm = searchManager.getSearchForm(JidCreate.domainBareFrom("search.alumchat.lol"));
            DataForm.Builder answerFormBuilder = DataForm.builder();
            answerFormBuilder.setType(Type.submit);

            // Set search criteria
            // Correctly using FormField for boolean values and text
            answerFormBuilder.addField(FormField.hiddenBuilder("Username").setValue("true").build());
            answerFormBuilder.addField(FormField.hiddenBuilder("Email").setValue("true").build());
            answerFormBuilder.addField(FormField.textSingleBuilder("search").setValue(searchTerm).build());

            ReportedData data = searchManager.getSearchResults(answerFormBuilder.build(), JidCreate.domainBareFrom("search.alumchat.lol"));
            Roster roster = Roster.getInstanceFor(connection);


            for (ReportedData.Row row : data.getRows()) {
                System.out.println(row);
                BareJid userJid = (BareJid)row.getValues("jid").get(0);
                String userName = (String) row.getValues("Username").get(0);
                String userEmail = (String) row.getValues("Email").get(0);

//                BareJid jid = JidCreate.bareFrom(row.getValues("jid").get(0));
                Presence presence = Roster.getInstanceFor(connection).getPresence(userJid);

                String status = presence.isAvailable() ? "Online" : "Offline";
                String mode = (presence.getMode() != null) ? presence.getMode().name() : "none";
                String statusMessage = (presence.getStatus() != null) ? presence.getStatus() : "none";

                User newUser = new User(userName,userJid.toString(), status, mode, statusMessage);
                foundUsers.add(newUser);
                System.out.println("User found: JID=" + userJid + ", Username=" + userName + ", Email=" + userEmail + ", Status=" + status + ", Mode=" + mode + ", Status Message=" + statusMessage);
            }
        } catch (Exception e) {
            System.out.println("Error searching users: " + e.getMessage());
        }
        return foundUsers;
    }

    public List<String> discoverServices() {
        List<String> services = new ArrayList<>();
        try {
            ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(connection);
            DiscoverItems items = discoManager.discoverItems(connection.getXMPPServiceDomain());

            for (DiscoverItems.Item item : items.getItems()) {
                services.add(item.getEntityID().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return services;
    }
    /**
     * This method gets a presence mode depending on a number option.
     * @param option option of the mode selected by user.
     * @return the presence mode corresponding to the option
     */
    private Presence.Mode getMode(String option) {
        switch (option) {
            case "2":
                return Presence.Mode.away;
            case "3":
                return Presence.Mode.chat;
            case "4":
                return Presence.Mode.xa;
            case "5":
                return Presence.Mode.dnd;
            default:
                return Presence.Mode.available;
        }
    }

    /**
     * This gets a presence type depending on a number option.
     * @param option option of the type selected by user.
     * @return the presence type corresponding to the option.
     */
    private Presence.Type getType(String option) {
        switch (option) {
            case "2":
                return Presence.Type.unavailable;
            default:
                return Presence.Type.available;
        }
    }

    /**
     * This method sends an available presence stanza whenever a user logs in.
     */
    private void sendAvailableStanza() {
        Presence presence = new Presence(Presence.Type.available);
        try {
            connection.sendStanza(presence);
            Thread.sleep(150);
        } catch (Exception e) {
            System.out.println("No pudimos enviar la presencia de conexion.");
        }
    }

    /**
     * This sets the new user status, including the type of the status, the mode and the status message.
     * @param data an array containing the type, mode and message of the new status.
     */
    public void setStatusMessage(String[] data) {
        String message = data[0];
        Presence.Type type = getType(data[1]);
        Presence.Mode mode = getMode(data[2]);
        Presence presence = new Presence(type);
        presence.setStatus(message);
        presence.setMode(mode);
        try {
            connection.sendStanza(presence);
            Thread.sleep(300);
            System.out.println("Status modificado exitosamente.");
        } catch (Exception e) {
            System.out.println("No pudimos modificar tu estado :(");
            // e.printStackTrace();
        }
    }

    /**
     * This method resets the current user. It is useful when the client stops chating.
     */
    public void resetChatUser() {
        currentChatUser = null;
    }

    /**
     * This method allows a user to send a message.
     * @param user the user to send the message to.
     * @param message the message to be sent.
     */
    public void sendMessage(String user, String message) {
        try {
            ChatManager chatManager = ChatManager.getInstanceFor(connection);
            Chat chat = chatManager.chatWith(JidCreate.from(user).asEntityBareJidIfPossible());
            chat.send(message);
        } catch (Exception e) {
            System.out.println("Lo sentimos, no pudimos enviar tu mensaje :(.");
        }
    }

    /**
     * This method prints the chat history with a user.
     * @param userJID the user to chat with.
     * @param messages list of messages to show
     */
    private void showMessageHistory(String userJID, HashMap<String, ArrayList<String>> messages) {
        try {
            semaphore.acquire();
            if (messages.containsKey(userJID)) {
                ArrayList<String> chatMessages = messages.get(userJID);
                for (int i = 0; i < chatMessages.size(); i++) {
                    System.out.println(chatMessages.get(i));
                }
            }
            semaphore.release();
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    /**
     * This method adds a user to the chat history dictionary.
     * @param userJID the user to chat with.
     */
    public void addUserToChatHistory(String userJID) throws Exception {
        if (!messages.containsKey(userJID)) {
            try {
                semaphore.acquire();
                ArrayList<String> newMessages = new ArrayList<String>();
                messages.put(userJID, newMessages);
                semaphore.release();
            } catch (Exception e) {
                System.out.println("Hubo un error al agregar al usuario al historial.");
                throw new Exception();
            }
        }
    }

    /**
     * This method allows a user to chat with another.
     * @param user the user to chat with.
     */
    public void chatWithUser(String user) {
        String userJID = user;
        System.out.println(screenCleaner);
        System.out.println("Iniciando chat, escriba 'exit' para salir...");
        System.out.println(blue + "--------------- Chat with " + userJID + " ---------------" + reset);
        currentChatUser = user;
        boolean finishChat = false;
        showMessageHistory(userJID, messages);
        while (!finishChat) {
            try {
                addUserToChatHistory(userJID);
                System.out.print(green + "> " + reset);
                String message = scanner.nextLine();
                if (message.toLowerCase().equals("exit")) {
                    finishChat = true;
                } else {
                    sendMessage(user, message);
                    messages.get(userJID).add("You: " + message);
                }
            } catch (Exception e) {
                finishChat = true;
            }

        }
        System.out.println(screenCleaner);
    }

    public List<String> getMessageHistory(String userJID) {
        List<String> messageHistory = new ArrayList<>();
        try {
            semaphore.acquire();
            if (messages.containsKey(userJID)) {
                messageHistory = new ArrayList<>(messages.get(userJID));
            }
            semaphore.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageHistory;
    }

    public void addMessageToHistory(String userJID, String message) {
        try {
            semaphore.acquire();
            if (messages.containsKey(userJID)) {
                messages.get(userJID).add(message);
            } else {
                ArrayList<String> userMessages = new ArrayList<>();
                userMessages.add(message);
                messages.put(userJID, userMessages);
            }
            semaphore.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * This method converts files to base64 to send them through messages.
     * @param filePath
     */
    private String convertToBase64 (String filePath) {
        System.out.println(filePath);
        try {
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            return Base64.getEncoder().encodeToString(fileBytes);
        } catch (IOException e) {
             e.printStackTrace();
            System.out.println("Hubo un error al convertir el contenido del archivo a Base64.");
            return null;
        }
    }

    /**
     * This method converts a base64 string to a file.
     * @param filePath the path to save the file.
     * @param base64String the content of the file.
     */
    private static void convertBase64ToFile(String base64String, String filePath) {
        try {
            byte[] fileBytes = Base64.getDecoder().decode(base64String);
            Files.write(Paths.get(filePath), fileBytes);
            System.out.println(blue + "Hemos guardado el archivo con exito en " + filePath + reset);
        } catch (IOException e) {
            System.out.println("No pudimos guardar el archivo porque el formato estaba mal.");
        }
        System.out.print("\n> ");
    }

    /**
     * This method gets the file extension of a file
     * @param path the file path which the extension is needed.
     */
    private String getFileExtension(String path) {
        int lastIndex = path.lastIndexOf(".");
        if (lastIndex != -1 && lastIndex < path.length() - 1) {
            return path.substring(lastIndex + 1);
        }
        return "";
    }


    /**
     * This method allows a user to send a file as a message.
     * The file content is converted to Base64 and sent as a special formatted message.
     * The format is: file|extension|base64content
     * @param user the user to send the file to.
     * @param path the path to the file the user wants to send.
     */
    public void sendFile(String user, String path) {
        try {
            String fileExtension = getFileExtension(path);
            if (fileExtension.equals("")) {
                System.out.println("The file path does not have an appropriate extension. We couldn't send it.");
            } else {
                String fileContent = "file|" + fileExtension + "|" + convertToBase64(path);
                sendMessage(user, fileContent);
                System.out.println("File sent successfully");
            }
        } catch (Exception e) {
            System.out.println("Something went wrong, we couldn't send the file :(");
        }
    }




    /**
     * This method creates a new entry in the dictionary for group chat history.
     * @param groupName the name of the group to add.
     */
    private void addGroupChatToHistory(String groupName) {
        try {
            // We use a sempahore to prevent concurrency conflicts when accessing the dictionary.
            semaphore.acquire();
            if (! groupMessages.containsKey(groupName)) {
                groupMessages.put(groupName, new ArrayList<String>());
            }
            semaphore.release();
        } catch (Exception e) {
            System.out.println("No pudimos añadir el chat al historial :(");
            // e.printStackTrace();
        }
    }

    /**
     * This method adds a message to the group chat history, given the group chat name.
     * @param groupName the name of the group.
     * @param message the message to add to history.
     */
    private void addMessageToGroupHistory(String groupName, String message) {
        try {
            semaphore.acquire();
            groupMessages.get(groupName).add(message);
            semaphore.release();
        } catch (Exception e) {
            System.out.println("No pudimos añadir el chat al historial :(");
            // e.printStackTrace();
        }
    }

    /**
     * This method creates a listener for a groupchat.
     * @param muc the multiuser group chat object from smack to handle the group chat actions.
     * @param roomName the group chat name.
     * @param nickname the alias of the user in the groupchat.
     */
    private void addMUCListener(MultiUserChat muc, String roomName, String nickname) {
        muc.addMessageListener((from) -> {
            String body = from.getBody();
            Jid fromJid = from.getFrom();
            Resourcepart senderJid = fromJid.getResourceOrNull();
            if (senderJid != null) {
                String sender = senderJid.toString();
                String message = sender + ": " + body;

                // Here we chose a different color to save the message in history depending on if it was from the current user or other users in the chat
                if (sender.equals(nickname)) {
                    addMessageToGroupHistory(roomName, green + message + reset);
                } else {
                    addMessageToGroupHistory(roomName, blue + message + reset);
                }

                if (currentChatUser != null && currentChatUser.equals(roomName)) {
                    if (!sender.equals(roomName)) {
                        if (sender.equals(nickname)) {
                            System.out.println(green + message + reset);
                        } else {
                            System.out.println(blue + message + reset);
                        }
                        System.out.print("> ");
                    }
                } else {
                    System.out.println(yellow + "\nIncoming message from group " + roomName + ". User " + sender + reset + ".\n");
                    System.out.print("> ");
                }
            }

        });
    }

    /**
     * This method creates a new credential for a group chat. This means the multiuser chat object is associated with the name given to it.
     * @param groupName the name of group.
     * @param muc the multiuser chat object.
     */
    private void newCredentialGroupChat(String groupName, MultiUserChat muc) {
        groupChatCredentials.put(groupName, muc);
    }

    /**
     * This method allows to create a new group chat.
     * @param chatRoomName the name of the group chat to create.
     * @param nickname the alias of the current user in the group.
     */
    public void createGroupChat(String chatRoomName, String nickname) {
        try {
            multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
            // It is needed to specify to the server that we are creating a group chat.
            String roomName = chatRoomName + "@conference.alumchat.lol";
            System.out.printf(roomName);
            System.out.printf(nickname);
            EntityBareJid roomJid = JidCreate.entityBareFrom(roomName);
            MultiUserChat muc = multiUserChatManager.getMultiUserChat(roomJid);
            Resourcepart resource = Resourcepart.from(nickname);
            // We make the group chat available immediately and to everyone.
            muc.create(resource).makeInstant();
            // We add the group chat to the history and make a listener for it.
            addGroupChatToHistory(roomName);
            addMUCListener(muc, roomName, nickname);
            newCredentialGroupChat(roomName, muc);
            System.out.println("Hemos creado el grupo con éxito.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Algo salió mal. No pudimos crear el grupo :(");
        }
    }

    /**
     * This method invites a user to a new group.
     * @param muc the multiuser chat object corresponding to the group.
     * @param user the user to be invited.
     */
    private void invite(MultiUserChat muc, String user) {
        try {
            muc.invite(JidCreate.entityBareFrom(user), "Te invito a mi grupo.");
        } catch (Exception e) {
            System.out.println("No pudimos invitarlo al grupo.");
        }
    }

    /**
     * This method allows a user to invite another one to a group chat.
     * @param groupName the name of the group to make the invitation from.
     * @param user the user to chat with.
     */
    public void inviteToGroupChat(String groupName, String user) {
        groupName = groupName + "@conference.alumchat.lol";
        // We check whether the group exists or not. If it does not exist, we create the group.
        if (groupChatCredentials.containsKey(groupName)) {
            MultiUserChat muc = groupChatCredentials.get(groupName);
            invite(muc, user);
            System.out.println("Se ha invitado bien al usuario.");
            System.out.print("\n> ");
        } else {
            try {
                MultiUserChat muc = groupChatCredentials.get(groupName);
                String nickname = connection.getUser().toString();
                createGroupChat(groupName, nickname);
                invite(muc, user);
                System.out.print("\n> ");
            } catch (Exception e) {
                System.out.println("No pudimos hacer la invitación al grupo :(");
            }
        }
    }

    /**
     * This method allows a user to join a group chat by providing its name.
     * @param chatRoomName the name of the group to join.
     * @param nickname the alias of the user in the group.
     */
    public void joinGroupChat(String chatRoomName, String nickname) {
        try {
            multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
            String roomName = chatRoomName + "@conference.alumchat.lol";
            EntityBareJid roomJid = JidCreate.entityBareFrom(roomName);
            MultiUserChat muc = multiUserChatManager.getMultiUserChat(roomJid);
            Resourcepart resource = Resourcepart.from(nickname);
            muc.join(resource);
            addGroupChatToHistory(roomName);
            addMUCListener(muc, roomName, nickname);
            newCredentialGroupChat(roomName, muc);
            System.out.println("Te has unido a la sala con éxito.");
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println("Algo salió mal. No pudimos unirte el grupo :(");
        }
    }

    /**
     * This method sends a message to a group chat
     * @param messageText the message to be sent.
     * @param muc the multiuser chat object to send the message.
     */
    private void sendGroupMessage(String messageText, MultiUserChat muc) {
        try {
            Message message = new Message();
            message.setBody(messageText);
            message.setType(Message.Type.groupchat);

            muc.sendMessage(message);
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println(red + "\nNo pudimos enviar tu mensaje :(\n" + reset);
            System.out.print("> ");
        }

    }

    public ConcurrentLinkedQueue<String> getNotificationQueue() {
        return notificationQueue;
    }
    /**
     * This method shows a user a space to chat in a group chat.
     * @param chatRoomName the group chat name.
     */
    public void useGroupChat(String chatRoomName) {
        String roomName = chatRoomName + "@conference.alumchat.lol";
        if (groupChatCredentials.containsKey(roomName)) {
            System.out.println("Iniciando chat, escriba 'exit' para salir...");
            System.out.println(blue + "--------------- Groupchat " + chatRoomName + " ---------------" + reset);
            currentChatUser = roomName;
            boolean finishChat = false;
            showMessageHistory(roomName, groupMessages);
            MultiUserChat muc = groupChatCredentials.get(roomName);
            String message = null;
            while(!finishChat) {
                System.out.print(green + "> " + reset);
                message = scanner.nextLine();
                if (message.toLowerCase().equals("exit")) {
                    finishChat = true;
                } else {
                    sendGroupMessage(message, muc);
                }
            }
        }
    }

    /**
     * This method retrieves the message history for a group chat.
     * @param chatRoomName the group chat name.
     * @return the list of messages in the group chat.
     */
    public List<String> getGroupChatHistory(String chatRoomName) {
        String roomName = chatRoomName + "@conference.alumchat.lol";
        List<String> history = new ArrayList<>();
        if (groupChatCredentials.containsKey(roomName)) {
            // Retrieve message history from stored messages.
            if (groupMessages.containsKey(roomName)) {
                history = groupMessages.get(roomName);
            }
        }
        return history;
    }

    /**
     * This method sends a message to a group chat.
     * @param chatRoomName the group chat name.
     * @param message the message to be sent.
     */
    public void sendGroupMessage(String chatRoomName, String message) {
        String roomName = chatRoomName + "@conference.alumchat.lol";
        if (groupChatCredentials.containsKey(roomName)) {
            MultiUserChat muc = groupChatCredentials.get(roomName);
            sendGroupMessageToChat(message, muc);
            // Optionally store the message locally if you want to keep a history
            addMessageToGroupHistory(roomName, "You: " + message);
        }
    }

    /**
     * This method sends a message to the group chat object.
     * @param messageText the message to be sent.
     * @param muc the multiuser chat object to send the message.
     */
    private void sendGroupMessageToChat(String messageText, MultiUserChat muc) {
        try {
            Message message = new Message();
            message.setBody(messageText);
            message.setType(Message.Type.groupchat);
            muc.sendMessage(message);
        } catch (Exception e) {
            System.out.println(red + "\nNo pudimos enviar tu mensaje :(\n" + reset);
            System.out.print("> ");
        }
    }

    /**
     *  This class is a listener for incoming chat messages.
     * @author Brandon Sicay 21757
     */
    private static class ChatMessageListener implements IncomingChatMessageListener {
        /**
         * This method converts files to Base64 to send them through messages.
         * @param filePath the path to the file to be converted.
         * @return the Base64 encoded file content.
         */
        private String convertToBase64(String filePath) {
            try {
                byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
                return Base64.getEncoder().encodeToString(fileBytes);
            } catch (IOException e) {
                System.out.println("There was an error converting the file content to Base64.");
                return null;
            }
        }


        /**
         * This method gets the file extension of a file.
         * @param path the file path from which the extension is needed.
         * @return the file extension as a string.
         */
        private String getFileExtension(String path) {
            int lastIndex = path.lastIndexOf(".");
            if (lastIndex != -1 && lastIndex < path.length() - 1) {
                return path.substring(lastIndex + 1);
            }
            return "";
        }
        /**
         * This method creates the filename, by combining the username of the sender and a timestamp.
         * @param message the message sent to get the extension of the file.
         * @param user the user who sent the file.
         * @return a string containing the filename
         */
        private String formFileName(String message, String user) {
            String extension = getFileExtension(message);
            Instant instant = Instant.now();
            long timestamp = instant.toEpochMilli();
            String name = "./Files/" + user + "_" + timestamp + "." + extension;
            return  name;
        }

        /**
         * This method gets the content of the file on the message sent.
         * @param message the message sent.
         * @return a string containing only the content of the file.
         */
        private String getFileContent(String message) {
            String extension = message.split("\\|")[2];
            return extension;
        }

        /**
         * This method checks whether the format of the message sent is correct to create the file.
         * @param message the message sent.
         * @return true if format is correct, false otherwise.
         */
        private boolean isFileFormatCorrect(String message) {
            try {
                String[] extension = message.split("\\|");
                if (extension.length != 3) {
                    return false;
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        /**
         * This method listens for incoming messages. It handles files received or creates history of messages and shows them.
         * @param entityBareJid the sender of the message
         * @param message the message sent
         * @param chat the context of the chat
         */
        @Override
        public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
            if (message.getBody().length() > 4 && message.getBody().substring(0, 4).equals("file")) {
                if (isFileFormatCorrect(message.getBody())) {
                    System.out.println(yellow + "File received from: " + chat.getXmppAddressOfChatPartner().toString() + reset);
                    String fileName = formFileName(message.getBody(), chat.getXmppAddressOfChatPartner().toString());
                    String fileContent = getFileContent(message.getBody());
                    convertBase64ToFile(fileContent, fileName);
                }
            }
            else if (chat.getXmppAddressOfChatPartner().toString().equals(currentChatUser)) {
                System.out.println(blue + "User: " + message.getBody() + reset);
                messages.get(chat.getXmppAddressOfChatPartner().toString()).add(blue + "User: " + message.getBody() + reset);
                System.out.println("");
                System.out.print("> ");
            } else {
                System.out.println(yellow + "Incoming message from: " + chat.getXmppAddressOfChatPartner() + reset);
                notificationQueue.add("Incoming message from: " + chat.getXmppAddressOfChatPartner());
                System.out.print("> ");
                try {
                    semaphore.acquire();
                    if (messages.containsKey(chat.getXmppAddressOfChatPartner().toString())) {
                        messages.get(chat.getXmppAddressOfChatPartner().toString()).add(blue + "User: " + message.getBody() + reset);
                    } else {
                        ArrayList<String> userMessages = new ArrayList<String>();
                        userMessages.add( "User: " + message.getBody() + reset);
                        messages.put(chat.getXmppAddressOfChatPartner().toString(), userMessages);
                    }
                    System.out.println("Mensaje guardado de " +  message.getBody());

                    semaphore.release();
                } catch (Exception e) {
                     e.printStackTrace();
                }
            }
        }


    }
}