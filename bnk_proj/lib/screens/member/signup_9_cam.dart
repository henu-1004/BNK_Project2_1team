import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';

class IdCameraPage extends StatefulWidget {
  const IdCameraPage({super.key});

  @override
  State<IdCameraPage> createState() => _IdCameraPageState();
}

class _IdCameraPageState extends State<IdCameraPage> {
  CameraController? _controller;
  late List<CameraDescription> _cameras;

  @override
  void initState() {
    super.initState();
    _initCamera();
  }

  Future<void> _initCamera() async {
    _cameras = await availableCameras();
    _controller = CameraController(
      _cameras.first,
      ResolutionPreset.high,
      enableAudio: false,
    );
    await _controller!.initialize();
    if (mounted) setState(() {});
  }

  @override
  void dispose() {
    _controller?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (_controller == null || !_controller!.value.isInitialized) {
      return const Scaffold(
        backgroundColor: Colors.black,
        body: Center(child: CircularProgressIndicator()),
      );
    }

    return Scaffold(
      backgroundColor: Colors.black,
      body: Stack(
        children: [
          CameraPreview(_controller!),

          // 🔲 사각형 가이드
          _IdGuideOverlay(),

          // 상단 안내 문구
          Positioned(
            top: 60,
            left: 0,
            right: 0,
            child: Center(
              child: Text(
                "사각형 안에 신분증을 맞춰주세요",
                style: TextStyle(
                  color: Colors.white.withOpacity(0.9),
                  fontSize: 16,
                ),
              ),
            ),
          ),

          // 하단 촬영 버튼
          Positioned(
            bottom: 40,
            left: 0,
            right: 0,
            child: Center(
              child: GestureDetector(
                onTap: _takePicture,
                child: Container(
                  width: 70,
                  height: 70,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(color: Colors.white, width: 4),
                  ),
                ),
              ),
            ),
          ),

          // 닫기 버튼
          Positioned(
            top: 50,
            right: 20,
            child: IconButton(
              icon: const Icon(Icons.close, color: Colors.white),
              onPressed: () => Navigator.pop(context),
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _takePicture() async {
    final image = await _controller!.takePicture();

    final inputImage = InputImage.fromFilePath(image.path);

    // 2️⃣ 한글 OCR 인식기 생성
    final textRecognizer = TextRecognizer(
      script: TextRecognitionScript.korean,
    );

    // 3️⃣ OCR 실행
    final recognizedText =
    await textRecognizer.processImage(inputImage);

    // 4️⃣ 결과 확인 (디버그)
    print(recognizedText.text);

    // 5️⃣ 사용 끝났으면 반드시 close
    await textRecognizer.close();

    // 👉 여기서 ML Kit OCR로 넘기면 됨
    // image.path
  }
}

class _IdGuideOverlay extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final width = MediaQuery.of(context).size.width * 0.85;
    final height = width * 0.63; // 신분증 비율

    return Center(
      child: Container(
        width: width,
        height: height,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: Colors.white.withOpacity(0.9),
            width: 3,
          ),
        ),
      ),
    );
  }
}
