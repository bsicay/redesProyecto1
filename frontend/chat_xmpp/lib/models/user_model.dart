class User {
  final String name;
  final String userJid;
  final String status;
  final String mode;
  final String imageUrl;

  final String statusMessage;

  User({
    required this.name,
    required this.userJid,
    required this.status,
    required this.mode,
    required this.imageUrl,
    required this.statusMessage,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      name: json['name'],
      userJid: json['userJid'],
      status: json['status'],
      mode: json['mode'],
      imageUrl: json['imageUrl'] ?? '',
      statusMessage: json['statusMessage'],
    );
  }

  @override
  String toString() {
    return '$name ($userJid)\n   * Status: $status\n   * Mode: $mode\n   * Status Message: $statusMessage';
  }
}
