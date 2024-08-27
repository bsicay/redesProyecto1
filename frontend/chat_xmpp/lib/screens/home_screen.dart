import 'package:flutter/material.dart';
import 'package:chat_xmpp/widgets/category_selector.dart';
import 'package:chat_xmpp/widgets/favorite_contacts.dart';
import 'package:chat_xmpp/widgets/recent_chats.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../client/chatClient.client.dart';
import '../models/user_model.dart';

class HomeScreen extends StatefulWidget {
  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final ChatClient _chatClient = ChatClient();
  List<User> userData = [];
  // User userData = User(
  //     name: '',
  //     userJid: '',
  //     status: '',
  //     mode: '',
  //     imageUrl: '',
  //     statusMessage: '');

  @override
  void initState() {
    super.initState();

    Future.delayed(Duration.zero, () async {
      await fetchRoster();
    });
  }

  Future<void> fetchRoster() async {
    final prefs = await SharedPreferences.getInstance();

    String? username = prefs.getString('username');
    final response = await _chatClient.getRoster(username ?? 'sic21757-test10');
    if (response.isNotEmpty) {
      setState(() {
        userData = response;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.red,
      appBar: AppBar(
        backgroundColor: Colors.red,
        leading: IconButton(
          icon: const Icon(Icons.menu),
          iconSize: 27.0,
          color: Colors.white,
          onPressed: () {},
        ),
        centerTitle: true,
        title: const Text(
          'Chats',
          textAlign: TextAlign.left,
          style: TextStyle(
            fontSize: 22.0,
            fontWeight: FontWeight.bold,
          ),
        ),
        elevation: 0.0,
        actions: <Widget>[
          IconButton(
            icon: const Icon(Icons.search),
            iconSize: 27.0,
            color: Colors.white,
            onPressed: () {},
          ),
        ],
      ),
      body: Column(
        children: <Widget>[
          CategorySelector(),
          Expanded(
            child: Container(
              decoration: BoxDecoration(
                color: Theme.of(context).accentColor,
                borderRadius: const BorderRadius.only(
                  topLeft: Radius.circular(30.0),
                  topRight: Radius.circular(30.0),
                ),
              ),
              child: Column(
                children: <Widget>[
                  FavoriteContacts(userData: userData),
                  // RecentChats(),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
