package com.foobar.foobarchallenge.domain.repo;

import com.foobar.foobarchallenge.domain.model.DocumentSchema;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

public interface DocumentRepository {
    long index(List<DocumentSchema> schemas);
    List<Document> getDocumentsBySearchPhrase(Query searchPhrase);

    long countAllDocuments();

    List<Document> getDocumentsWithCompanyNameInTextAndTitle(String companyName);

    List<String> getDocumentTitlesWithCompanyNameInText(String companyName);

    List<String> getDocumentIdsWithCompanyNameInText(String companyName);
}
