const API_BASE = 'http://localhost:8080/api';
const EXPENSES = `${API_BASE}/ai/expenses`;
const DOCUMENTS = `${API_BASE}/ai/documents`;

export interface Expense {
  id: number;
  description: string;
  amount: number;
  currency: string;
  category: string;
  expenseDate: string;
}

export async function fetchExpenses(): Promise<Expense[]> {
  const res = await fetch(EXPENSES);
  if (!res.ok) throw new Error('Failed to fetch expenses');
  return res.json();
}

export async function parseAndSaveExpense(text: string): Promise<Expense[]> {
  const res = await fetch(`${EXPENSES}/parse`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ text }),
  });
  if (!res.ok) throw new Error('Failed to parse expense');
  console.log(res);
  return res.json();
}

export async function askAiAssistant(question: string): Promise<string> {
  const res = await fetch(`${EXPENSES}/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question }),
  });
  if (!res.ok) throw new Error('Failed to get AI answer');
  const data = await res.json();
  return data.answer;
}

export async function uploadPdfDocument(file: File): Promise<string> {
  const formData = new FormData();
  formData.append('file', file);

  const res = await fetch(`${DOCUMENTS}/upload-pdf`, {
    method: 'POST',
    body: formData,
  });
  if (!res.ok) throw new Error('Failed to upload PDF');
  return res.text();
}

export function streamAiAssistant(
  question: string,
  onChunk: (chunk: string) => void,
  onDone: () => void,
  onError: () => void
) {
  const url = `${EXPENSES}/chat/stream?question=${encodeURIComponent(question)}`;
  const eventSource = new EventSource(url);

  eventSource.onmessage = (event) => {
    onChunk(event.data);
  };

  eventSource.onerror = () => {
    eventSource.close();
    onDone();
  };

  return () => eventSource.close();
}
