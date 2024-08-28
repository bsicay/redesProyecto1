import 'dart:async';
import 'dart:io';
import 'dart:convert';
import 'package:chat_xmpp/screens/user_detail_screen.dart';
import 'package:flutter/material.dart';
import 'package:chat_xmpp/models/user_model.dart';
import 'package:chat_xmpp/client/chatClient.client.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:file_picker/file_picker.dart';

class ChatScreen extends StatefulWidget {
  final User user;

  ChatScreen({required this.user});

  @override
  _ChatScreenState createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
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
        await _chatClient.getMessageHistory(username, widget.user.userJid);

    // Aplicar la eliminación de secuencias ANSI a cada mensaje
    List<String> cleanMessages = messageHistory.map((message) {
      return _removeAnsiEscapeCodes(message);
    }).toList();

    setState(() {
      // Almacenar los mensajes en orden inverso (para mostrar los más recientes primero)
      _messages = cleanMessages.reversed.toList();
    });
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
      await _chatClient.sendMessage(username, widget.user.userJid,
          message: message);
    }
  }

  void sendFile(String path) async {
    final prefs = await SharedPreferences.getInstance();
    String? username = prefs.getString('username');
    try {
      // Lee el archivo y conviértelo a base64
      final bytes = await File(path).readAsBytes();
      final base64File = base64Encode(bytes);
      final fileName = path.split('/').last;
      final fileExtension = fileName.split('.').last;

      // Crea el mensaje en el formato esperado
      final fileContent = 'file|$fileExtension|$base64File';

      await _chatClient.sendMessage(
        username ?? '',
        widget.user.userJid,
        message: fileContent,
      );
      print('File sent successfully');
    } catch (e) {
      print('Failed to send file: $e');
    }
  }

  Future<void> _pickFile() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles();

    if (result != null) {
      String? filePath = result.files.single.path;
      if (filePath != null) {
        sendFile(filePath);
      }
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
          IconButton(
            icon: Icon(Icons.attach_file),
            iconSize: 25.0,
            color: Theme.of(context).primaryColor,
            onPressed: _pickFile,
          ),
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
          widget.user.name.split('@')[0],
          style: TextStyle(
            fontSize: 28.0,
            fontWeight: FontWeight.bold,
          ),
        ),
        elevation: 0.0,
        actions: <Widget>[
          IconButton(
            icon: Icon(Icons.more_horiz),
            iconSize: 30.0,
            color: Colors.white,
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => UserDetailScreen(
                    userName: username,
                    userJid: widget.user.userJid,
                  ),
                ),
              );
            },
          ),
        ],
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
