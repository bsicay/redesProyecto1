import 'package:chat_xmpp/screens/home_screen.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:chat_xmpp/screens/login_screen.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool _isCustomerSet = false;
  bool _isLoading = true; // Variable para manejar la carga inicial

  @override
  void initState() {
    super.initState();
    _checkCustomerSet();
  }

  Future<void> _checkCustomerSet() async {
    final prefs = await SharedPreferences.getInstance();
    bool isCustomerSet = prefs.getBool('isCustomerSet') ?? false;

    setState(() {
      _isCustomerSet = isCustomerSet;
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const MaterialApp(
        home: Scaffold(
          body: Center(
            child: CircularProgressIndicator(),
          ),
        ),
      );
    } else {
      return MaterialApp(
        title: 'XMPP Chat',
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          primaryColor: Colors.red,
          accentColor: const Color(0xFFFEF9EB),
        ),
        home: _isCustomerSet ? HomeScreen() : LoginScreen(),
      );
    }
  }
}
