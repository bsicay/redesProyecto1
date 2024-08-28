import 'package:flutter/material.dart';

import '../client/chatClient.client.dart';

class StatusMessageScreen extends StatefulWidget {
  final String username;

  StatusMessageScreen({required this.username});

  @override
  _StatusMessageScreenState createState() => _StatusMessageScreenState();
}

class _StatusMessageScreenState extends State<StatusMessageScreen> {
  final ChatClient _chatClient = ChatClient();
  String? _selectedType;
  String? _selectedMode;
  String _message = '';

  List<String> _types = [];
  List<String> _modes = [];

  @override
  void initState() {
    super.initState();
    _loadPresenceOptions();
  }

  Future<void> _loadPresenceOptions() async {
    try {
      final options = await _chatClient.getPresenceOptions();
      setState(() {
        _types = options['types']!;
        _modes = options['modes']!;
      });
    } catch (e) {
      // Manejar error
    }
  }

  Future<void> _submitStatusMessage() async {
    if (_selectedType != null && _selectedMode != null && _message.isNotEmpty) {
      try {
        await _chatClient.setStatusMessage(
            widget.username, _message, _selectedType!, _selectedMode!);
        ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Status modified successfully')));
      } catch (e) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text('Failed to modify status')));
      }
    } else {
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text('Please complete all fields')));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.red,
        title: Text('Set Status Message'),
      ),
      body: Padding(
        padding: EdgeInsets.all(16.0),
        child: Column(
          children: <Widget>[
            DropdownButton<String>(
              hint: Text('Select Type'),
              value: _selectedType,
              items: _types.map((String value) {
                return DropdownMenuItem<String>(
                  value: value,
                  child: Text(value),
                );
              }).toList(),
              onChanged: (newValue) {
                setState(() {
                  _selectedType = newValue;
                });
              },
            ),
            DropdownButton<String>(
              hint: Text('Select Mode'),
              value: _selectedMode,
              items: _modes.map((String value) {
                return DropdownMenuItem<String>(
                  value: value,
                  child: Text(value),
                );
              }).toList(),
              onChanged: (newValue) {
                setState(() {
                  _selectedMode = newValue;
                });
              },
            ),
            TextField(
              decoration: InputDecoration(labelText: 'Status Message'),
              onChanged: (text) {
                _message = text;
              },
            ),
            SizedBox(height: 20),
            ElevatedButton(
              style: OutlinedButton.styleFrom(
                backgroundColor: Colors.red,
                primary: Colors.white,
              ),
              onPressed: _submitStatusMessage,
              child: Text('Set Status Message'),
            ),
          ],
        ),
      ),
    );
  }
}
