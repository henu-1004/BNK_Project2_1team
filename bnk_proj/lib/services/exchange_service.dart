import 'dart:convert';
import 'package:http/http.dart' as http;
import 'api_service.dart';

class ExchangeService {
  static String get baseUrl => "${ApiService.currentUrl}/exchange";

  static Future<Map<String, dynamic>> fetchMyExchangeAccounts({
    required String currency,
  }) async {
    final url = Uri.parse("$baseUrl/accounts?currency=$currency");
    final headers = await ApiService.getAuthHeaders();

    final response = await http.get(url, headers: headers);

    if (response.statusCode != 200) {
      throw Exception("계좌 조회 실패: ${response.statusCode} ${response.body}");
    }

    return jsonDecode(utf8.decode(response.bodyBytes));
  }

  static Future<void> buyForeignCurrency({
    required String toCurrency,
    required int krwAmount,
  }) async {
    final url = Uri.parse("$baseUrl/online");
    final headers = await ApiService.getAuthHeaders();

    print(">>> 전송하는 헤더: $headers");

    final response = await http.post(
      url,
      headers: headers,
      body: jsonEncode({
        "exchType": "B",
        "exchFromCurrency": "KRW",
        "exchToCurrency": toCurrency,
        "exchKrwAmount": krwAmount,
      }),
    );

    if (response.statusCode != 200) {
      throw Exception("환전 실패: ${response.statusCode} ${response.body}");
    }
  }

  // 외화 팔기 (Sell)
  static Future<void> sellForeignCurrency({
    required String fromCurrency, // 팔려고 하는 외화 (예: USD)
    required int frgnAmount,      // 팔려고 하는 외화 금액
  }) async {
    final url = Uri.parse("$baseUrl/online");
    final headers = await ApiService.getAuthHeaders();

    print(">>> [Sell] 전송하는 헤더: $headers");

    // 백엔드 OnlineExchangeService 로직에 맞춰 파라미터 전송
    final body = jsonEncode({
      "exchType": "S",                // 거래 유형: 팔기(Sell)
      "exchFromCurrency": fromCurrency, // 내가 가진 돈 (외화)
      "exchToCurrency": "KRW",        // 받을 돈 (원화)
      "exchFrgnAmount": frgnAmount,   // 외화 금액 기준
    });

    final response = await http.post(
      url,
      headers: headers,
      body: body,
    );

    if (response.statusCode != 200) {
      throw Exception("환전(팔기) 실패: ${response.statusCode} ${response.body}");
    }
  }
}

