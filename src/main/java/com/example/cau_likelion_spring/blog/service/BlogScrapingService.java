package com.example.cau_likelion_spring.blog.service;

import com.example.cau_likelion_spring.blog.dto.BlogScrapingResponse;
import com.example.cau_likelion_spring.global.exception.CustomException;
import com.example.cau_likelion_spring.global.exception.ErrorCode;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Open Graph → Twitter Card → JSON-LD(schema.org) → 순수 HTML 순으로
 * 필드별 fallback을 시도해 특정 블로그 플랫폼에 종속되지 않는 링크 미리보기를 생성한다.
 */
@Service
@RequiredArgsConstructor
public class BlogScrapingService {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";
    private static final int TIMEOUT_MILLIS = 5000;
    private static final int MIN_IMAGE_SIZE = 100;
    private static final int MIN_PARAGRAPH_LENGTH = 10;
    private static final int DESCRIPTION_PARAGRAPH_LIMIT = 3;
    private static final int DESCRIPTION_MAX_LENGTH = 150;
    /** 네이버 등 일부 플랫폼은 og:description을 자체적으로 짧게(80자 내외) 잘라서 내려주므로, 이보다 짧으면 본문 크롤링으로 폴백한다 */
    private static final int DESCRIPTION_MIN_LENGTH = 100;

    private static final String[] CONTENT_CONTAINER_SELECTORS = {
            "article", "main", ".post-content", ".entry-content", "#content", ".article-body",
            ".se-main-container", "body"
    };

    private static final String[] DATE_META_SELECTORS = {
            "meta[property=article:published_time]",
            "meta[name=article:published_time]",
            "meta[property=og:article:published_time]",
            "meta[property=og:published_time]",
            "meta[name=publish-date]",
            "meta[name=pubdate]",
            "meta[name=date]",
            "meta[name=parsely-pub-date]",
            "meta[name=sailthru.date]",
            "meta[itemprop=datePublished]"
    };

    /** 사이트마다 클래스명이 제각각(blog_date, wrt_date, entry-date ...)이라 "date"를 포함하는 class/id는 전부 후보로 본다 */
    private static final String DATE_TEXT_SELECTOR =
            ".date, .post-date, .published, .publish-date, .article-date, "
                    + "[class*=date], [class*=Date], [id*=date], [id*=Date]";

    /**
     * SPA(React/Next 등)는 meta·time·JSON-LD 없이 하이드레이션용 JS 상태 객체에만
     * 작성일을 담아 보내는 경우가 많다(예: velog의 "released_at"). 흔히 쓰이는 필드명으로 스캔한다.
     */
    private static final Pattern SCRIPT_STATE_DATE_PATTERN = Pattern.compile(
            "\"(?:publishedAt|pubDate|datePublished|createdAt|created_at|releasedAt|released_at"
                    + "|releaseDate|postDate|writtenAt|regDate|articleDate)\"\\s*:\\s*\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern EXCLUDE_IMAGE_PATTERN = Pattern.compile("(?i)(icon|logo|sprite)");

    /** "2026-2-10", "2026.02.10", "2026. 2. 10." 처럼 구분자 앞뒤 공백/한글 단위까지 허용 */
    private static final Pattern DATE_TEXT_PATTERN = Pattern.compile(
            "(\\d{4})\\s*[-./년]\\s*(\\d{1,2})\\s*[-./월]\\s*(\\d{1,2})\\s*일?");

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ISO_DATE,
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)
    );

    private final ObjectMapper objectMapper;

    public BlogScrapingResponse scrape(String url) {
        validateUrl(url);
        Document doc = resolveContentDocument(fetchDocument(url), url);

        return new BlogScrapingResponse(
                url,
                extractTitle(doc),
                extractThumbnail(doc),
                truncate(extractDescription(doc)),
                extractPublishedDate(doc)
        );
    }

    private void validateUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                throw new CustomException(ErrorCode.INVALID_SCRAPING_URL, "유효하지 않은 URL입니다. url=" + url);
            }
        } catch (URISyntaxException e) {
            throw new CustomException(ErrorCode.INVALID_SCRAPING_URL, "유효하지 않은 URL입니다. url=" + url);
        }
    }

    private Document fetchDocument(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MILLIS)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .get();
        } catch (IOException e) {
            throw new CustomException(ErrorCode.BLOG_SCRAPING_FAILED, "블로그 페이지를 불러올 수 없습니다. url=" + url);
        }
    }

    /**
     * 네이버 PC 블로그처럼 실제 본문을 iframe(mainFrame) 안에 담아 내려주는 래퍼 페이지 대응.
     * 래퍼는 head에 og 태그가 없어 모든 추출이 실패하므로, og:title이 없고 본문 iframe이 있으면
     * 그 iframe 문서를 한 번 더 가져온다(최대 1홉). 무한 리다이렉트/서드파티 위젯 iframe을 피하기 위해
     * iframe URL이 원본과 같은 호스트일 때만 따라간다. og:title이 있는 일반 페이지는 그대로 통과한다.
     */
    private Document resolveContentDocument(Document doc, String originalUrl) {
        if (StringUtils.hasText(metaContent(doc, "meta[property=og:title]"))) {
            return doc;
        }

        Element frame = doc.selectFirst("#mainFrame[src]");
        if (frame == null) {
            frame = doc.selectFirst("iframe[src], frame[src]");
        }
        if (frame == null) {
            return doc;
        }

        String frameUrl = frame.absUrl("src");
        if (!isSameHostHttpUrl(frameUrl, originalUrl)) {
            return doc;
        }

        try {
            return fetchDocument(frameUrl);
        } catch (CustomException e) {
            return doc;
        }
    }

    private boolean isSameHostHttpUrl(String candidate, String originalUrl) {
        if (!StringUtils.hasText(candidate)) {
            return false;
        }
        try {
            URI frameUri = new URI(candidate);
            String scheme = frameUri.getScheme();
            if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                return false;
            }
            return frameUri.getHost() != null
                    && frameUri.getHost().equalsIgnoreCase(new URI(originalUrl).getHost());
        } catch (URISyntaxException e) {
            return false;
        }
    }

    // ---------- 제목 ----------

    private String extractTitle(Document doc) {
        String ogTitle = metaContent(doc, "meta[property=og:title]");
        if (StringUtils.hasText(ogTitle)) {
            return ogTitle;
        }
        String twitterTitle = metaContent(doc, "meta[name=twitter:title]");
        if (StringUtils.hasText(twitterTitle)) {
            return twitterTitle;
        }
        String title = doc.title();
        return StringUtils.hasText(title) ? title : null;
    }

    // ---------- 썸네일 ----------

    private String extractThumbnail(Document doc) {
        String ogImage = doc.select("meta[property=og:image]").attr("abs:content");
        if (StringUtils.hasText(ogImage)) {
            return ogImage;
        }
        String twitterImage = doc.select("meta[name=twitter:image]").attr("abs:content");
        if (StringUtils.hasText(twitterImage)) {
            return twitterImage;
        }
        String jsonLdImage = extractFromJsonLd(doc, "image");
        if (StringUtils.hasText(jsonLdImage)) {
            return resolveAbsoluteUrl(doc, jsonLdImage);
        }
        return extractFirstContentImage(doc);
    }

    private String extractFirstContentImage(Document doc) {
        for (Element img : doc.select("img[src]")) {
            String src = img.absUrl("src");
            if (!StringUtils.hasText(src)) {
                continue;
            }
            if (isTooSmall(img) || EXCLUDE_IMAGE_PATTERN.matcher(src).find()) {
                continue;
            }
            return src;
        }
        return null;
    }

    private boolean isTooSmall(Element img) {
        int width = parseIntSafely(img.attr("width"));
        int height = parseIntSafely(img.attr("height"));
        return (width > 0 && width < MIN_IMAGE_SIZE) || (height > 0 && height < MIN_IMAGE_SIZE);
    }

    private int parseIntSafely(String value) {
        if (!StringUtils.hasText(value)) {
            return -1;
        }
        try {
            return Integer.parseInt(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ---------- 설명 (초반 본문 3줄) ----------

    private String extractDescription(Document doc) {
        String ogDescription = metaContent(doc, "meta[property=og:description]");
        if (isLongEnough(ogDescription)) {
            return ogDescription;
        }
        String twitterDescription = metaContent(doc, "meta[name=twitter:description]");
        if (isLongEnough(twitterDescription)) {
            return twitterDescription;
        }
        String metaDescription = metaContent(doc, "meta[name=description]");
        if (isLongEnough(metaDescription)) {
            return metaDescription;
        }

        String paragraphs = extractFirstParagraphs(doc);
        if (StringUtils.hasText(paragraphs)) {
            return paragraphs;
        }

        // 본문 크롤링도 실패했다면, 짧더라도 메타 설명 중 하나를 마지막 fallback으로 사용
        if (StringUtils.hasText(ogDescription)) {
            return ogDescription;
        }
        if (StringUtils.hasText(twitterDescription)) {
            return twitterDescription;
        }
        return metaDescription;
    }

    private boolean isLongEnough(String text) {
        return StringUtils.hasText(text) && text.length() >= DESCRIPTION_MIN_LENGTH;
    }

    private String extractFirstParagraphs(Document doc) {
        for (String selector : CONTENT_CONTAINER_SELECTORS) {
            Element container = doc.selectFirst(selector);
            if (container == null) {
                continue;
            }
            List<String> paragraphs = container.select("p").stream()
                    .map(Element::text)
                    .map(String::trim)
                    .filter(text -> text.length() > MIN_PARAGRAPH_LENGTH)
                    .limit(DESCRIPTION_PARAGRAPH_LIMIT)
                    .toList();
            if (!paragraphs.isEmpty()) {
                return String.join("\n", paragraphs);
            }
        }
        return null;
    }

    // ---------- 작성일 ----------

    private String extractPublishedDate(Document doc) {
        for (String selector : DATE_META_SELECTORS) {
            String parsed = parseDate(doc.select(selector).attr("content"));
            if (parsed != null) {
                return parsed;
            }
        }

        Element time = doc.selectFirst("time[datetime]");
        if (time != null) {
            String parsed = parseDate(time.attr("datetime"));
            if (parsed != null) {
                return parsed;
            }
        }

        String jsonLdParsed = parseDate(extractFromJsonLd(doc, "datePublished"));
        if (jsonLdParsed != null) {
            return jsonLdParsed;
        }

        String scriptStateParsed = extractFromScriptState(doc);
        if (scriptStateParsed != null) {
            return scriptStateParsed;
        }

        Element dateEl = doc.selectFirst(DATE_TEXT_SELECTOR);
        if (dateEl != null) {
            String parsed = parseDate(extractDateByRegex(dateEl.text()));
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    /** head 태그의 meta/time/JSON-LD에는 없고 하이드레이션용 SPA 상태 스크립트에만 작성일이 박혀있는 사이트(velog 등) 대응 */
    private String extractFromScriptState(Document doc) {
        for (Element script : doc.select("script")) {
            String data = script.data();
            if (!StringUtils.hasText(data)) {
                continue;
            }
            Matcher matcher = SCRIPT_STATE_DATE_PATTERN.matcher(data);
            if (matcher.find()) {
                String parsed = parseDate(matcher.group(1));
                if (parsed != null) {
                    return parsed;
                }
            }
        }
        return null;
    }

    private String extractDateByRegex(String text) {
        Matcher matcher = DATE_TEXT_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        int year = Integer.parseInt(matcher.group(1));
        int month = Integer.parseInt(matcher.group(2));
        int day = Integer.parseInt(matcher.group(3));
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    private String parseDate(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String value = raw.trim();

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                TemporalAccessor parsed = formatter.parse(value);
                return LocalDate.from(parsed).format(DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                // 다음 포맷으로 재시도
            }
        }

        try {
            return OffsetDateTime.parse(value).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    // ---------- JSON-LD (schema.org) ----------

    private String extractFromJsonLd(Document doc, String fieldName) {
        for (Element script : doc.select("script[type=application/ld+json]")) {
            try {
                JsonNode root = objectMapper.readTree(script.data());
                JsonNode found = findJsonLdField(root, fieldName);
                String value = jsonLdValueToString(found);
                if (StringUtils.hasText(value)) {
                    return value;
                }
            } catch (Exception e) {
                // 형식이 깨진 JSON-LD 블록은 건너뛴다
            }
        }
        return null;
    }

    private JsonNode findJsonLdField(JsonNode node, String fieldName) {
        if (node == null) {
            return null;
        }
        if (node.isObject()) {
            if (node.has(fieldName)) {
                return node.get(fieldName);
            }
            for (JsonNode child : node) {
                JsonNode found = findJsonLdField(child, fieldName);
                if (found != null) {
                    return found;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                JsonNode found = findJsonLdField(element, fieldName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private String jsonLdValueToString(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.isString()) {
            return node.asString();
        }
        if (node.isArray() && !node.isEmpty()) {
            return jsonLdValueToString(node.get(0));
        }
        if (node.isObject()) {
            if (node.has("url")) {
                return node.get("url").asString();
            }
            if (node.has("@id")) {
                return node.get("@id").asString();
            }
        }
        return null;
    }

    // ---------- 공통 ----------

    private String metaContent(Document doc, String selector) {
        String content = doc.select(selector).attr("content");
        return StringUtils.hasText(content) ? content.trim() : null;
    }

    private String resolveAbsoluteUrl(Document doc, String maybeRelative) {
        try {
            return new URI(doc.baseUri()).resolve(maybeRelative).toString();
        } catch (Exception e) {
            return maybeRelative;
        }
    }

    private String truncate(String text) {
        if (text == null || text.length() <= DESCRIPTION_MAX_LENGTH) {
            return text;
        }
        return text.substring(0, DESCRIPTION_MAX_LENGTH) + "...";
    }
}
