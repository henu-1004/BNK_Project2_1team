import 'package:flutter/material.dart';
import '../app_colors.dart';

import 'signup_18_addr.dart';


class CustomerInfoPage extends StatefulWidget {
  const CustomerInfoPage({super.key, required this.name, required this.rrn, required this.phone});
  final String name;
  final String rrn;
  final String phone;

  @override
  State<CustomerInfoPage> createState() => _CustomerInfoPageState();
}

class _CustomerInfoPageState extends State<CustomerInfoPage> {
  // 휴대폰
  String phonePrefix = "010";
  final TextEditingController phoneCtrl = TextEditingController();

  // 이메일
  final TextEditingController emailIdCtrl = TextEditingController();
  String emailDomain = "gmail.com";
  bool noEmail = false;

  // 주소
  String zipCode = "";
  String address = "";

  // 고객관리 안내수단
  String mailAgree = "자택";
  String phoneAgree = "수신";
  String emailAgree = "수신";
  String smsAgree = "수신";

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,

      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        leading: const BackButton(color: Colors.black),
        title: const Text("회원가입", style: TextStyle(color: Colors.black)),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text("취소", style: TextStyle(color: Colors.black54)),
          ),
        ],
      ),

      body: Column(
        children: [
          Expanded(
            child: ListView(
              padding: const EdgeInsets.symmetric(horizontal: 24),
              children: [
                const SizedBox(height: 24),

                const Text(
                  "고객정보 등록",
                  style: TextStyle(fontSize: 26, fontWeight: FontWeight.bold),
                ),

                const SizedBox(height: 32),

                _phoneInput(),
                const SizedBox(height: 32),

                _emailInput(),
                const SizedBox(height: 32),

                _addressInput(),
                const SizedBox(height: 32),

                const Divider(),

                const SizedBox(height: 24),
                const Text(
                  "고객관리 안내수단",
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 16),

                _radioRow("우편물", ["자택", "직장", "거부"], mailAgree,
                        (v) => setState(() => mailAgree = v)),
                _radioRow("전화", ["수신", "거부"], phoneAgree,
                        (v) => setState(() => phoneAgree = v)),
                _radioRow("이메일", ["수신", "거부"], emailAgree,
                        (v) => setState(() => emailAgree = v)),
                _radioRow("SMS", ["수신", "거부"], smsAgree,
                        (v) => setState(() => smsAgree = v)),

                const SizedBox(height: 20),
              ],
            ),
          ),

          // 하단 버튼
          SizedBox(
            width: double.infinity,
            height: 56,
            child: ElevatedButton(
              onPressed: () {},
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.pointDustyNavy,
                shape: const RoundedRectangleBorder(
                  borderRadius: BorderRadius.zero,
                ),
              ),
              child: const Text(
                "다음",
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: Colors.white,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  // -------------------------
  // 휴대폰 입력
  Widget _phoneInput() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text("휴대폰", style: TextStyle(color: Colors.black54)),
        const SizedBox(height: 6),
        Row(
          children: [
            DropdownButton<String>(
              value: phonePrefix,
              items: ["010", "011", "016"]
                  .map((e) => DropdownMenuItem(value: e, child: Text(e)))
                  .toList(),
              onChanged: (v) => setState(() => phonePrefix = v!),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: TextField(
                controller: phoneCtrl,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  hintText: "휴대폰 번호",
                  border: UnderlineInputBorder(),
                ),
              ),
            ),
          ],
        ),
      ],
    );
  }

  // -------------------------
  // 이메일 입력 + 도메인 선택
  Widget _emailInput() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text("이메일", style: TextStyle(color: Colors.black54)),
        const SizedBox(height: 6),

        Row(
          children: [
            Expanded(
              child: TextField(
                controller: emailIdCtrl,
                enabled: !noEmail,
                decoration: const InputDecoration(
                  hintText: "아이디",
                  border: UnderlineInputBorder(),
                ),
              ),
            ),
            const Padding(
              padding: EdgeInsets.symmetric(horizontal: 8),
              child: Text("@", style: TextStyle(fontSize: 18)),
            ),
            Expanded(
              child: InkWell(
                onTap: noEmail ? null : _showEmailDomainSheet,
                child: InputDecorator(
                  decoration: const InputDecoration(
                    border: UnderlineInputBorder(),
                  ),
                  child: Text(
                    emailDomain,
                    style: TextStyle(
                      color: noEmail ? Colors.grey : Colors.black,
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),

        const SizedBox(height: 8),

        InkWell(
          onTap: () => setState(() => noEmail = !noEmail),
          child: Row(
            children: [
              Icon(
                noEmail
                    ? Icons.check_circle
                    : Icons.radio_button_unchecked,
                color: noEmail
                    ? AppColors.mainPaleBlue
                    : Colors.grey,
              ),
              const SizedBox(width: 6),
              const Text("이메일 없음"),
            ],
          ),
        ),
      ],
    );
  }

  // -------------------------
  // 주소 입력
  Widget _addressInput() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text("자택주소", style: TextStyle(color: Colors.black54)),
        const SizedBox(height: 6),

        Row(
          children: [
            Expanded(
              child: Text(
                zipCode.isEmpty ? "우편번호" : zipCode,
                style: const TextStyle(fontSize: 16),
              ),
            ),
            ElevatedButton(
              onPressed: () async {
                final result = await Navigator.push<String>(
                  context,
                  MaterialPageRoute(builder: (_) => const AddressSearchPage()),
                );

                if (result != null) {
                  final parts = result.split('|');
                  setState(() {
                    zipCode = parts[0];
                    address = parts[1];
                  });
                }
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.pointDustyNavy,
              ),
              child: const Text("주소검색",
                style: TextStyle(color: Colors.white),),
            ),

          ],
        ),

        const SizedBox(height: 8),

        Text(
          address.isEmpty ? "주소" : address,
          style: const TextStyle(fontSize: 16),
        ),

        const SizedBox(height: 6),

        const Text(
          "입력된 주소가 다르면 주소검색 후 변경해 주세요",
          style: TextStyle(fontSize: 13, color: Colors.black45),
        ),
      ],
    );
  }

  // -------------------------
  // 이메일 도메인 바텀시트
  void _showEmailDomainSheet() {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (_) {
        final domains = [
          "직접입력",
          "naver.com",
          "gmail.com",
          "daum.net",
          "hanmail.net",
          "nate.com",
        ];

        return SafeArea(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const SizedBox(height: 16),
              const Text(
                "이메일도메인 선택",
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 16),
              ...domains.map((d) => ListTile(
                title: Center(child: Text(d)),
                onTap: () {
                  setState(() {
                    emailDomain = d == "직접입력" ? "" : d;
                  });
                  Navigator.pop(context);
                },
              )),
              const SizedBox(height: 20),
            ],
          ),
        );
      },
    );
  }

  // -------------------------
  // 라디오 행
  Widget _radioRow(
      String title,
      List<String> options,
      String value,
      void Function(String) onChanged,
      ) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title, style: const TextStyle(color: Colors.black54)),
        Row(
          children: options.map((o) {
            return Expanded(
              child: RadioListTile(
                value: o,
                groupValue: value,
                onChanged: (v) => onChanged(v!),
                title: Text(o),
                dense: true,
                contentPadding: EdgeInsets.zero,
              ),
            );
          }).toList(),
        ),
        const SizedBox(height: 8),
      ],
    );
  }
}
