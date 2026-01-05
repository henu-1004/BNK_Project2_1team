import 'package:flutter/material.dart';
import 'package:test_main/screens/deposit/survey.dart';
import 'package:test_main/screens/app_colors.dart';
import 'package:test_main/services/deposit_service.dart';
import 'package:test_main/services/survey_service.dart';
import 'package:test_main/models/survey.dart';
import 'package:test_main/models/deposit/application.dart';
import 'package:test_main/screens/deposit/step_1.dart';

class RecommendScreen extends StatefulWidget {
  static const routeName = "/recommend";

  const RecommendScreen({super.key});

  @override
  State<RecommendScreen> createState() => _RecommendScreenState();
}

class _RecommendScreenState extends State<RecommendScreen> {
  final SurveyService _surveyService = SurveyService();
  final DepositService _depositService = DepositService();

  late Future<_RecommendData> _future;

  @override
  void initState() {
    super.initState();
    _future = _loadRecommendations();
  }

  Future<_RecommendData> _loadRecommendations() async {
    final context = await _depositService.fetchContext();
    final custCode = context.customerCode ?? '';
    if (custCode.isEmpty) {
      throw Exception('고객 정보를 찾을 수 없습니다.');
    }
    final items = await _surveyService.fetchRecommendations(
      surveyId: DepositSurveyScreen.surveyId,
      custCode: custCode,
    );
    return _RecommendData(custCode, items);
  }

  Future<void> _handleQuickEnroll({
    required String custCode,
    required SurveyRecommendation recommendation,
  }) async {
    final prefill = await _surveyService.fetchPrefill(
      surveyId: DepositSurveyScreen.surveyId,
      custCode: custCode,
      productId: recommendation.dpstId,
    );

    final application = DepositApplication(dpstId: recommendation.dpstId)
      ..newCurrency = prefill.currency ?? ''
      ..newAmount = prefill.amount
      ..newPeriodMonths = prefill.periodMonths
      ..withdrawType = prefill.withdrawType ?? 'krw';

    if (!mounted) return;

    Navigator.pushNamed(
      context,
      DepositStep1Screen.routeName,
      arguments: DepositStep1Args(
        dpstId: recommendation.dpstId,
        prefill: application,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundOffWhite,
      appBar: AppBar(
        backgroundColor: AppColors.pointDustyNavy,
        title: const Text(
          "AI 외화예금 추천",
          style: TextStyle(color: Colors.white),
        ),
      ),

      body: FutureBuilder<_RecommendData>(
        future: _future,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }

          if (snapshot.hasError) {
            return Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    '추천 정보를 불러오는 중 오류가 발생했습니다.',
                    style: TextStyle(color: Colors.redAccent),
                  ),
                  const SizedBox(height: 12),
                  OutlinedButton.icon(
                    onPressed: () => setState(() => _future = _loadRecommendations()),
                    icon: const Icon(Icons.refresh),
                    label: const Text('다시 시도'),
                  ),
                ],
              ),
            );
          }

          final data = snapshot.data!;
          final recommendations = data.recommendations;

          return SingleChildScrollView(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _sectionTitle(" 내 투자 성향 기반 추천"),
                if (recommendations.isEmpty)
                  _emptyState()
                else
                  ...recommendations.map(
                        (item) => Padding(
                      padding: const EdgeInsets.only(bottom: 16),
                      child: _productCard(
                        name: item.dpstName,
                        desc: item.dpstInfo.isNotEmpty
                            ? item.dpstInfo
                            : '추천 상품 정보가 없습니다.',
                        tag: item.tag,
                        currency: item.dpstCurrency,
                        onSelect: () => _handleQuickEnroll(
                          custCode: data.custCode,
                          recommendation: item,
                        ),
                      ),
                    ),
                  ),
                const SizedBox(height: 20),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: () {
                      Navigator.pushNamed(
                        context,
                        DepositSurveyScreen.routeName,
                      );
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.pointDustyNavy,
                      padding: const EdgeInsets.symmetric(vertical: 15),
                      elevation: 0,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10),
                      ),
                    ),
                    child: const Text(
                      "외화예금 성향 테스트 다시하기",
                      style: TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.w600,
                        color: Colors.white,
                      ),
                    ),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  // -----------------------------------------------------
  // Section Title
  // -----------------------------------------------------
  Widget _sectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Text(
        title,
        style: const TextStyle(
          fontSize: 20,
          fontWeight: FontWeight.bold,
          color: AppColors.pointDustyNavy,
        ),
      ),
    );
  }

  // -----------------------------------------------------
  // Product Card UI
  // -----------------------------------------------------
  Widget _productCard({
    required String name,
    required String desc,
    required String tag,
    required String currency,
    required VoidCallback onSelect,
  }) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(
          color: AppColors.mainPaleBlue,
          width: 1.2,
        ),
      ),
      child: Row(
        children: [
          // 왼쪽 텍스트들
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  name,
                  style: const TextStyle(
                    fontSize: 17,
                    fontWeight: FontWeight.bold,
                    color: AppColors.pointDustyNavy,
                  ),
                ),
                const SizedBox(height: 8),

                Text(
                  desc,
                  style: const TextStyle(
                    fontSize: 14,
                    height: 1.4,
                    color: Colors.black87,
                  ),
                ),
                const SizedBox(height: 10),

                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: AppColors.mainPaleBlue.withOpacity(0.30),
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Text(
                    tag,
                    style: const TextStyle(
                      fontSize: 12,
                      color: AppColors.pointDustyNavy,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ],
            ),
          ),

          // 오른쪽 버튼
          Column(
            children: [
              Text(
                currency.isNotEmpty ? currency : "통화 정보",
                style: const TextStyle(fontSize: 12),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 8),
              ElevatedButton(
                onPressed: onSelect,
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.pointDustyNavy,
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  elevation: 0,
                ),
                child: const Text(
                  "빠른 가입",
                  style: TextStyle(fontSize: 12, color: Colors.white),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _emptyState() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.mainPaleBlue),
      ),
      child: const Text(
        '추천 결과가 아직 없습니다.\n설문을 완료하면 맞춤 추천을 받을 수 있어요.',
        textAlign: TextAlign.center,
        style: TextStyle(
          fontSize: 14,
          height: 1.5,
          color: AppColors.pointDustyNavy,
        ),
      ),
    );
  }
}

class _RecommendData {
  final String custCode;
  final List<SurveyRecommendation> recommendations;

  _RecommendData(this.custCode, this.recommendations);
}
