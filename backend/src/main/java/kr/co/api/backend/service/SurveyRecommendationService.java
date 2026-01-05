package kr.co.api.backend.service;

import kr.co.api.backend.dto.survey.*;
import kr.co.api.backend.mapper.SurveyMapper;
import kr.co.api.backend.mapper.SurveyRecommendationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyRecommendationService {

    private static final String TYPE_STABLE = "TYPE_STABLE";
    private static final String TYPE_LIQUID = "TYPE_LIQUID";
    private static final String TYPE_FX = "TYPE_FX";
    private static final String TYPE_OVERSEAS = "TYPE_OVERSEAS";
    private static final String TYPE_EVENT = "TYPE_EVENT";

    private final SurveyRecommendationMapper recommendationMapper;
    private final SurveyMapper surveyMapper;

    public SurveyRecommendationResponseDTO getRecommendations(String custCode, Long surveyId) {
        List<RecoProductDTO> products = recommendationMapper.selectRecoTop3(custCode, surveyId);
        if (products.isEmpty()) {
            refreshTop3(custCode, surveyId);
            products = recommendationMapper.selectRecoTop3(custCode, surveyId);
        }

        String tag = resolveRecommendationTag(custCode, surveyId);

        for (RecoProductDTO product : products) {
            product.setTag(tag);
        }

        SurveyRecommendationResponseDTO response = new SurveyRecommendationResponseDTO();
        response.setSurveyId(surveyId);
        response.setCustCode(custCode);
        response.setProducts(products);
        return response;
    }

    public SurveyPrefillResponseDTO buildPrefill(String custCode, Long surveyId, String productId) {
        Long respId = recommendationMapper.selectLatestRespId(surveyId, custCode);
        if (respId == null) {
            SurveyPrefillResponseDTO empty = new SurveyPrefillResponseDTO();
            empty.setProductId(productId);
            return empty;
        }

        List<SurveyResponseDetailDTO> details = recommendationMapper.selectResponseDetails(respId);
        List<Long> optIds = details.stream()
                .map(SurveyResponseDetailDTO::getOptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Long, List<String>> valuesByQId = fetchOptionValues(optIds);

        SurveyPrefillResponseDTO dto = new SurveyPrefillResponseDTO();
        dto.setProductId(productId);
        dto.setCurrency(firstValue(valuesByQId, 4L));
        dto.setPeriodMonths(parseInt(firstValue(valuesByQId, 6L)));
        dto.setAmount(resolveAmount(firstValue(valuesByQId, 5L)));
        dto.setWithdrawType("krw");
        dto.setAccountPreference(firstValue(valuesByQId, 9L));
        return dto;
    }

    @Transactional
    public void refreshTop3(String custCode, Long surveyId) {
        if (custCode == null || custCode.isBlank() || surveyId == null) {
            return;
        }

        Long respId = recommendationMapper.selectLatestRespId(surveyId, custCode);
        if (respId == null) {
            log.info("[RECO] No response found for custCode={}, surveyId={}", custCode, surveyId);
            return;
        }

        List<SurveyResponseDetailDTO> details = recommendationMapper.selectResponseDetails(respId);
        List<Long> optIds = details.stream()
                .map(SurveyResponseDetailDTO::getOptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Long, List<String>> valuesByQId = fetchOptionValues(optIds);
        String type = resolveType(valuesByQId);

        List<RecoCandidateDTO> candidates = recommendationMapper.selectRecoCandidates();
        Map<String, Double> collabScores = computeCollaborativeScores(
                surveyId,
                respId,
                custCode,
                new HashSet<>(optIds)
        );

        List<ScoredProduct> scored = new ArrayList<>();
        Set<String> selectedCurrencies = new HashSet<>(valuesByQId.getOrDefault(4L, Collections.emptyList()));

        for (RecoCandidateDTO candidate : candidates) {
            double score = baseScoreForType(candidate, type);
            score += scoreCurrencyMatch(candidate, selectedCurrencies);
            score += scorePreference(candidate, valuesByQId);
            score += collabScores.getOrDefault(candidate.getDpstId(), 0.0);
            scored.add(new ScoredProduct(candidate, score));
        }

        scored.sort(Comparator.comparingDouble(ScoredProduct::score).reversed()
                .thenComparing(scoredProduct -> scoredProduct.candidate().getDpstId()));

        List<ScoredProduct> top3 = scored.stream().limit(3).toList();

        recommendationMapper.deleteRecoTop3(custCode, surveyId);

        if (top3.isEmpty()) {
            return;
        }

        List<RecoTop3RowDTO> rows = new ArrayList<>();
        for (int i = 0; i < top3.size(); i++) {
            RecoTop3RowDTO row = new RecoTop3RowDTO();
            row.setCustCode(custCode);
            row.setSurveyId(surveyId);
            row.setRankNo(i + 1);
            row.setProductId(top3.get(i).candidate().getDpstId());
            rows.add(row);
        }

        recommendationMapper.insertRecoTop3(rows);
    }

    private Map<Long, List<String>> fetchOptionValues(List<Long> optIds) {
        if (optIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<SurveyOptionValueDTO> optionValues = surveyMapper.selectOptionValues(optIds);
        Map<Long, List<String>> valuesByQId = new HashMap<>();
        for (SurveyOptionValueDTO option : optionValues) {
            if (option == null || option.getQId() == null) {
                continue;
            }
            valuesByQId.computeIfAbsent(option.getQId(), k -> new ArrayList<>())
                    .add(option.getOptValue());
        }
        return valuesByQId;
    }

    private String resolveType(Map<Long, List<String>> valuesByQId) {
        List<String> resultValues = valuesByQId.getOrDefault(10L, Collections.emptyList());
        if (!resultValues.isEmpty()) {
            return resultValues.get(0);
        }

        if (containsValue(valuesByQId, 3L, "LIQ_NEED")) return TYPE_LIQUID;
        if (containsValue(valuesByQId, 1L, "GOAL_STABLE")) return TYPE_STABLE;
        if (containsValue(valuesByQId, 1L, "GOAL_FX")) return TYPE_FX;
        if (containsValue(valuesByQId, 1L, "GOAL_OVERSEAS")) return TYPE_OVERSEAS;
        if (containsValue(valuesByQId, 1L, "GOAL_EVENT")) return TYPE_EVENT;
        if (containsValue(valuesByQId, 2L, "PRIOR_LIQ")) return TYPE_LIQUID;
        if (containsValue(valuesByQId, 2L, "PRIOR_RATE")) return TYPE_STABLE;
        if (containsValue(valuesByQId, 2L, "PRIOR_FX")) return TYPE_FX;
        if (containsValue(valuesByQId, 2L, "PRIOR_EVENT")) return TYPE_EVENT;

        return TYPE_STABLE;
    }

    private String resolveRecommendationTag(String custCode, Long surveyId) {
        Long respId = recommendationMapper.selectLatestRespId(surveyId, custCode);
        if (respId == null) {
            return "AI 추천";
        }
        List<SurveyResponseDetailDTO> details = recommendationMapper.selectResponseDetails(respId);
        List<Long> optIds = details.stream()
                .map(SurveyResponseDetailDTO::getOptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, List<String>> valuesByQId = fetchOptionValues(optIds);
        String type = resolveType(valuesByQId);
        return mapTagByType(type);
    }

    private String mapTagByType(String type) {
        return switch (type) {
            case TYPE_LIQUID -> "유동성 추천";
            case TYPE_FX -> "환율 활용 추천";
            case TYPE_OVERSEAS -> "해외사용 추천";
            case TYPE_EVENT -> "이벤트 추천";
            default -> "안정형 추천";
        };
    }

    private double baseScoreForType(RecoCandidateDTO candidate, String type) {
        double score = 0;
        String name = safeLower(candidate.getDpstName());
        String info = safeLower(candidate.getDpstInfo());
        String desc = safeLower(candidate.getDpstDescript());
        String text = name + " " + info + " " + desc;

        switch (type) {
            case TYPE_LIQUID -> {
                if (isYes(candidate.getDpstPartWdrwYn())) score += 25;
                if (isYes(candidate.getDpstAddPayYn())) score += 10;
                if (Objects.equals(candidate.getDpstType(), 2)) score += 10;
            }
            case TYPE_FX -> {
                if (isYes(candidate.getDpstAddPayYn())) score += 20;
                if (text.contains("환율")) score += 10;
                if (candidate.getDpstCurrency() != null && candidate.getDpstCurrency().contains("JPY")) score += 5;
            }
            case TYPE_OVERSEAS -> {
                int currencyCount = countCurrencies(candidate.getDpstCurrency());
                if (currencyCount >= 4) score += 20;
                if (text.contains("해외") || text.contains("유학") || text.contains("여행")) score += 15;
            }
            case TYPE_EVENT -> {
                if (text.contains("이벤트") || text.contains("추천") || text.contains("경품") || text.contains("보너스") || text.contains("혜택")) {
                    score += 25;
                }
            }
            default -> {
                if (Objects.equals(candidate.getDpstRateType(), 1)) score += 20;
                if (isYes(candidate.getDpstAutoRenewYn())) score += 10;
                if (Objects.equals(candidate.getDpstType(), 1)) score += 10;
            }
        }
        return score;
    }

    private double scoreCurrencyMatch(RecoCandidateDTO candidate, Set<String> selectedCurrencies) {
        if (selectedCurrencies.isEmpty() || candidate.getDpstCurrency() == null) {
            return 0;
        }

        double score = 0;
        for (String currency : selectedCurrencies) {
            if (currency == null) continue;
            if (candidate.getDpstCurrency().contains(currency)) {
                score += 6;
            } else {
                score -= 2;
            }
        }
        return score;
    }

    private double scorePreference(RecoCandidateDTO candidate, Map<Long, List<String>> valuesByQId) {
        double score = 0;
        if (containsValue(valuesByQId, 2L, "PRIOR_LIQ") && isYes(candidate.getDpstPartWdrwYn())) {
            score += 10;
        }
        if (containsValue(valuesByQId, 2L, "PRIOR_RATE") && Objects.equals(candidate.getDpstRateType(), 1)) {
            score += 10;
        }
        if (containsValue(valuesByQId, 2L, "PRIOR_FX")) {
            String info = safeLower(candidate.getDpstInfo()) + " " + safeLower(candidate.getDpstDescript());
            if (info.contains("환율")) score += 10;
        }
        if (containsValue(valuesByQId, 3L, "LIQ_NEED") && isYes(candidate.getDpstPartWdrwYn())) {
            score += 8;
        }
        return score;
    }

    private Map<String, Double> computeCollaborativeScores(
            Long surveyId,
            Long respId,
            String custCode,
            Set<Long> currentOptIds
    ) {
        if (currentOptIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<SimilarResponseOptionDTO> rows = recommendationMapper.selectOtherResponseOptions(surveyId, custCode);
        if (rows.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Set<Long>> optIdsByResp = new HashMap<>();
        Map<Long, String> custByResp = new HashMap<>();

        for (SimilarResponseOptionDTO row : rows) {
            optIdsByResp.computeIfAbsent(row.getRespId(), k -> new HashSet<>()).add(row.getOptId());
            custByResp.put(row.getRespId(), row.getCustCode());
        }

        List<SimilarCustomer> similarCustomers = new ArrayList<>();
        for (Map.Entry<Long, Set<Long>> entry : optIdsByResp.entrySet()) {
            double similarity = jaccard(currentOptIds, entry.getValue());
            if (similarity >= 0.3) {
                similarCustomers.add(new SimilarCustomer(custByResp.get(entry.getKey()), similarity));
            }
        }

        similarCustomers.sort(Comparator.comparingDouble(SimilarCustomer::similarity).reversed());

        List<SimilarCustomer> top = similarCustomers.stream().limit(3).toList();
        if (top.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> custCodes = top.stream()
                .map(SimilarCustomer::custCode)
                .filter(Objects::nonNull)
                .toList();

        if (custCodes.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Double> similarityByCust = top.stream()
                .filter(customer -> customer.custCode() != null)
                .collect(Collectors.toMap(SimilarCustomer::custCode, SimilarCustomer::similarity, Math::max));

        List<SimilarPurchaseDTO> purchases = recommendationMapper.selectSimilarCustomerPurchases(custCodes);
        Map<String, Double> scores = new HashMap<>();
        for (SimilarPurchaseDTO purchase : purchases) {
            Double similarity = similarityByCust.get(purchase.getCustCode());
            if (similarity == null) {
                continue;
            }
            double weight = similarity * (purchase.getCnt() != null ? purchase.getCnt() : 1) * 10;
            scores.merge(purchase.getProductId(), weight, Double::sum);
        }
        return scores;
    }

    private double jaccard(Set<Long> a, Set<Long> b) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        Set<Long> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<Long> union = new HashSet<>(a);
        union.addAll(b);
        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }

    private boolean containsValue(Map<Long, List<String>> valuesByQId, Long qId, String target) {
        return valuesByQId.getOrDefault(qId, Collections.emptyList()).contains(target);
    }

    private String firstValue(Map<Long, List<String>> valuesByQId, Long qId) {
        List<String> values = valuesByQId.getOrDefault(qId, Collections.emptyList());
        if (values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    private Integer parseInt(String value) {
        if (value == null) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer resolveAmount(String amountKey) {
        if (amountKey == null) return null;
        return switch (amountKey) {
            case "AMT_LT_1M" -> 500_000;
            case "AMT_1_5M" -> 3_000_000;
            case "AMT_5_10M" -> 7_500_000;
            case "AMT_GT_10M" -> 15_000_000;
            default -> null;
        };
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private boolean isYes(String value) {
        return "Y".equalsIgnoreCase(value);
    }

    private int countCurrencies(String currencies) {
        if (currencies == null || currencies.isBlank()) return 0;
        return (int) Arrays.stream(currencies.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .count();
    }

    private record ScoredProduct(RecoCandidateDTO candidate, double score) {
    }

    private record SimilarCustomer(String custCode, double similarity) {
    }
}
