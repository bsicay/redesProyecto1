import 'dart:async';

import 'package:chat_xmpp/screens/groups_screen.dart';
import 'package:chat_xmpp/screens/search_screen.dart';
import 'package:flutter/material.dart';
import 'package:chat_xmpp/widgets/category_selector.dart';
import 'package:chat_xmpp/widgets/favorite_contacts.dart';
import 'package:chat_xmpp/widgets/recent_chats.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../client/chatClient.client.dart';
import '../models/user_model.dart';
import '../widgets/status_presence.dart';
import 'login_screen.dart';

class HomeScreen extends StatefulWidget {
  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final ChatClient _chatClient = ChatClient();
  List<User> userData = [];
  final List<String> categories = ['Messages', 'Groups', 'Requests'];
  int selectedIndex = 0;
  String username = '';

  @override
  void initState() {
    super.initState();

    Future.delayed(Duration.zero, () async {
      await fetchRoster();
    });

    Timer.periodic(Duration(seconds: 5), (Timer timer) {
      _fetchNotifications();
    });
  }

  Future<void> _fetchNotifications() async {
    final prefs = await SharedPreferences.getInstance();
    String username = prefs.getString('username') ?? '';

    if (username.isNotEmpty) {
      try {
        final notifications = await _chatClient.fetchNotifications(username);

        if (notifications.isNotEmpty) {
          _showNotificationDialog(notifications);
        }
      } catch (e) {
        print('Failed to fetch notifications: $e');
      }
    }
  }

  void _showNotificationDialog(List<String> notifications) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text('New Notifications'),
          content: Container(
            width: double.maxFinite,
            child: ListView.builder(
              shrinkWrap: true,
              itemCount: notifications.length,
              itemBuilder: (BuildContext context, int index) {
                return ListTile(
                  title: Text(notifications[index]),
                );
              },
            ),
          ),
          actions: <Widget>[
            TextButton(
              child: Text('Close'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  Future<void> fetchRoster() async {
    final prefs = await SharedPreferences.getInstance();

    username = prefs.getString('username') ?? '';
    final response = await _chatClient.getRoster(username ?? 'sic21757-test10');
    if (response.isNotEmpty) {
      setState(() {
        userData = response;
      });
    }
  }

  Future<void> _logout() async {
    final prefs = await SharedPreferences.getInstance();
    username = prefs.getString('username') ?? '';

    if (username != null) {
      await _chatClient.logout(username);
      await prefs.clear();
      Navigator.pushAndRemoveUntil(
        context,
        MaterialPageRoute(builder: (context) => LoginScreen()),
        (Route<dynamic> route) => false,
      );
    }
  }

  Future<void> _deleteAccount() async {
    final prefs = await SharedPreferences.getInstance();
    String? username = prefs.getString('username');

    if (username != null) {
      // Aquí deberías implementar el endpoint para eliminar la cuenta
      // await _chatClient.deleteAccount(username);
      await prefs.clear();
      Navigator.pushAndRemoveUntil(
        context,
        MaterialPageRoute(builder: (context) => LoginScreen()),
        (Route<dynamic> route) => false,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.red,
      appBar: AppBar(
        backgroundColor: Colors.red,
        leading: Builder(
          builder: (context) => IconButton(
            icon: const Icon(Icons.menu),
            iconSize: 27.0,
            color: Colors.white,
            onPressed: () {
              Scaffold.of(context).openDrawer();
            },
          ),
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
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => SearchScreen(),
                ),
              );
            },
          ),
        ],
      ),
      drawer: Drawer(
        child: ListView(
          padding: EdgeInsets.zero,
          children: <Widget>[
            DrawerHeader(
              decoration: BoxDecoration(
                color: Colors.red,
              ),
              child: Text(
                'Opciones',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 24,
                ),
              ),
            ),
            ListTile(
              leading: Icon(Icons.message),
              title: Text('Cambiar mensaje de presencia'),
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) =>
                        StatusMessageScreen(username: username),
                  ),
                );
              },
            ),
            ListTile(
              leading: Icon(Icons.exit_to_app),
              title: Text('Logout'),
              onTap: () async {
                await _logout();
              },
            ),
            ListTile(
              leading: Icon(Icons.delete),
              title: Text('Eliminar Cuenta'),
              onTap: () async {
                await _deleteAccount();
              },
            )
          ],
        ),
      ),
      body: Column(
        children: <Widget>[
          Container(
            height: 90.0,
            color: Theme.of(context).primaryColor,
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              itemCount: categories.length,
              itemBuilder: (BuildContext context, int index) {
                return GestureDetector(
                  onTap: () {
                    setState(() {
                      selectedIndex = index;
                    });
                  },
                  child: Padding(
                    padding: EdgeInsets.symmetric(
                      horizontal: 20.0,
                      vertical: 30.0,
                    ),
                    child: Text(
                      categories[index],
                      style: TextStyle(
                        color: index == selectedIndex
                            ? Colors.white
                            : Colors.white60,
                        fontSize: 20.0,
                        fontWeight: FontWeight.bold,
                        letterSpacing: 1.2,
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
          Expanded(
            child: Container(
              decoration: BoxDecoration(
                color: Theme.of(context).accentColor,
                borderRadius: const BorderRadius.only(
                  topLeft: Radius.circular(30.0),
                  topRight: Radius.circular(30.0),
                ),
              ),
              child: IndexedStack(
                index: selectedIndex,
                children: <Widget>[
                  FavoriteContacts(userData: userData),
                  GroupScreen(),
                  Center(child: Text('Requests')),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
