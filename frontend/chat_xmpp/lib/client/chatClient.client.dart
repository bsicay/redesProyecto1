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

  Future<User> getUserDetails(String userName, String userJid) async {
    final url =
        Uri.parse("$baseUrl/getUserDetail?username=$userName&userJid=$userJid");
    final response = await http.get(url);

    if (response.statusCode == 200) {
      final jsonResponse = jsonDecode(response.body);
      return User.fromJson(jsonResponse);
    } else {
      throw Exception('Failed to load user details');
    }
  }

  Future<List<User>> searchContact(String userName, String searchTerm) async {
    final url = Uri.parse(
        "$baseUrl/searchAndAddContact?username=$userName&searchTerm=$searchTerm");
    final response = await http.get(url);

    if (response.statusCode == 200) {
      List<dynamic> contactJson = jsonDecode(response.body);
      return contactJson.map((json) => User.fromJson(json)).toList();
    } else {
      throw Exception('Failed to load searchContact');
    }
  }

  Future<void> addContact(String userName, String searchTerm) async {
    final url = Uri.parse(
        "$baseUrl/addContact?username=$userName&contactJid=$searchTerm");
    final response = await http.post(url);

    if (response.statusCode != 200) {
      throw Exception('Failed to load searchContact');
    }
  }

  Future<List<String>> getMessageHistory(
      String userName, String recipient) async {
    final url = Uri.parse(
        "$baseUrl/chatWithUser?username=$userName&recipient=$recipient");
    final response = await http.get(url);

    if (response.statusCode == 200) {
      List<dynamic> messageJson = jsonDecode(response.body);
      return List<String>.from(messageJson.map((msg) => msg.toString()));
    } else {
      throw Exception('Failed to load message history');
    }
  }

  Future<void> sendMessage(
      String userName, String recipent, String message) async {
    final url = Uri.parse(
        "$baseUrl/sendMessage?username=$userName&recipient=$recipent&message=$message");
    final response = await http.post(url);

    if (response.statusCode != 200) {
      throw Exception('Failed to load searchContact');
    }
  }

  Future<bool> createGroupChat(
      String username, String groupName, String nickname) async {
    final url = Uri.parse(
        '$baseUrl/createGroupChat?username=$username&chatRoomName=$groupName&nickname=$nickname');
    final response = await http.post(url);
    if (response.statusCode != 200) {
      throw Exception('Failed to load searchContact');
    }
    return response.statusCode == 200;
  }

  Future<bool> inviteToGroupChat(
      String username, String groupName, String userToInvite) async {
    final url = Uri.parse(
        '$baseUrl/inviteToGroupChat?username=$username&groupName=$groupName&user=$userToInvite');
    final response = await http.post(url);

    return response.statusCode == 200;
  }

  Future<bool> joinGroupChat(
      String username, String groupName, String nickname) async {
    final url = Uri.parse(
        '$baseUrl/joinGroupChat?username=$username&chatRoomName=$groupName&nickname=$nickname');
    final response = await http.post(url);

    return response.statusCode == 200;
  }

  Future<List<String>> useGroupChat(String username, String groupName) async {
    final url = Uri.parse(
        '$baseUrl/useGroupChat?username=$username&chatRoomName=$groupName');
    final response = await http.post(url);

    if (response.statusCode == 200) {
      List<dynamic> messageJson = jsonDecode(response.body);
      return List<String>.from(messageJson.map((msg) => msg.toString()));
    } else {
      throw Exception('Failed to load message history');
    }
  }

  Future<void> sendGroupMessage(
      String username, String groupName, String message) async {
    final url = Uri.parse(
        '$baseUrl/sendGroupMessage?username=$username&chatRoomName=$groupName&message=$message');
    final response = await http.post(url);

    if (response.statusCode != 200) {
      throw Exception('Failed to load message history');
    }
  }

  Future<void> logout(String userName) async {
    final url = Uri.parse("$baseUrl/logout?username=$userName");
    final response = await http.post(url);

    if (response.statusCode != 200) {
      throw Exception('Failed to load logout');
    }
  }
}
