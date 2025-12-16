import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class ApiService {
  // 에뮬레이터에서 로컬호스트 접속 시 10.0.2.2 사용
  // 실기기 연결 시에는 내 PC의 IP 주소(예: 192.168.0.x)를 써야 함
  // 1. 실제 배포 주소 (앱 출시용 - 나중에 서버 올리면 그때 적으세요)
  static const String _prodUrl = "http://34.64.124.33:8080/backend/api/mobile";
  static const String baseUrl = "http://10.0.2.2:8080/backend/api/mobile";
  static const String base2Url = "http://192.168.0.209:8080/backend/api/mobile";
  static const _storage = FlutterSecureStorage();

  /// 로그인 요청
  static Future<bool> login(String userid, String password, String deviceId) async {
    final url = Uri.parse('$baseUrl/member/login');

    try {
      final response = await http.post(
        url,
        headers: {"Content-Type": "application/json"},
        body: jsonEncode({
          "userid": userid,
          "password": password,
          "deviceId": deviceId, // Controller의 LoginRequest 필드명과 일치해야 함
        }),
      );

      if (response.statusCode == 200) {
        // 성공 시 JSON 파싱
        final data = jsonDecode(utf8.decode(response.bodyBytes)); // 한글 깨짐 방지
        String token = data['token'];
        String custName = data['custName'];

        print("로그인 성공! 이름: $custName, 토큰: $token");

        // 토큰을 안전한 저장소에 보관 (나중에 API 호출 때 사용)
        await _storage.write(key: 'auth_token', value: token);

        return true;
      } else {
        print("로그인 실패: ${response.body}");
        return false;
      }
    } catch (e) {
      print("서버 통신 오류: $e");
      return false;
    }
  }
}