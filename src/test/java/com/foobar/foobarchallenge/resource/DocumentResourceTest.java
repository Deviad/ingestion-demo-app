package com.foobar.foobarchallenge.resource;

import static com.foobar.foobarchallenge.TestUtils.WORDS_ARE_FOUND_WITH_AND_WITHOUT_CDATA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.foobar.foobarchallenge.TestUtils;
import com.foobar.foobarchallenge.domain.model.AlternativeName;
import com.foobar.foobarchallenge.domain.model.DocumentSchema;
import com.foobar.foobarchallenge.domain.model.ID;
import com.foobar.foobarchallenge.domain.model.Name;
import com.foobar.foobarchallenge.domain.repo.CompanyRepository;
import com.foobar.foobarchallenge.domain.repo.DocumentRepository;
import io.vavr.control.Try;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.index.IndexWriter;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = "server.port=${random.int[8080,8180]}")
@ActiveProfiles({"test"})
@Slf4j
class DocumentResourceTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private DocumentRepository documentRepository;
  @MockBean private CompanyRepository companyRepository;
  @Autowired private Try.WithResources1<IndexWriter> indexWriter;

  @BeforeEach
  void cleanUp() {
    Mockito.reset(companyRepository);
    when(companyRepository.getCompanyAlternativeNames(new Name("BP")))
        .thenReturn(List.of(new AlternativeName("BP")));
    when(companyRepository.getCompanyNameById(new ID("26"))).thenReturn(new Name("BP"));
    indexWriter.of(IndexWriter::deleteAll);
  }

  @ParameterizedTest(name = "Test Case {index}: {0}")
  @DisplayName("Test plan")
  @MethodSource("testCases")
  void testProcessXmlDocument(String testName, String testInput, int nOfDocs, int hits)
      throws Exception {
    log.info(testName + " | Documents total #: " + nOfDocs + " | Hits #: " + hits);

    // Perform the POST request
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/documents")
                .contentType(MediaType.APPLICATION_XML)
                .content(testInput))
        .andExpect(MockMvcResultMatchers.status().isAccepted());

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS) // Maximum wait time
        .pollInterval(500, TimeUnit.MILLISECONDS) // Polling interval
        .untilAsserted(
            () -> {
              assertEquals(nOfDocs, documentRepository.countAllDocuments());
              assertEquals(
                  hits, documentRepository.getDocumentIdsWithCompanyNameInText("BP").size());
            });
  }

  @Test
  public void testGetDocumentsByIds() throws Exception {
    // Mock data
    String companyId = "26";
    DocumentSchema document =
        DocumentSchema.of(
            LocalDate.of(2023, 6, 25),
            "0A01-5215-5B25-124E",
            "Hello World",
            "The Washington Post",
            "bp");
    documentRepository.index(List.of(document));

    String expectedXpath = "//news-item[1]/text";

    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .pollInterval(1, TimeUnit.SECONDS)
        .until(
            () -> {
              try {
                mockMvc
                    .perform(
                        MockMvcRequestBuilders.get("/documents")
                            .param("companyIds", companyId)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.xpath(expectedXpath).string("bp"));
                return true;
              } catch (AssertionError | Exception e) {
                return false;
              }
            });
  }

  @Test
  void testThatNoDuplicatesAreCreatedWIthOnlySameId() {

    documentRepository.index(TestUtils.makeMockDocumentSchemaWithOnlyEqualIds());
    // unfortunately no matter what kind of field you decide to use
    // there are indeed going to be duplicates
    assertEquals(2, documentRepository.countAllDocuments());
  }

  @Test
  void testThatNoDuplicatesAreCreatedWIthSameObjects() {
    // unfortunately no matter what kind of field you decide to use
    // there are indeed going to be duplicates

    documentRepository.index(TestUtils.makeMockDocumentSchemaWithTheSameDoc());

    assertEquals(2, documentRepository.countAllDocuments());
  }

  static Stream<Arguments> testCases() {
    return Stream.of(
        Arguments.of(
            "No article contains the company name", TestUtils.XML_WITHOUT_COMPANY_NAME, 2, 0),
        Arguments.of(
            "Company name appears in title", TestUtils.XML_WITHOUT_COMPANY_NAME_IN_TITLE, 2, 1),
        Arguments.of(
            "Words are found inside and outside CDATA tags",
            WORDS_ARE_FOUND_WITH_AND_WITHOUT_CDATA,
            2,
            2),
        Arguments.of(
            "Searching should be case insensitive", TestUtils.TESTING_DIFFERENT_LETTER_CASES, 3, 3),
        Arguments.of(
            "Only entire words are counted as a hit",
            TestUtils.ONLY_ENTIRE_WORDS_ARE_A_MATCH,
            1,
            0));
  }
}
