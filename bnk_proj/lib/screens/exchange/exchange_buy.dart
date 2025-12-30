import 'package:flutter/material.dart';
import 'forex_insight.dart';
import '../../services/exchange_service.dart';
import 'exchange_complete_page.dart';
import 'package:flutter/services.dart';
import 'package:local_auth/local_auth.dart';
import 'package:test_main/screens/auth/pin_login_screen.dart';
import 'package:test_main/screens/auth/pin_setup_screen.dart';
import '../../services/api_service.dart';


class ExchangeBuyPage extends StatefulWidget {
  final CurrencyRate rate;

  const ExchangeBuyPage({
    super.key,
    required this.rate,
  });

  @override
  State<ExchangeBuyPage> createState() => _ExchangeBuyPageState();
}


class _ExchangeBuyPageState extends State<ExchangeBuyPage> {
  String foreignAmount = "1";

  final LocalAuthentication auth = LocalAuthentication();

  int krwBalance = 0;
  bool isLoading = true;
  int foreignBalance = 0;

  @override
  void initState() {
    super.initState();
    _loadMyAccounts();
  }

  // [추가] 통합 인증 및 환전 실행 로직 (사기)
  Future<void> _handleAuthAndBuy() async {
    // 0. 금액 검증
    if (foreignAmount.isEmpty || double.parse(foreignAmount) <= 0) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("구매할 금액을 입력해주세요.")),
      );
      return;
    }

    // [1] 현재 로그인한 사용자 아이디 가져오기
    String? currentUserId = await ApiService.getSavedUserId();

    if (currentUserId == null) {
      // 아이디가 없으면(로그인 풀림 등) 에러 처리 후 종료
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("로그인 정보가 없습니다. 다시 로그인해주세요.")),
      );
      return;
    }

    // 1. PIN 등록 여부 확인
    bool hasPin = await ApiService.checkHasPin();

    if (!hasPin) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("보안을 위해 간편비밀번호 설정이 필요합니다.")),
      );
      await Navigator.push(
        context,
        MaterialPageRoute(builder: (context) => PinSetupScreen(userId: currentUserId)),
      );
      return;
    }

    // 2. 생체 인증 시도
    bool authenticated = false;
    try {
      bool canCheckBiometrics = await auth.canCheckBiometrics;
      if (canCheckBiometrics) {
        authenticated = await auth.authenticate(
          localizedReason: '환전을 진행하려면 인증해주세요.',
          options: const AuthenticationOptions(
            biometricOnly: true,
            stickyAuth: true,
          ),
        );
      }
    } catch (e) {
      print("생체 인증 실패 또는 미지원: $e");
    }

    // 3. 생체 인증 실패 시 -> PIN 인증 화면으로 이동
    if (!authenticated) {
      if (!mounted) return;
      final bool? pinResult = await Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => PinLoginScreen(
            userId: currentUserId,
            isAuthMode: true,
          ),
        ),
      );

      if (pinResult != true) return; // 취소/실패 시 중단
    }

    // 4. 인증 성공 -> 환전 실행
    _executeBuy();
  }

  // [추가] 실제 환전 API 호출 함수
  Future<void> _executeBuy() async {
    try {
      final double foreign = double.tryParse(foreignAmount) ?? 0;
      final int krwAmount = (foreign * widget.rate.rate).round();

      // 1️⃣ 서버 환전 요청
      await ExchangeService.buyForeignCurrency(
        toCurrency: widget.rate.code,
        krwAmount: krwAmount,
      );

      if (!mounted) return;

      // 2️⃣ 환전 완료 화면으로 이동
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (_) => ExchangeCompletePage(
            currency: widget.rate.code,
            foreignAmount: foreign,
            krwAmount: krwAmount,
            appliedRate: widget.rate.rate,
          ),
        ),
      );

    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("환전 실패: $e")),
      );
    }
  }

  Future<void> _loadMyAccounts() async {
    try {
      final data = await ExchangeService.fetchMyExchangeAccounts(
        currency: widget.rate.code,
      );

      setState(() {
        krwBalance = (data['krwBalance'] as num?)?.toInt() ?? 0;
        foreignBalance = (data['frgnBalance'] as num?)?.toInt() ?? 0; // ✅ 추가
        isLoading = false;
      });
    } catch (e) {
      setState(() => isLoading = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("계좌 조회 실패: $e")),
      );
    }
  }

  void _onKeyTap(String value) {
    setState(() {
      if (value == "back") {
        if (foreignAmount.isNotEmpty) {
          foreignAmount =
              foreignAmount.substring(0, foreignAmount.length - 1);
          if (foreignAmount.isEmpty) foreignAmount = "0";
        }
        return;
      }

      // 🔹 소수점 처리
      if (value == ".") {
        // 이미 소수점이 있으면 무시
        if (foreignAmount.contains(".")) return;

        // "0" 또는 빈 값이면 "0."
        if (foreignAmount.isEmpty || foreignAmount == "0") {
          foreignAmount = "0.";
        } else {
          foreignAmount += ".";
        }
        return;
      }

      // 🔹 숫자 처리
      if (foreignAmount == "0") {
        foreignAmount = value;
      } else {
        foreignAmount += value;
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final double foreign = double.tryParse(foreignAmount) ?? 0;
    final int krwAmount = (foreign * widget.rate.rate).round();

    return Scaffold(
      backgroundColor: const Color(0xFFF9F7FB),
      appBar: AppBar(
        title: Text("${widget.rate.code} 사기"),
        centerTitle: true,
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
      ),
      body: Column(
        children: [
          const SizedBox(height: 16),

          // 환율 텍스트
          Text(
            "1 ${widget.rate.code} = ${widget.rate.rate.toStringAsFixed(2)}원",
            style: const TextStyle(fontSize: 14, color: Colors.black54),
          ),

          const SizedBox(height: 4),


          Text(
            '기준일: ${widget.rate.regDt}',
            style: const TextStyle(
              fontSize: 12,
              color: Colors.black38,
            ),


          ),

          const SizedBox(height: 20), // ✅ 이거 추가



          const SizedBox(height: 20),

          // 외화 카드
          _currencyCard(
            flag: widget.rate.flagEmoji,
            title: widget.rate.name,
            amount: "$foreignAmount ${widget.rate.code}",
            isActive: true,
            balance: "잔액 $foreignBalance ${widget.rate.code}",
          ),

          const SizedBox(height: 12),

          // 스왑 아이콘
          Container(
            width: 36,
            height: 36,
            decoration: BoxDecoration(
              color: Colors.white,
              shape: BoxShape.circle,
              boxShadow: [
                BoxShadow(
                  color: Colors.black12,
                  blurRadius: 4,
                )
              ],
            ),
            child: const Icon(Icons.swap_vert),
          ),

          const SizedBox(height: 12),

          // KRW 카드
          _currencyCard(
            flag: "🇰🇷",
            title: "대한민국 원",
            amount: "$krwAmount KRW",
            isActive: false,
            balance: "잔액 ${krwBalance.toString()}원",
          ),

          const Spacer(),

          _keypad(),

          const SizedBox(height: 12),

          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: SizedBox(
              width: double.infinity,
              height: 52,
              child: ElevatedButton(
                onPressed: _handleAuthAndBuy,

                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF3F5073),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                ),
                child: const Text(
                  "확인",
                  style: TextStyle(fontSize: 16, color: Colors.white),
                ),
              ),
            ),
          ),

          const SizedBox(height: 16),
        ],
      ),
    );
  }

  Widget _currencyCard({
    required String flag,
    required String title,
    required String amount,
    required String balance,
    required bool isActive,
  }) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
      ),
      child: Row(
        children: [
          Text(flag, style: const TextStyle(fontSize: 28)),
          const SizedBox(width: 12),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(title, style: const TextStyle(fontSize: 14)),
              const SizedBox(height: 4),
              Text(
                balance,
                style: const TextStyle(fontSize: 12, color: Colors.black54),
              ),
            ],
          ),
          const Spacer(),
          Text(
            amount,
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: isActive ? Colors.blue : Colors.black54,
            ),
          ),
        ],
      ),
    );
  }

  Widget _keypad() {
    final keys = [
      "1","2","3",
      "4","5","6",
      "7","8","9",
      ".", "0", "back",
    ];

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: GridView.builder(
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 3,
          childAspectRatio: 2.2,
        ),
        itemCount: keys.length,
        itemBuilder: (context, index) {
          final key = keys[index];
          return GestureDetector(
            onTap: () => _onKeyTap(key),
            child: Center(
              child: key == "back"
                  ? const Icon(Icons.backspace_outlined)
                  : Text(
                key,
                style: const TextStyle(fontSize: 20),
              ),
            ),
          );
        },
      ),
    );
  }
}
