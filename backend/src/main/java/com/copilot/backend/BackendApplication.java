package com.copilot.backend;

import com.copilot.backend.expense.ExpenseTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider expenseToolCallbackProvider(ExpenseTools expenseTools) {
		return MethodToolCallbackProvider.builder()
				.toolObjects(expenseTools)
				.build();
	}

}
