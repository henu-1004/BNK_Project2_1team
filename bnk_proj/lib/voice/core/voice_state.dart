
/// 음성 비서 상태
enum VoiceState {
  s0Idle,
  s1Recommend,
  s2ProductExplain,
  s3JoinConfirm,

  s4Terms,
  s4Input,
  s4Confirm,
  s4Signature,

  s5End;

  static VoiceState from(String value) {
    switch (value) {
      case 'S0_IDLE':
        return VoiceState.s0Idle;
      case 'S1_RECOMMEND':
        return VoiceState.s1Recommend;
      case 'S2_PROD_EXPLAIN':
        return VoiceState.s2ProductExplain;
      case 'S3_JOIN_CONFIRM':
        return VoiceState.s3JoinConfirm;
      case 'S4_1_TERMS':
        return VoiceState.s4Terms;
      case 'S4_2_INPUT':
        return VoiceState.s4Input;
      case 'S4_3_CONFIRM':
        return VoiceState.s4Confirm;
      case 'S4_4_SIGNATURE':
        return VoiceState.s4Signature;
      case 'S5_END':
        return VoiceState.s5End;
      default:
        throw Exception('Unknown VoiceState: $value');
    }
  }
}

