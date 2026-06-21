package com.storex.storex.controller;

import com.storex.storex.service.RagService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/files")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/{id}/rag/ingest")
    public ResponseEntity<String> ingestFile(@PathVariable Long id) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            ragService.ingestFile(id, currentUsername);
            return ResponseEntity.ok("Document indexed successfully into vector database.");
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (com.storex.storex.exception.FileNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error during document indexing: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/rag/chat")
    public ResponseEntity<String> chatWithFile(
            @PathVariable Long id,
            @RequestBody ChatRequest request
    ) {
        if (request == null || request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Question cannot be empty");
        }

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            String answer = ragService.chatWithFile(id, request.getQuestion(), currentUsername);
            return ResponseEntity.ok(answer);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (com.storex.storex.exception.FileNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error during retrieval query: " + e.getMessage());
        }
    }

    // Static helper class for mapping request body
    public static class ChatRequest {
        private String question;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }
    }
}
