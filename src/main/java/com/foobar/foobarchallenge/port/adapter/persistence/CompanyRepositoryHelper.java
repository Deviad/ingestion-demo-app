package com.foobar.foobarchallenge.port.adapter.persistence;

import com.foobar.foobarchallenge.common.CaseInsensitiveKey;
import com.foobar.foobarchallenge.domain.model.AlternativeName;
import com.foobar.foobarchallenge.domain.model.ID;
import com.foobar.foobarchallenge.domain.model.Name;
import com.foobar.foobarchallenge.port.CustomResourceLoader;
import io.vavr.control.Try;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyRepositoryHelper {
  private final CustomResourceLoader customResourceLoader;
  private final List<String> noise = List.of("please refer to", "formerly known as", "formerly");

  @SneakyThrows
  public void index(
      Map<ID, Name> companyRepo,
      Map<CaseInsensitiveKey<Name>, ID> reverseCompanyRepo,
      Map<CaseInsensitiveKey<Name>, List<AlternativeName>> names) {

    Try.WithResources1<BufferedReader> bufferReaderTry =
        Try.withResources(
            () ->
                new BufferedReader(
                    new InputStreamReader(
                        customResourceLoader.getResource(
                            "/inputdata/companies/company_list.csv"))));

    var tryResult =
        bufferReaderTry.of(
            resource -> {
              boolean isHeader = true;
              for (String line = resource.readLine(); line != null; line = resource.readLine()) {
                if (isHeader) {
                  isHeader = false;
                  continue;
                }
                populateRepositoriesFromLines(companyRepo, reverseCompanyRepo, names, line);
              }
              return null;
            });
    tryResult.onFailure(
        throwable ->
            log.error(
                "Exception occurred in CompanyRepositoryHelper:index: " + throwable.getMessage()));
    log.info("Indexed");
  }

  private void populateRepositoriesFromLines(
      Map<ID, Name> companyRepo,
      Map<CaseInsensitiveKey<Name>, ID> reverseCompanyRepo,
      Map<CaseInsensitiveKey<Name>, List<AlternativeName>> names,
      String line) {
    String[] components = line.split(";", 2);
    String id = components[0];
    String name = getNameWithoutDoubleQuotes(components[1]);
    Pattern excludeParenthesisPattern = Pattern.compile("^(.*?)\\s*\\([^)]*\\)$");
    Matcher excludeParenthesisMatcher = excludeParenthesisPattern.matcher(name);

    if (excludeParenthesisMatcher.find()) {
      String nameOutsideParenthesis = excludeParenthesisMatcher.group(1);
      log.debug(nameOutsideParenthesis);
      companyRepo.put(new ID(id), new Name(nameOutsideParenthesis));
      reverseCompanyRepo.put(
          new CaseInsensitiveKey<>(new Name(nameOutsideParenthesis)), new ID(id));
      addAlternativeNames(names, name, nameOutsideParenthesis);
    } else {
      companyRepo.put(new ID(id), new Name(name));
      reverseCompanyRepo.put(new CaseInsensitiveKey<>(new Name(name)), new ID(id));
    }
  }

  private static String getNameWithoutDoubleQuotes(String string) {
    return string
        .chars()
        .filter(c -> c != '"')
        .mapToObj(Character::toString)
        .collect(Collectors.joining());
  }

  private void addAlternativeNames(
      Map<CaseInsensitiveKey<Name>, List<AlternativeName>> names,
      String fullName,
      String nameOutsideParenthesis) {
    Pattern insideParenthesisPattern = Pattern.compile("\\(([^)]+)\\)");
    Matcher insideParenthesisMatcher = insideParenthesisPattern.matcher(fullName);
    if (insideParenthesisMatcher.find()) {
      List<String> extractedText = getFilteredExtractedTest(insideParenthesisMatcher);
      extractedText.forEach(
          el ->
              names
                  .computeIfAbsent(
                      new CaseInsensitiveKey<>(new Name(nameOutsideParenthesis)),
                      n -> new ArrayList<>())
                  .add(new AlternativeName(el)));
    }
  }

  private List<String> getFilteredExtractedTest(Matcher insideParenthesisMatcher) {
    String extractedText = insideParenthesisMatcher.group(1);
    for (var el : noise) {
      extractedText = extractedText.replace(el, "");
    }
    return Arrays.asList(extractedText.split(";"));
  }
}
