[//]: # (PROMPT)
คุณเป็น Senior Java Developer
อ่านไฟล์ CLAUDE.md ในroot project ก่อน แล้วทำตาม convention, naming, และ coding style ที่ระบุไว้ทุกข้อ
ช่วยเขียน Java สำหรับดึงข้อมูล Factsheet หุ้นจากเว็บ SET Thailand
โดยมีข้อกำหนดดังนี้:

════════════════════════════════════════════════
## 1. ข้อมูลที่ต้องดึง
════════════════════════════════════════════════
source: set.or.th/th/market/product/stock/quote/{TICKER}/factsheet

- ชื่อบริษัท / Ticker
- Paid-up (ล้านบาท)
- Market Cap (ล้านบาท)
- EV (ล้านบาท)
- EBITDA (ล้านบาท)
- EV/EBITDA
- Price (บาท)
- 52 Week High / Low
- P/E (X)
- P/BV (X)
- ปันผลย้อนหลัง 3-5 รอบ (dividendPerShare, xdDate, paymentDate)
  ════════════════════════════════════════════════
## 2. Tech Stack & Project Structure
════════════════════════════════════════════════
- Java 17+
- Spring Boot 3
- Gradle
- package หลัก: com.dashboard.retrieveInformation

สร้างไฟล์ทั้งหมดนี้ให้ครบ:
- FactsheetResponseDto.java
- ScraperService.java
- FactsheetService.java
- ScraperServiceImpl.java
- FactsheetServiceImpl.java
- SetFactsheetController.java

════════════════════════════════════════════════
## 3. DTO Design
════════════════════════════════════════════════

FactsheetResponseDto:
BigDecimal price;         // มาจากคอลัมน์ "Price (บาท)"
BigDecimal paidUp;        // มาจากคอลัมน์ "Paid-up (ล้านบาท)"
BigDecimal marketCap;     // มาจากคอลัมน์ "Market Cap (ล้านบาท)"
BigDecimal ev;            // มาจากคอลัมน์ "EV (ล้านบาท)"
BigDecimal ebitda;        // มาจากคอลัมน์ "EBITDA (ล้านบาท)"
BigDecimal evEbitda;      // มาจากคอลัมน์ "EV/EBITDA"
BigDecimal peRatio;       // มาจากคอลัมน์ "P/E (X)"
BigDecimal pbvRatio;      // มาจากคอลัมน์ "P/BV (X)"
BigDecimal fiftyTwoWeekHigh; // มาจากคอลัมน์ "52 Week High/Low" ส่วนซ้าย
BigDecimal fiftyTwoWeekLow;  // มาจากคอลัมน์ "52 Week High/Low" ส่วนขวา


════════════════════════════════════════════════
## 4. ScraperService
════════════════════════════════════════════════
- ถ้าได้รับ HTTP 403/4xx ให้ fallback scrape HTML จาก:
  https://www.set.or.th/th/market/product/stock/quote/{ticker}/factsheet

- ใช้ RestTemplate หรือ WebClient ตาม convention ใน CLAUDE.md
- ตั้ง timeout: connect 5s / read 10s

- ถ้า ticker ไม่พบ (body ว่าง หรือ 404) → throw BaseException
- ถ้าเกิด error อื่น → throw BaseException
  ════════════════════════════════════════════════
## 5. FactsheetService
════════════════════════════════════════════════

parseHtmlFallback(String html) → ValuationDto
- ใช้ Regex หรือ Jsoup (ตาม convention) parse ตาราง Factsheet
-   mapping JSON key → DTO field:
    "prior"         → price
    "paidUpCapital" → paidUp
    "marketCap"     → marketCap
    "ev"            → ev
    "ebitda"        → ebitda
    "evEbitda"      → evEbitda
    "pe"            → peRatio
    "pbv"           → pbvRatio
    "high52Week"    → fiftyTwoWeekHigh
    "low52Week"     → fiftyTwoWeekLow
    ════════════════════════════════════════════════
## 6. SetFactsheetController
════════════════════════════════════════════════
@RestController @RequestMapping("/api/v1/stocks") @Validated

@GetMapping("/{symbol}/factsheet")
════════════════════════════════════════════════
## 7. Coding Requirements
════════════════════════════════════════════════
- ทุก class ต้องทำตาม CLAUDE.md
  ════════════════════════════════════════════════
## Output ที่ต้องการ
════════════════════════════════════════════════
เขียนแต่ละไฟล์แยกกันชัดเจน พร้อม:
1. full package declaration
2. full import statements
3. code ที่ compile ได้ทันที
4. ไม่ละ method ใด ๆ (ห้ามเขียน // TODO)