import 'dart:async';

import 'package:chat_xmpp/screens/user_detail_screen.dart';
import 'package:flutter/material.dart';
import 'package:chat_xmpp/models/user_model.dart';
import 'package:chat_xmpp/client/chatClient.client.dart';
import 'package:shared_preferences/shared_preferences.dart';

class GroupChatScreen extends StatefulWidget {
  final String groupName;

  GroupChatScreen({required this.groupName});

  @override
  _GroupChatScreenState createState() => _GroupChatScreenState();
}

class _GroupChatScreenState extends State<GroupChatScreen> {
  final ChatClient _chatClient = ChatClient();
  List<String> _messages = [];
  Timer? _timer;
  String username = '';
  final TextEditingController _messageController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _startPolling();
    _loadMessageHistory();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  void _startPolling() {
    _timer = Timer.periodic(Duration(seconds: 1), (timer) {
      _loadMessageHistory();
    });
  }

  Future<void> _loadMessageHistory() async {
    final prefs = await SharedPreferences.getInstance();
    username = prefs.getString('username') ?? '';
    _messages.clear();
    final messageHistory =
        await _chatClient.useGroupChat(username, widget.groupName);

    // Aplicar la eliminación de secuencias ANSI a cada mensaje
    List<String> processedMessages = processMessages(messageHistory);

    setState(() {
      // Almacenar los mensajes en orden inverso (para mostrar los más recientes primero)
      _messages = processedMessages.reversed.toList();
    });
  }

  List<String> processMessages(List<String> messages) {
    Set<String> uniqueMessages = {}; // Para almacenar mensajes únicos
    List<String> filteredMessages = [];

    for (String message in messages) {
      // Eliminar el código de color ANSI antes de comprobar si el mensaje es único
      String cleanedMessage = _removeAnsiEscapeCodes(message);

      // Comprobar si el mensaje no contiene 'null' y no está duplicado
      if (!cleanedMessage.contains('null') &&
          uniqueMessages.add(cleanedMessage)) {
        filteredMessages.add(cleanedMessage);
      }
    }

    return filteredMessages;
  }

// Función para eliminar las secuencias de escape ANSI
  String _removeAnsiEscapeCodes(String input) {
    final ansiEscapePattern = RegExp(r'\x1B\[[0-?]*[ -/]*[@-~]');
    return input.replaceAll(ansiEscapePattern, '');
  }

  Future<void> sendMessage(String message) async {
    final prefs = await SharedPreferences.getInstance();
    String? username = prefs.getString('username');
    if (username != null) {
      await _chatClient.sendGroupMessage(username, widget.groupName, message);
    }
  }

  Widget _buildMessage(String message, bool isMe) {
    final Container msg = Container(
      margin: isMe
          ? EdgeInsets.only(
              top: 8.0,
              bottom: 8.0,
              left: 80.0,
            )
          : EdgeInsets.only(
              top: 8.0,
              bottom: 8.0,
              right: 80.0,
            ),
      padding: EdgeInsets.symmetric(horizontal: 25.0, vertical: 15.0),
      width: MediaQuery.of(context).size.width * 0.75,
      decoration: BoxDecoration(
        color: isMe ? Theme.of(context).accentColor : Color(0xFFFFEFEE),
        borderRadius: isMe
            ? BorderRadius.only(
                topLeft: Radius.circular(15.0),
                bottomLeft: Radius.circular(15.0),
              )
            : BorderRadius.only(
                topRight: Radius.circular(15.0),
                bottomRight: Radius.circular(15.0),
              ),
      ),
      child: Text(
        message.replaceFirst("You: ", ""),
        style: TextStyle(
          color: Colors.blueGrey,
          fontSize: 16.0,
          fontWeight: FontWeight.w600,
        ),
      ),
    );

    return msg;
  }

  Widget _buildMessageComposer() {
    return Container(
      padding: EdgeInsets.symmetric(horizontal: 8.0),
      height: 70.0,
      color: Colors.white,
      child: Row(
        children: <Widget>[
          Expanded(
            child: TextField(
              controller: _messageController,
              textCapitalization: TextCapitalization.sentences,
              decoration: InputDecoration.collapsed(
                hintText: 'Send a message...',
              ),
            ),
          ),
          IconButton(
            icon: Icon(Icons.send),
            iconSize: 25.0,
            color: Theme.of(context).primaryColor,
            onPressed: () async {
              String message = _messageController.text;
              if (message.isNotEmpty) {
                _messageController.clear();
                sendMessage(message);
                // setState(() {
                //   _messages.insert(0, "You: $message");
                // });
                // Optionally, you can send the message to the server here
              }
            },
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).primaryColor,
      appBar: AppBar(
        backgroundColor: Colors.red,
        title: Text(
          widget.groupName,
          style: TextStyle(
            fontSize: 28.0,
            fontWeight: FontWeight.bold,
          ),
        ),
        elevation: 0.0,
        actions: <Widget>[],
      ),
      body: GestureDetector(
        onTap: () => FocusScope.of(context).unfocus(),
        child: Column(
          children: <Widget>[
            Expanded(
              child: Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.only(
                    topLeft: Radius.circular(30.0),
                    topRight: Radius.circular(30.0),
                  ),
                ),
                child: ClipRRect(
                  borderRadius: BorderRadius.only(
                    topLeft: Radius.circular(30.0),
                    topRight: Radius.circular(30.0),
                  ),
                  child: ListView.builder(
                    reverse: true,
                    padding: EdgeInsets.only(top: 15.0),
                    itemCount: _messages.length,
                    itemBuilder: (BuildContext context, int index) {
                      final String message = _messages[index];
                      final bool isMe = message.startsWith("You: ");
                      return _buildMessage(message, isMe);
                    },
                  ),
                ),
              ),
            ),
            _buildMessageComposer(),
          ],
        ),
      ),
    );
  }
}
