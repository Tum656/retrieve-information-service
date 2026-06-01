package com.retrieve_information_service.service.impl;

import com.retrieve_information_service.dto.*;
import com.retrieve_information_service.service.ParserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ParserServiceImpl implements ParserService {

    // Matches "งบปี 2568", "ปี 2568", or bare "2568"
    private static final Pattern ANNUAL_PATTERN    = Pattern.compile("(?:งบปี|ปี)?\\s*(\\d{4})\\s*$");
    // Matches "ไตรมาส 1/2569", "Q1/2569", "Q1 2569"
    private static final Pattern QUARTERLY_PATTERN = Pattern.compile("(?:ไตรมาส|Q)\\s*(\\d)[/\\s]*(\\d{4})");
    // Matches DD/MM/YYYY in column headers (Thai Buddhist year)
    private static final Pattern DATE_PATTERN      = Pattern.compile("(\\d{1,2})/(\\d{1,2})/(\\d{4})");

    @Override
    public StockHighlightsResponse parse(String html, String symbol) {
        try {
            Document doc = Jsoup.parse(html);

            String companyName = extractCompanyName(doc, symbol);

            Element financialTable = findTableContaining(doc, "สินทรัพย์รวม");
            Element ratioTable     = findTableContaining(doc, "ROA");
            Element statsTable     = findTableContaining(doc, "ราคาล่าสุด");

            log.info("Table detection for symbol={}: financial={} ratio={} stats={}",
                    symbol,
                    financialTable != null,
                    ratioTable != null,
                    statsTable != null);

            return StockHighlightsResponse.builder()
                    .symbol(symbol.toUpperCase())
                    .companyName(companyName)
                    .currency("THB")
                    .unit("million_baht")
                    .generatedAt(Instant.now().toString())
                    .financialStatements(parseFinancialStatements(financialTable))
                    .financialRatios(parseFinancialRatios(ratioTable))
                    .marketStatistics(parseMarketStatistics(statsTable))
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse HTML for symbol={}", symbol, e);
            throw new RuntimeException("Parse failed for symbol: " + symbol, e);
        }
    }

    // ── Section 1: บัญชีทางการเงินที่สำคัญ ─────────────────────────────────────

    private List<FinancialStatement> parseFinancialStatements(Element table) {
        try {
            if (table == null) return List.of();

            Elements rows   = table.select("tr");
            List<String> periods = extractHeaders(rows.first());
            int n = periods.size();

            List<Map<String, Double>> maps = initMaps(n);

            for (int r = 1; r < rows.size(); r++) {
                Elements cells = rows.get(r).select("td, th");
                if (cells.isEmpty()) continue;

                String field = mapFinancialLabel(cells.get(0).text().trim());
                if (field == null) continue;

                for (int i = 0; i < Math.min(n, cells.size() - 1); i++) {
                    maps.get(i).put(field, parseDouble(cells.get(i + 1).text()));
                }
            }

            List<FinancialStatement> result = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                String label = periods.get(i);
                String[] d   = parseThaiPeriod(label);
                Map<String, Double> m = maps.get(i);

                result.add(FinancialStatement.builder()
                        .periodLabel(label)
                        .periodStart(d[0])
                        .periodEnd(d[1])
                        .type(d[2])
                        .financials(Financials.builder()
                                .totalAssets(m.get("totalAssets"))
                                .totalLiabilities(m.get("totalLiabilities"))
                                .shareholdersEquity(m.get("shareholdersEquity"))
                                .paidUpCapital(m.get("paidUpCapital"))
                                .totalRevenue(m.get("totalRevenue"))
                                .otherIncome(m.get("otherIncome"))
                                .netProfit(m.get("netProfit"))
                                .eps(m.get("eps"))
                                .build())
                        .build());
            }
            return result;

        } catch (Exception e) {
            log.error("Error parsing financial statements table", e);
            return List.of();
        }
    }

    /** Maps Thai row labels to Java field names for Section 1. */
    private String mapFinancialLabel(String label) {
        if (label.contains("สินทรัพย์รวม"))                          return "totalAssets";
        if (label.contains("หนี้สินรวม"))                             return "totalLiabilities";
        if (label.contains("ส่วนของผู้ถือหุ้น"))                      return "shareholdersEquity";
        if (label.contains("มูลค่าหุ้นที่เรียกชำระ"))                 return "paidUpCapital";
        if (label.contains("รายได้รวม"))                               return "totalRevenue";
        if (label.contains("กำไร") && label.contains("กิจกรรมอื่น")) return "otherIncome";
        if (label.equalsIgnoreCase("กำไรสุทธิ"))                               return "netProfit";
        if (label.contains("กำไรต่อหุ้น") || label.equalsIgnoreCase("EPS")) return "eps";
        return null;
    }

    // ── Section 2: อัตราส่วนทางการเงินที่สำคัญ ──────────────────────────────────

    private List<FinancialRatios> parseFinancialRatios(Element table) {
        try {
            if (table == null) return List.of();

            Elements rows    = table.select("tr");
            List<String> periods = extractHeaders(rows.first());
            int n = periods.size();

            List<Map<String, Double>> maps = initMaps(n);

            for (int r = 1; r < rows.size(); r++) {
                Elements cells = rows.get(r).select("td, th");
                if (cells.isEmpty()) continue;

                String field = mapRatioLabel(cells.get(0).text().trim());
                if (field == null) continue;

                for (int i = 0; i < Math.min(n, cells.size() - 1); i++) {
                    maps.get(i).put(field, parseDouble(cells.get(i + 1).text()));
                }
            }

            List<FinancialRatios> result = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                String label = periods.get(i);
                String[] d   = parseThaiPeriod(label);
                Map<String, Double> m = maps.get(i);

                result.add(FinancialRatios.builder()
                        .periodLabel(label)
                        .asOfDate(d[1])        // period_end is the best proxy for as-of date
                        .roaPct(m.get("roaPct"))
                        .roePct(m.get("roePct"))
                        .netProfitMarginPct(m.get("netProfitMarginPct"))
                        .build());
            }
            return result;

        } catch (Exception e) {
            log.error("Error parsing financial ratios table", e);
            return List.of();
        }
    }

    /** Maps Thai row labels to Java field names for Section 2. */
    private String mapRatioLabel(String label) {
        String upper = label.toUpperCase();
        if (upper.contains("ROA"))              return "roaPct";
        if (upper.contains("ROE"))              return "roePct";
        if (label.contains("อัตรากำไรสุทธิ")) return "netProfitMarginPct";
        return null;
    }

    // ── Section 3: ค่าสถิติสำคัญ ณ วันที่ล่าสุด ─────────────────────────────────

    private List<MarketStatistics> parseMarketStatistics(Element table) {
        try {
            if (table == null) return List.of();

            Elements rows    = table.select("tr");
            List<String> periods = extractHeaders(rows.first());
            int n = periods.size();

            List<Map<String, Double>> maps = initMaps(n);

            for (int r = 1; r < rows.size(); r++) {
                Elements cells = rows.get(r).select("td, th");
                if (cells.isEmpty()) continue;

                String field = mapStatsLabel(cells.get(0).text().trim());
                if (field == null) continue;

                for (int i = 0; i < Math.min(n, cells.size() - 1); i++) {
                    maps.get(i).put(field, parseDouble(cells.get(i + 1).text()));
                }
            }

            List<MarketStatistics> result = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                String label    = periods.get(i);
                String statDate = parseDateLabel(label);
                Map<String, Double> m = maps.get(i);

                result.add(MarketStatistics.builder()
                        .statDate(statDate)
                        .lastPrice(m.get("lastPrice"))
                        .marketCapMillion(m.get("marketCapMillion"))
                        .ratioDate(statDate)
                        .pe(m.get("pe"))
                        .pbv(m.get("pbv"))
                        .bookValuePerShare(m.get("bookValuePerShare"))
                        .dividendYieldPct(m.get("dividendYieldPct"))
                        .build());
            }
            return result;

        } catch (Exception e) {
            log.error("Error parsing market statistics table", e);
            return List.of();
        }
    }

    /** Maps Thai row labels to Java field names for Section 3. */
    private String mapStatsLabel(String label) {
        if (label.contains("ราคาล่าสุด"))                              return "lastPrice";
        if (label.contains("มูลค่าหลักทรัพย์ตามราคาตลาด"))            return "marketCapMillion";
        if (label.equalsIgnoreCase("P/E") || label.contains("P/E"))    return "pe";
        if (label.equalsIgnoreCase("P/BV") || label.contains("P/BV")) return "pbv";
        if (label.contains("มูลค่าหุ้นทางบัญชีต่อหุ้น"))                   return "bookValuePerShare";
        if (label.contains("อัตราส่วนเงินปันผลตอบแทน"))               return "dividendYieldPct";
        return null;
    }

    // ── Shared Helpers ────────────────────────────────────────────────────────

    /**
     * Finds the first table that contains a cell with the given keyword.
     * Traverses all td/th elements in the document.
     */
    private Element findTableContaining(Document doc, String keyword) {
        for (Element cell : doc.select("td, th")) {
            if (cell.text().trim().contains(keyword)) {
                Element table = cell.closest("table");
                if (table != null) return table;
            }
        }
        return null;
    }

    /**
     * Extracts period labels from the first (header) row of a table,
     * skipping the first cell which is the row-name column.
     */
    private List<String> extractHeaders(Element headerRow) {
        List<String> headers = new ArrayList<>();
        if (headerRow == null) return headers;
        Elements cells = headerRow.select("th, td");
        for (int i = 1; i < cells.size(); i++) {
            String text = cells.get(i).text().trim();
            if (!text.isEmpty()) headers.add(text);
        }
        return headers;
    }

    /** Creates n empty LinkedHashMaps for accumulating field values per period. */
    private List<Map<String, Double>> initMaps(int n) {
        List<Map<String, Double>> maps = new ArrayList<>();
        for (int i = 0; i < n; i++) maps.add(new LinkedHashMap<>());
        return maps;
    }

    /**
     * Converts a Thai period label to ISO date strings and type.
     * Buddhist year (พ.ศ.) is converted to Gregorian by subtracting 543.
     *
     * <ul>
     *   <li>"งบปี 2568"       → ["2025-01-01", "2025-12-31", "annual"]</li>
     *   <li>"ไตรมาส 1/2569"   → ["2026-01-01", "2026-03-31", "quarterly"]</li>
     * </ul>
     *
     * @param label raw period label from the table header
     * @return String array [periodStart, periodEnd, type]
     */
    private String[] parseThaiPeriod(String label) {
        label = label.trim();

        Matcher qm = QUARTERLY_PATTERN.matcher(label);
        if (qm.find()) {
            int quarter = Integer.parseInt(qm.group(1));
            int gy      = Integer.parseInt(qm.group(2)) - 543;
            return switch (quarter) {
                case 1 -> new String[]{gy + "-01-01", gy + "-03-31", "quarterly"};
                case 2 -> new String[]{gy + "-04-01", gy + "-06-30", "quarterly"};
                case 3 -> new String[]{gy + "-07-01", gy + "-09-30", "quarterly"};
                case 4 -> new String[]{gy + "-10-01", gy + "-12-31", "quarterly"};
                default -> new String[]{null, null, "quarterly"};
            };
        }

        Matcher am = ANNUAL_PATTERN.matcher(label);
        if (am.find()) {
            int gy = Integer.parseInt(am.group(1)) - 543;
            return new String[]{gy + "-01-01", gy + "-12-31", "annual"};
        }

        return new String[]{null, null, "unknown"};
    }

    /**
     * Parses a date from a header label that contains DD/MM/YYYY (Thai Buddhist year).
     * Returns ISO date string in Gregorian calendar, or the raw label if no date is found.
     */
    private String parseDateLabel(String label) {
        Matcher dm = DATE_PATTERN.matcher(label);
        if (dm.find()) {
            int day   = Integer.parseInt(dm.group(1));
            int month = Integer.parseInt(dm.group(2));
            int year  = Integer.parseInt(dm.group(3));
            int gy    = year > 2400 ? year - 543 : year;
            return String.format("%04d-%02d-%02d", gy, month, day);
        }
        return label;
    }

    /**
     * Parses a raw number string, returning null for blanks and dash placeholders.
     * Handles comma-separated thousands and parentheses-wrapped negatives.
     */
    private Double parseDouble(String raw) {
        if (raw == null) return null;
        raw = raw.trim();
        if (raw.isEmpty() || raw.equals("-")
                || raw.equalsIgnoreCase("N/A")
                || raw.equalsIgnoreCase("n.a.")
                || raw.equalsIgnoreCase("n/a")) {
            return null;
        }

        boolean negative = raw.startsWith("(") && raw.endsWith(")");
        if (negative) raw = raw.substring(1, raw.length() - 1);

        try {
            double value = Double.parseDouble(raw.replace(",", ""));
            return negative ? -value : value;
        } catch (NumberFormatException e) {
            log.debug("Cannot parse number: '{}'", raw);
            return null;
        }
    }

    /** Extracts the company name from h1 or falls back to page title, then symbol. */
    private String extractCompanyName(Document doc, String fallback) {
        Element h1 = doc.selectFirst("h1");
        if (h1 != null && !h1.text().isBlank()) return h1.text().trim();
        Element title = doc.selectFirst("title");
        if (title != null && !title.text().isBlank()) return title.text().trim();
        return fallback.toUpperCase();
    }
}
