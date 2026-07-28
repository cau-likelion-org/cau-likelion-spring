package com.example.cau_likelion_spring.blog.service;

import com.example.cau_likelion_spring.blog.dto.BlogScrapingResponse;
import com.example.cau_likelion_spring.blog.exception.BlogScrapingFetchException;
import com.example.cau_likelion_spring.blog.exception.InvalidScrapingUrlException;
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

    private static final String[] CONTENT_CONTAINER_SELECTORS = {
            "article", "main", ".post-content", ".entry-content", "#content", ".article-body", "body"
    };

    private static final String[] DATE_META_SELECTORS = {
            "meta[property=article:published_time]",
            "meta[name=article:published_time]",
            "meta[property=og:article:published_time]",
            "meta[name=publish-date]",
            "meta[name=pubdate]",
            "meta[itemprop=datePublished]"
    };

    private static final String DATE_TEXT_SELECTOR = ".date, .post-date, .published, .publish-date, .article-date";

    private static final Pattern EXCLUDE_IMAGE_PATTERN = Pattern.compile("(?i)(icon|logo|sprite)");
    private static final Pattern DATE_TEXT_PATTERN = Pattern.compile("\\d{4}[-./]\\d{1,2}[-./]\\d{1,2}");

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
        Document doc = fetchDocument(url);

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
                throw new InvalidScrapingUrlException(url);
            }
        } catch (URISyntaxException e) {
            throw new InvalidScrapingUrlException(url);
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
            throw new BlogScrapingFetchException(url);
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
        if (StringUtils.hasText(ogDescription)) {
            return ogDescription;
        }
        String twitterDescription = metaContent(doc, "meta[name=twitter:description]");
        if (StringUtils.hasText(twitterDescription)) {
            return twitterDescription;
        }
        String metaDescription = metaContent(doc, "meta[name=description]");
        if (StringUtils.hasText(metaDescription)) {
            return metaDescription;
        }
        return extractFirstParagraphs(doc);
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

        Element dateEl = doc.selectFirst(DATE_TEXT_SELECTOR);
        if (dateEl != null) {
            String parsed = parseDate(extractDateByRegex(dateEl.text()));
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private String extractDateByRegex(String text) {
        Matcher matcher = DATE_TEXT_PATTERN.matcher(text);
        return matcher.find() ? matcher.group() : null;
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
