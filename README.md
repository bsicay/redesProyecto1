# Project 1: XMPP Chat Application

Welcome to my first project for the **Computer Networks** course at Universidad del Valle de Guatemala.

## Project Overview

This project involves developing a client application using the XMPP chat protocol. The implementation includes various features, which are detailed in the following sections. The client operates via a command-line interface (CLI) and connects to a server with the domain **@alumchat.xyz**.

The following tools were utilized in building this project:

* Java (version 18.0.1 or higher)
* Smack (version 4.4.0 or higher)
* Maven
* Flutter

Java was chosen as the programming language for this project due to its ease of handling asynchronous operations and the availability of supportive libraries. Smack, in particular, stands out as a robust, flexible, and intuitive library for developing XMPP client applications. Maven was employed to manage the project, making the setup straightforward. However, the verbose commands required for compiling the project were a minor drawback, which is why IntelliJ IDEA was used for its seamless integration with Maven, enabling easy project execution with just a click.

You can view the project presentation here: [Presentation](./Presentacion_proyecto.pdf)

## Features

### Account Management
- [X] **Registration**: The client enables users to create a new account on the server. After creating an account, the client automatically logs the user into the chat.
- [X] **Login**: If a user already has an account, the client prompts for their credentials and then authenticates them with the server, granting access to chat features.
- [X] **Account Deletion**: Users who wish to remove their account from the server can do so through the client. While this option is not immediately visible in the CLI interface, it is related to account management. It is accessible only when the user is logged in.

### Chat Functionalities
- [X] **View Roster Status**: Logged-in users can view the status of their contacts, including their username, status message, mode, and availability.
- [X] **Add Contacts**: Users can add contacts to their roster by entering a username and domain, whether it’s alumchat or another domain.
- [X] **View Contact Details**: The client allows users to view detailed information about a contact by providing their username.
- [X] **Private Messaging**: Users can send direct messages to any other user by entering their username, even if they’re not on the user’s roster.
- [X] **Group Chats**: The client supports creating, joining, and participating in group chats.
- [X] **Update Status**: Users can update their status, choosing whether they are online or not, and setting a status mode and message.
- [X] **File Transfer**: The client allows users to send files to other users.
- [X] **Logout**: Users can log out of their session on the server.

For detailed code documentation, please refer to the [Javadoc](./Javadoc/index.html).

## Running the Project

To run the project, start by cloning the repository:

```bash
git clone https://github.com/bsicay/redesProyecto1


Once you have the project on your machine, you have two options. If you have IntelliJ IDEA installed, simply open the project in the IDE and run it. If you prefer using the command line, follow these steps to compile and run the project:


First, compile the project:

```bash
mvn compile


Then, execute the project:

```bash
mvn exec:java -Dexec.mainClass=com.example.App

### Flutter