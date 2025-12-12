



import 'package:flutter/material.dart';

class IdOcrResultPage extends StatelessWidget {
  final String recognizedText;

  const IdOcrResultPage({
    super.key,
    required this.recognizedText,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("신분증 확인")),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Text(
          recognizedText,
          style: const TextStyle(fontSize: 16),
        ),
      ),
    );
  }
}
