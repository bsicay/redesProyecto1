import 'dart:convert';
import 'package:http/http.dart' as http;

import '../models/user_model.dart';

class ChatClient {
  final String baseUrl;

  ChatClient({this.baseUrl = "http://192.168.0.3:8080/api"});

  Future<Map<String, dynamic>> login(String username, String password) async {
    final url =
        Uri.parse("$baseUrl/login?username=$username&password=$password");
    final response = await http.post(url);

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Failed to login');
    }
  }

  Future<List<User>> getRoster(String userName) async {
    final url = Uri.parse("$baseUrl/roster?username=$userName");
    final response = await http.get(url);

    if (response.statusCode == 200) {
      List<dynamic> rosterJson = jsonDecode(response.body);
      return rosterJson.map((json) => User.fromJson(json)).toList();
    } else {
      throw Exception('Failed to load roster');
    }
  }
}
