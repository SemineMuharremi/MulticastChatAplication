# Multicast Chat Application

A simple desktop chat application developed in Java using multicast networking. The application allows multiple users to communicate through a shared multicast group.

## Features

* Real-time group messaging using Java MulticastSocket
* Multiple clients can communicate through the same multicast group
* User name identification for messages
* Send and receive messages
* Leave chat functionality
* Chat history saved in JSON format
* Simple graphical user interface built with Java Swing
* Messages sent by the current user are displayed as `ME`
* Users do not receive their own messages twice

## Technologies

* Java
* Java Swing
* Java Networking
* UDP Multicast
* JSON
* File I/O

## How It Works

The application uses Java's `MulticastSocket` to allow multiple clients to communicate through the same multicast IP address and port.

Each client joins the multicast group and listens for incoming messages. When a user sends a message, it is sent to the multicast group and received by the other connected clients.

The application also stores sent messages in a `chat_history.json` file together with the username and timestamp.


## Running the Application

1. Make sure Java is installed on your computer.
2. Open the project in a Java-compatible IDE such as VS Code.
3. Compile the Java file:
   javac MulticastChatClient.java
4. Run the application:
   java MulticastChatClient
5. Enter your name when prompted.
6. Run multiple instances of the application to test communication between clients.

## Multicast Configuration

The application currently uses:
Multicast IP: 230.0.0.0
Port: 4446

## Purpose

This project was developed as a learning project to practice Java networking, multicast communication, threads, GUI development with Swing, and file handling.

This project was developed as a learning project to practice Java networking, multicast communication, threads, GUI development with Swing, and file handling.
