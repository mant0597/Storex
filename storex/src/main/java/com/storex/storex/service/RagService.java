package com.storex.storex.service;

import com.storex.storex.entity.FileMetadata;
import com.storex.storex.exception.FileNotFoundException;
import com.storex.storex.repository.FileMetadataRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final FileService fileService;
    private final FileMetadataRepository repository;
    private final VectorStore vectorStore;
    private final ChatModel chatModel;

    public RagService(
            FileService fileService,
            FileMetadataRepository repository,
            VectorStore vectorStore,
            ChatModel chatModel
    ) {
        this.fileService = fileService;
        this.repository = repository;
        this.vectorStore = vectorStore;
        this.chatModel = chatModel;
    }

    public void ingestFile(Long fileId, String username) throws Exception {
        // 1. Fetch metadata and check ownership
        FileMetadata metadata = repository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with id " + fileId));

        if (!metadata.getUploadedBy().equals(username)) {
            throw new AccessDeniedException("Access Denied: You do not own this file");
        }

        // 2. Fetch the PDF resource from MinIO via FileService
        Resource pdfResource = fileService.getFile(fileId);

        // 3. Read PDF pages as Document chunks
        PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfResource);
        List<Document> documents = reader.read();

        // 4. Split PDF pages into smaller overlapping tokens
        TokenTextSplitter splitter = TokenTextSplitter.builder().build();
        List<Document> splitDocs = splitter.split(documents);

        // 5. Inject metadata fileId into each split chunk to allow filtered searches
        for (Document doc : splitDocs) {
            doc.getMetadata().put("fileId", fileId);
        }

        // 6. Generate embeddings and save chunks to Qdrant Vector Store
        vectorStore.accept(splitDocs);
    }

    public String chatWithFile(Long fileId, String question, String username) throws Exception {
        // 1. Check ownership
        FileMetadata metadata = repository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with id " + fileId));

        if (!metadata.getUploadedBy().equals(username)) {
            throw new AccessDeniedException("Access Denied: You do not own this file");
        }

        // 2. Construct search request with metadata filter
        FilterExpressionBuilder filterBuilder = new FilterExpressionBuilder();
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(4)
                .filterExpression(filterBuilder.eq("fileId", String.valueOf(fileId)).build())
                .build();

        // 3. Perform similarity search in Qdrant
        List<Document> contextDocs = vectorStore.similaritySearch(searchRequest);

        if (contextDocs.isEmpty()) {
            return "Could not find any relevant information in the document to answer your question.";
        }

        // 4. Join context text chunks
        String context = contextDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        // 5. Create prompt template
        String promptTemplateText = """
                You are an intelligent assistant helping a user extract information from a document they uploaded.
                Answer the user's question based strictly on the provided context. If the answer cannot be found in the context, say "I cannot find the answer to your question in the provided document."
                
                CONTEXT:
                {context}
                
                QUESTION:
                {question}
                
                ANSWER:
                """;

        PromptTemplate template = new PromptTemplate(promptTemplateText);
        Prompt prompt = template.create(Map.of(
                "context", context,
                "question", question
        ));

        // 6. Query the LLM (Ollama) and return response
        return chatModel.call(prompt).getResult().getOutput().getText();
    }
}
