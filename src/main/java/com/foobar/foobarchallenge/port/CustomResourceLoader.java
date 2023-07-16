package com.foobar.foobarchallenge.port;

import static com.foobar.foobarchallenge.domain.FoobarException.ErrorCode.CANNOT_LOAD_COMPANY_FILE;

import com.foobar.foobarchallenge.domain.FoobarException;
import io.vavr.control.Either;
import io.vavr.control.Try;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomResourceLoader {
  final ResourceLoader resourceLoader;

  public InputStream getResource(String filePath) {
    String folderPath = String.format("classpath:%s", filePath);
    Resource resource = resourceLoader.getResource(folderPath);

    Either<Throwable, InputStream> resourceEither = Try.of(resource::getInputStream).toEither();

    if (resourceEither.isLeft()) {
      log.error(
          "Exception in CompanyIndexerService:getResource with message: {}, {}",
          resourceEither.getLeft().getMessage(),
          List.of(resourceEither.getLeft().getStackTrace()));
      throw new FoobarException(CANNOT_LOAD_COMPANY_FILE, resourceEither.getLeft().getMessage());
    }
    return resourceEither.swap().getLeft();
  }

  public List<Resource> getResources(String path) {
    String folderPath = String.format("classpath:%s", path);
    Resource folderResource = resourceLoader.getResource(folderPath);
    if (folderResource.exists()) {
      var resourcesEither =
          Try.of(
                  () ->
                      ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                          .getResources(folderPath + "/*"))
              .toEither();

      return resourcesEither.fold(
          error -> {
            log.error(
                "Exception in CompanyIndexerService:index with message: {}, {}",
                error.getMessage(),
                List.of(error.getStackTrace()));
            return Collections.emptyList();
          },
          List::of);
    }
    return Collections.emptyList();
  }
}
