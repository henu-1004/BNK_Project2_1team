import 'package:flutter/material.dart';

class MyAccountInfoScreen extends StatefulWidget {
  const MyAccountInfoScreen({super.key});

  @override
  State<MyAccountInfoScreen> createState() => _MyAccountInfoScreenState();
}

class _MyAccountInfoScreenState extends State<MyAccountInfoScreen> {
  bool _hideBalance = true;

  @override
  Widget build(BuildContext context) {
    final accounts = const <_AccountItem>[
      _AccountItem(
        name: '전용준의 통장',
        type: '입출금',
        number: '3333-20-6606173',
        balanceText: '1,234,567원',
      ),
      _AccountItem(
        name: '급여통장',
        type: '입출금',
        number: '123-456-789012',
        balanceText: '1,235원',
      ),
      _AccountItem(
        name: '생활비통장',
        type: '입출금',
        number: '123-456-789013',
        balanceText: '520원',
      ),
    ];

    return Scaffold(
      backgroundColor: const Color(0xFFF7F7F7),
      appBar: AppBar(
        backgroundColor: const Color(0xFFF7F7F7),
        surfaceTintColor: const Color(0xFFF7F7F7),
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.close, color: Colors.black87),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(20, 0, 20, 20),
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              const Expanded(
                child: Text(
                  '내 계좌',
                  style: TextStyle(
                    fontSize: 28,
                    fontWeight: FontWeight.w800,
                    color: Colors.black87,
                  ),
                ),
              ),
              IconButton(
                tooltip: _hideBalance ? '잔액 표시' : '잔액 가리기',
                onPressed: () => setState(() => _hideBalance = !_hideBalance),
                icon: Icon(
                  _hideBalance ? Icons.visibility_off : Icons.visibility,
                  color: Colors.black54,
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),

          // 요약 바 (총 n개)
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
            decoration: BoxDecoration(
              color: const Color(0xFFEFEFEF),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Row(
              children: [
                RichText(
                  text: TextSpan(
                    style: const TextStyle(
                      color: Colors.black87,
                      fontSize: 16,
                      fontWeight: FontWeight.w700,
                    ),
                    children: [
                      const TextSpan(text: '총 '),
                      TextSpan(
                        text: '${accounts.length}개',
                        style: const TextStyle(color: Color(0xFF2F6BFF)),
                      ),
                    ],
                  ),
                ),
                const Spacer(),
                Text(
                  _hideBalance ? '합계 ******' : '합계 (더미)',
                  style: const TextStyle(
                    color: Colors.black45,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
          ),

          const SizedBox(height: 14),

          ...accounts.map((a) => _AccountCard(item: a, hideBalance: _hideBalance)),

          const SizedBox(height: 24),

          // 하단 링크 느낌
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: const [
              Text('해지계좌', style: TextStyle(color: Colors.black45, fontWeight: FontWeight.w600)),
              SizedBox(width: 12),
              Text('|', style: TextStyle(color: Colors.black26)),
              SizedBox(width: 12),
              Text('휴면예금', style: TextStyle(color: Colors.black45, fontWeight: FontWeight.w600)),
            ],
          ),
        ],
      ),
    );
  }
}

class _AccountCard extends StatelessWidget {
  const _AccountCard({
    required this.item,
    required this.hideBalance,
  });

  final _AccountItem item;
  final bool hideBalance;

  String _mask(String s) => '••••••';

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.fromLTRB(18, 16, 18, 16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 10,
            offset: const Offset(0, 6),
          ),
        ],
      ),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item.name,
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.w800,
                    color: Colors.black87,
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  '${item.type} · ${item.number}',
                  style: const TextStyle(
                    color: Colors.black45,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
          ),
          Text(
            hideBalance ? _mask(item.balanceText) : item.balanceText,
            style: const TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w800,
              color: Colors.black87,
            ),
          ),
          const SizedBox(width: 6),
          const Icon(Icons.chevron_right, color: Colors.black38),
        ],
      ),
    );
  }
}

class _AccountItem {
  final String name;
  final String type;
  final String number;
  final String balanceText;

  const _AccountItem({
    required this.name,
    required this.type,
    required this.number,
    required this.balanceText,
  });
}
