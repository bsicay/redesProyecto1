import 'package:flutter/material.dart';
import 'package:chat_xmpp/models/user_model.dart';
import 'package:chat_xmpp/client/chatClient.client.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'chat_screen.dart';

class SearchScreen extends StatefulWidget {
  @override
  _SearchScreenState createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen> {
  final TextEditingController _searchController = TextEditingController();
  final ChatClient _chatClient = ChatClient();
  List<User> searchResults = [];

  Future<void> _searchUsers(String searchTerm) async {
    final prefs = await SharedPreferences.getInstance();
    String? username = prefs.getString('username');
    if (username != null && searchTerm.isNotEmpty) {
      final results = await _chatClient.searchContact(username, searchTerm);
      for (var user in results) {
        if (user.name != username) {
          searchResults.add(user);
        }
      }

      setState(() {});
    }
  }

  Future<void> _addContact(String contactJid) async {
    final prefs = await SharedPreferences.getInstance();
    String? username = prefs.getString('username');

    if (username != null) {
      await _chatClient.addContact(username, contactJid);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Sucription request sent successfully')),
      );
    }
  }

  void _showOptionsDialog(User user) {
    showModalBottomSheet(
      context: context,
      builder: (BuildContext context) {
        return Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            ListTile(
              leading: Icon(Icons.message),
              title: Text('Send Message'),
              onTap: () {
                Navigator.pop(context);
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => ChatScreen(user: user),
                  ),
                );
              },
            ),
            ListTile(
              leading: Icon(Icons.person_add),
              title: Text('Add Contact'),
              onTap: () async {
                Navigator.pop(context);
                await _addContact(user.userJid);
              },
            ),
          ],
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.red,
        title: const Text('Search Users'),
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: TextField(
              controller: _searchController,
              decoration: InputDecoration(
                hintText: 'Search by username...',
                suffixIcon: IconButton(
                  icon: const Icon(Icons.search),
                  onPressed: () {
                    _searchUsers(_searchController.text);
                  },
                ),
              ),
              onSubmitted: (value) {
                _searchUsers(value);
              },
            ),
          ),
          Expanded(
            child: ListView.builder(
              itemCount: searchResults.length,
              itemBuilder: (context, index) {
                final user = searchResults[index];
                return ListTile(
                  leading: const CircleAvatar(
                    radius: 35.0,
                    backgroundImage: AssetImage('assets/images/greg.png'),
                  ),
                  title: Text(user.name.split('@')[0]),
                  subtitle: Text(user.status),
                  onTap: () => _showOptionsDialog(user),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
