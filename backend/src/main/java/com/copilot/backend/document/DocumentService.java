package com.copilot.backend.document;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final VectorStore vectorStore;

    public int ingestPdf(Resource pdfResource) {
        // 1. Читаємо текст по сторінках із PDF
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource);
        List<Document> rawDocuments = pdfReader.get();

        // 2. Ріжемо великий текст на менші шматки (чанки), щоб LLM було легко їх читати
        TokenTextSplitter textSplitter = TokenTextSplitter.builder()
                .withChunkSize(500)
                .withMinChunkSizeChars(350)
                .withKeepSeparator(true)
                .build();
        List<Document> chunks = textSplitter.apply(rawDocuments);

        // 3. Відправляємо шматки в Ollama (mxbai-embed-large) і зберігаємо вектори в PostgreSQL
        vectorStore.add(chunks);

        return chunks.size();
    }
}
