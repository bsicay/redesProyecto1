import 'package:flutter/material.dart';
import '../client/chatClient.client.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'group_chat_screen.dart';

class GroupScreen extends StatefulWidget {
  @override
  _GroupScreenState createState() => _GroupScreenState();
}

class _GroupScreenState extends State<GroupScreen> {
  final ChatClient _chatClient = ChatClient();

  Future<void> _createGroupChat() async {
    final prefs = await SharedPreferences.getInstance();
    String? username = prefs.getString('username');

    String? groupName = await _showInputModal('Enter Group Name');
    String? nickname = await _showInputModal('Enter Your Nickname');

    if (username != null && groupName != null && nickname != null) {
      final result =
          await _chatClient.createGroupChat(username, groupName, nickname);
      _showResult(
          result ? 'Group created successfully' : 'Failed to create group');
    }
  }

  Future<void> _inviteToGroupChat() async {
    final prefs = await SharedPreferences.getInstance();
    String? username = prefs.getString('username');

    String? groupName = await _showInputModal('Enter Group Name');
    String? userToInvite = await _showInputModal('Enter User JID to Invite');

    if (username != null && groupName != null && userToInvite != null) {
      final result = await _chatClient.inviteToGroupChat(
          username, groupName, userToInvite);
      _showResult(
          result ? 'User invited successfully' : 'Failed to invite user');
    }
  }

  Future<void> _joinGroupChat() async {
    final prefs = await SharedPreferences.getInstance();
    String? username = prefs.getString('username');

    String? groupName = await _showInputModal('Enter Group Name');
    String? nickname = await _showInputModal('Enter Your Nickname');

    if (username != null && groupName != null && nickname != null) {
      final result =
          await _chatClient.joinGroupChat(username, groupName, nickname);
      _showResult(
          result ? 'Joined group successfully' : 'Failed to join group');
    }
  }

  Future<void> _useGroupChat() async {
    final prefs = await SharedPreferences.getInstance();
    String? username = prefs.getString('username');

    String? groupName = await _showInputModal('Enter Group Name');

    if (username != null && groupName != null) {
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => GroupChatScreen(groupName: groupName),
        ),
      );
    }
  }

  Future<String?> _showInputModal(String title) async {
    String? input;
    return showDialog<String?>(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text(title),
          content: TextField(
            onChanged: (value) {
              input = value;
            },
            decoration: InputDecoration(hintText: "Enter value"),
          ),
          actions: <Widget>[
            TextButton(
              child: Text('Cancel'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
            TextButton(
              child: Text('OK'),
              onPressed: () {
                Navigator.of(context).pop(input);
              },
            ),
          ],
        );
      },
    );
  }

  void _showResult(String message) {
    ScaffoldMessenger.of(context)
        .showSnackBar(SnackBar(content: Text(message)));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        body: Center(
      child: Padding(
        padding: EdgeInsets.all(16.0),
        child: Column(
          children: <Widget>[
            ElevatedButton(
              style: OutlinedButton.styleFrom(
                backgroundColor: Colors.red,
                primary: Colors.white,
              ),
              onPressed: _createGroupChat,
              child: Text('Create Group Chat'),
            ),
            ElevatedButton(
              style: OutlinedButton.styleFrom(
                backgroundColor: Colors.red,
                primary: Colors.white,
              ),
              onPressed: _inviteToGroupChat,
              child: Text('Invite to Group Chat'),
            ),
            ElevatedButton(
              style: OutlinedButton.styleFrom(
                backgroundColor: Colors.red,
                primary: Colors.white,
              ),
              onPressed: _joinGroupChat,
              child: Text('Join Group Chat'),
            ),
            ElevatedButton(
              style: OutlinedButton.styleFrom(
                backgroundColor: Colors.red,
                primary: Colors.white,
              ),
              onPressed: _useGroupChat,
              child: Text('Use Group Chat'),
            ),
          ],
        ),
      ),
    ));
  }
}
