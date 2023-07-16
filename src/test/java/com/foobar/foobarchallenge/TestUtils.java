package com.foobar.foobarchallenge;

import com.foobar.foobarchallenge.domain.model.DocumentSchema;
import java.time.LocalDate;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestUtils {
  // language=XML
  public static final String XML_WITHOUT_COMPANY_NAME =
      """
            <?xml version="1.0"?>
            <items>
                <news-item id="0A01-5215-5B25-124C">
                    <date>2014-03-14</date>
                    <title>US contract ban</title>
                    <source>Digital Journal</source>
                    <author></author>
                    <text><![CDATA[ Foo ]]></text>
                </news-item>
                <news-item id="0A01-5215-5B25-124E">
                    <date>2014-03-14</date>
                    <title>US contract ban2</title>
                    <source>Digital Journal</source>
                    <author></author>
                    <text><![CDATA[ Bar ]]></text>
                </news-item>
            </items>""";

  // language=XML
  public static final String XML_WITHOUT_COMPANY_NAME_IN_TITLE =
      """
                <?xml version="1.0"?>
                <items>
                    <news-item id="0A01-5215-5B25-124C">
                        <date>2014-03-14</date>
                        <title>US ends BP contract ban</title>
                        <source>Digital Journal</source>
                        <author></author>
                        <text><![CDATA[ Foo ]]></text>
                    </news-item>
                    <news-item id="0A01-5215-5B25-124E">
                        <date>2014-03-14</date>
                        <title>US contract ban2</title>
                        <source>Digital Journal</source>
                        <author></author>
                        <text><![CDATA[ Bar ]]></text>
                    </news-item>
                </items>""";

  // language=XML
  public static final String TESTING_DIFFERENT_LETTER_CASES =
      """
            <?xml version="1.0"?>
            <items>
                <news-item id="0A01-5215-5B25-124C">
                    <date>2014-03-14</date>
                    <title>US ends BP contract ban</title>
                    <source>Digital Journal</source>
                    <author></author>
                    <text><![CDATA[ BP ]]></text>
                </news-item>
                <news-item id="0A01-5215-5B25-124E">
                    <date>2014-03-14</date>
                    <title>US ends BP contract ban2</title>
                    <source>Digital Journal</source>
                    <author></author>
                    <text><![CDATA[ bP ]]></text>
                </news-item>
                <news-item id="0A01-5215-5B25-124E">
                    <date>2014-03-14</date>
                    <title>US ends BP contract ban2</title>
                    <source>Digital Journal</source>
                    <author></author>
                    <text><![CDATA[ bp ]]></text>
                </news-item>
            </items>""";

  public static List<DocumentSchema> makeMockDocumentSchemaWithOnlyEqualIds() {
    DocumentSchema doc1 = DocumentSchema.of(LocalDate.of(2023, 10, 25), "2", "abc", "NYT", "hello");
    DocumentSchema doc2 =
        DocumentSchema.of(LocalDate.of(2024, 12, 26), "2", "abcd", "WP", "hello2");

    return List.of(doc1, doc2);
  }

  public static List<DocumentSchema> makeMockDocumentSchemaWithTheSameDoc() {
    DocumentSchema doc1 = DocumentSchema.of(LocalDate.of(2023, 10, 25), "2", "abc", "NYT", "hello");

    return List.of(doc1, doc1);
  }
  // language=XML
  public static final String WORDS_ARE_FOUND_WITH_AND_WITHOUT_CDATA =
      """
            <?xml version="1.0"?>
            <items>
                <news-item id="0A01-5215-5B25-124E">
                    <date>2014-03-14</date>
                    <title>US ends BP contract ban2</title>
                    <source>Digital Journal</source>
                    <author></author>
                    <text><![CDATA[ bp ]]></text>
                </news-item>
                <news-item id="0A01-5215-5B25-124E">
                    <date>2014-03-14</date>
                    <title>US ends BP contract ban2</title>
                    <source>Digital Journal</source>
                    <author></author>
                    <text> bp </text>
                </news-item>
            </items>""";

  // language=XML
  public static final String ONLY_ENTIRE_WORDS_ARE_A_MATCH =
      """
            <?xml version="1.0"?>
            <items>
                <news-item id="0A01-5215-5B25-124E">
                    <date>2014-03-14</date>
                    <title>US ends BPc contract ban2</title>
                    <source>Digital Journal</source>
                    <author></author>
                    <text> bpc </text>
                </news-item>
            </items>""";

  // language=XML
  public static final String FIELDS_ARE_MAPPED_CORRECTLY =
      """
           <news-item id="0A01-5215-5B25-124E">
                  <date>2014-03-14</date>
                  <title>US ends BP contract ban2</title>
                  <source>Digital Journal</source>
                  <author></author>
                  <text> bp </text>
           </news-item>
          """;
}
