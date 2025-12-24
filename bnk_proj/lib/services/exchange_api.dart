import 'dart:convert';
import 'package:http/http.dart' as http;
import '../screens/exchange/forex_insight.dart';
import '../services/api_service.dart';

class ExchangeApi {
  // 1. 기본 URL 설정
  // 백엔드: @RequestMapping("/api/mobile/exchange")
  // 플러터: ApiService.currentUrl이 ".../api/mobile" 이므로 "/exchange"만 붙입니다.
  static String get baseUrl => '${ApiService.currentUrl}/exchange';

  /// [공개 API] 환율 목록 조회
  /// SecurityConfig에서 permitAll() 설정된 경로이므로 토큰 없이 요청합니다.
  static Future<List<CurrencyRate>> fetchRates() async {
    // ★ 인증 헤더 제외 (로그인 안 해도 조회 가능하게)
    final headers = {
      "Content-Type": "application/json",
    };

    final url = Uri.parse('$baseUrl/rates');
    print("📌 [ExchangeApi] fetchRates URL: $url");

    final response = await http.get(url, headers: headers);

    if (response.statusCode == 200) {
      final List data = jsonDecode(utf8.decode(response.bodyBytes));
      return data.map((e) => CurrencyRate.fromJson(e)).toList();
    } else {
      print("❌ 환율 조회 실패: ${response.statusCode} ${response.body}");
      throw Exception('환율 정보를 불러오는데 실패했습니다.');
    }
  }

  /// [공개 API] 특정 통화 히스토리 조회
  static Future<List<ExchangeHistory>> fetchHistory(String currency) async {
    final headers = {
      "Content-Type": "application/json",
    };

    final url = Uri.parse('$baseUrl/rates/$currency');

    final response = await http.get(url, headers: headers);

    if (response.statusCode == 200) {
      final List data = jsonDecode(utf8.decode(response.bodyBytes));
      return data.map<ExchangeHistory>((e) {
        return ExchangeHistory(
          date: DateTime.parse(e['rhistRegDt']),
          rate: (e['rhistBaseRate'] as num).toDouble(),
        );
      }).toList();
    } else {
      throw Exception('히스토리 조회 실패');
    }
  }

  /// [보안 API] 내 외화 계좌 조회
  /// 백엔드에서 Authentication 객체를 확인하므로, 토큰(Header)이 반드시 필요합니다.
  static Future<Map<String, dynamic>> fetchAccounts(String currency) async {
    // ★ 로그인 토큰 포함 헤더 가져오기
    final headers = await ApiService.getAuthHeaders();

    final url = Uri.parse('$baseUrl/accounts?currency=$currency');
    print("📌 [ExchangeApi] fetchAccounts URL: $url");

    final response = await http.get(url, headers: headers);

    if (response.statusCode == 200) {
      return jsonDecode(utf8.decode(response.bodyBytes));
    } else {
      print("❌ 계좌 조회 실패: ${response.statusCode} ${response.body}");
      throw Exception("계좌 조회 실패: ${response.statusCode}");
    }
  }
}