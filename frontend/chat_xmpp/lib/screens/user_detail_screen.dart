import 'package:flutter/material.dart';
import '../client/chatClient.client.dart';
import '../models/user_model.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/painting.dart';

import 'chat_screen.dart';

class UserDetailScreen extends StatefulWidget {
  final String userName;
  final String userJid;

  UserDetailScreen({required this.userName, required this.userJid});

  @override
  _UserDetailScreenState createState() => _UserDetailScreenState();
}

class _UserDetailScreenState extends State<UserDetailScreen> {
  final ChatClient _chatClient = ChatClient();
  User? _userDetails;

  @override
  void initState() {
    super.initState();
    _fetchUserDetails();
  }

  Future<void> _fetchUserDetails() async {
    try {
      final userDetails =
          await _chatClient.getUserDetails(widget.userName, widget.userJid);
      setState(() {
        _userDetails = userDetails;
      });
    } catch (e) {
      print('Failed to load user details: $e');
    }
  }

  Widget profile() {
    var height = MediaQuery.of(context).size.height;
    var width = MediaQuery.of(context).size.width;
    return Container(
      child: Stack(
        alignment: Alignment.center,
        children: <Widget>[
          Container(
            color: Colors.red,

            // decoration: BoxDecoration(
            //     image: DecorationImage(
            //   image: NetworkImage(
            //       "https://image.shutterstock.com/image-photo/red-sunset-mountains-landscape-sunny-260nw-234300205.jpg"),
            //   fit: BoxFit.cover,
            // )),
            // color: Colors.deepOrange,
          ),
          Align(
            alignment: Alignment.bottomCenter,
            child: Container(
              height: height / 1.3,
              decoration: const BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.only(
                  topLeft: Radius.circular(40.0),
                  topRight: Radius.circular(40.0),
                ),
              ),
            ),
          ),
          Positioned(
            top: height / 14,
            child: Column(
              children: <Widget>[
                Container(
                  height: height / 5,
                  width: height / 5,
                  decoration: const BoxDecoration(
                      image: DecorationImage(
                          image: NetworkImage(
                              "https://cdn-icons-png.flaticon.com/512/147/147144.png"),
                          fit: BoxFit.cover),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black54,
                          blurRadius: 5.0,
                          spreadRadius: 2.0,
                          offset: Offset(0, 1),
                        )
                      ],
                      color: Colors.white,
                      shape: BoxShape.circle),
                ),
                const SizedBox(
                  height: 10,
                ),
                Container(
                  child: Text(
                    _userDetails!.name.split('@')[0],
                    style: const TextStyle(
                        fontSize: 25,
                        fontFamily: 'OpenSans',
                        fontWeight: FontWeight.bold),
                  ),
                ),
                const SizedBox(
                  height: 10,
                ),
                Container(
                  child: Text(
                    _userDetails!.userJid,
                    style: const TextStyle(fontSize: 20, color: Colors.black45),
                  ),
                ),
                const SizedBox(
                  height: 10,
                ),
                Container(
                    child: Text(
                  "Status: ${_userDetails!.statusMessage}",
                  style: const TextStyle(
                    fontSize: 17,
                  ),
                )),
                const SizedBox(
                  height: 10,
                ),
                Row(
                  children: <Widget>[
                    SizedBox(
                      width: width / 4,
                      height: height / 16.5,
                      child: ElevatedButton(
                        style: OutlinedButton.styleFrom(
                          backgroundColor: Colors.red,
                          primary: Colors.white,
                        ),
                        // borderSide:
                        //     BorderSide(color: Colors.deepPurple, width: 2.0),
                        // shape: RoundedRectangleBorder(
                        //     borderRadius: BorderRadius.circular(50.0)),
                        onPressed: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (_) => ChatScreen(
                                user: _userDetails!,
                              ),
                            ),
                          );
                        },
                        child: const Text('Message'),
                      ),
                    ),
                  ],
                ),
                const SizedBox(
                  height: 20,
                ),
                Row(
                  children: <Widget>[
                    Container(
                      child: Column(
                        children: <Widget>[
                          const Text(
                            'Status',
                            style: TextStyle(
                              fontWeight: FontWeight.w500,
                              fontSize: 20,
                            ),
                          ),
                          Text(
                            _userDetails!.status,
                            style: const TextStyle(
                              fontWeight: FontWeight.normal,
                              fontSize: 16,
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(
                      width: 50,
                    ),
                    Container(
                      child: Column(
                        children: <Widget>[
                          const Text(
                            'Mode',
                            style: TextStyle(
                              fontWeight: FontWeight.w500,
                              fontSize: 18,
                            ),
                          ),
                          Text(
                            _userDetails!.mode,
                            style: const TextStyle(
                              fontWeight: FontWeight.normal,
                              fontSize: 16,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(
                  height: 20,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text('User Details'),
          backgroundColor: Colors.red,
        ),
        body: _userDetails == null
            ? const Center(child: CircularProgressIndicator())
            : profile());
  }
}
