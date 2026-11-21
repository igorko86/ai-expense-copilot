package com.copilot.backend.document;

import com.copilot.backend.expense.Expense;
import com.copilot.backend.expense.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ai/documents")
public class DocumentController {

    private final DocumentService documentService;


    @PostMapping("/upload-pdf")
    public String uploadPdf(@RequestParam("file") MultipartFile file) {
        int chunksCount =  documentService.ingestPdf(file.getResource());

        return "PDF successfully processed into " + chunksCount + " vector chunks!";
    }

}
